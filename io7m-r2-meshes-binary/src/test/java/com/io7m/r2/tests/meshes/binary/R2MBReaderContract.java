/*
 * Copyright © 2015 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.r2.tests.meshes.binary;

import com.io7m.r2.meshes.R2MeshParserListenerType;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class R2MBReaderContract
{
  @Rule public ExpectedException expected = ExpectedException.none();

  protected abstract Runnable getReader(
    String name,
    R2MeshParserListenerType listener)
    throws IOException;

  @Test public final void testEmpty()
    throws Exception
  {
    final Runnable r = this.getReader("empty.r2mb", new Listener());

    this.expected.expect(ParseError.class);
    this.expected.expectMessage(new StringStartsWith("Error parsing header"));
    r.run();
  }

  @Test public final void testBadMagicNumber()
    throws Exception
  {
    final Runnable r = this.getReader("bad_magic.r2mb", new Listener());

    this.expected.expect(ParseError.class);
    this.expected.expectMessage(new StringStartsWith("Bad magic number"));
    r.run();
  }

  @Test public final void testBadVersion()
    throws Exception
  {
    final Runnable r = this.getReader("bad_version.r2mb", new Listener());

    this.expected.expect(ParseError.class);
    this.expected.expectMessage(new StringStartsWith("Unsupported version"));
    r.run();
  }

  @Test public final void testNoData()
    throws Exception
  {
    final AtomicLong tri = new AtomicLong(Long.MAX_VALUE);
    final AtomicLong ver = new AtomicLong(Long.MAX_VALUE);

    final Runnable r = this.getReader(
      "no_data.r2mb",
      new Listener()
      {
        @Override public void onEventTriangleCount(final long count)
        {
          tri.set(count);
        }

        @Override public void onEventVertexCount(final long count)
        {
          ver.set(count);
        }
      });

    r.run();

    Assert.assertEquals(0L, tri.get());
    Assert.assertEquals(0L, ver.get());
  }

  @Test public final void testTri()
    throws Exception
  {
    final AtomicLong tri = new AtomicLong(Long.MAX_VALUE);
    final AtomicLong ver = new AtomicLong(Long.MAX_VALUE);
    final AtomicBoolean ver_started = new AtomicBoolean(false);
    final AtomicBoolean ver_finished = new AtomicBoolean(false);
    final AtomicBoolean ver_finished_all = new AtomicBoolean(false);
    final AtomicBoolean tri_finished_all = new AtomicBoolean(false);
    final AtomicBoolean tri_received = new AtomicBoolean(false);
    final AtomicBoolean pos_received = new AtomicBoolean(false);
    final AtomicBoolean nor_received = new AtomicBoolean(false);
    final AtomicBoolean tan_received = new AtomicBoolean(false);
    final AtomicBoolean uv_received = new AtomicBoolean(false);

    final Runnable r = this.getReader(
      "one_tri.r2mb",
      new Listener()
      {
        @Override public void onEventTriangleCount(final long count)
        {
          tri.set(count);
        }

        @Override public void onEventTrianglesFinished()
        {
          Assert.assertFalse(tri_finished_all.get());
          tri_finished_all.set(true);
        }

        @Override public void onEventVerticesFinished()
        {
          Assert.assertFalse(ver_finished_all.get());
          ver_finished_all.set(true);
        }

        @Override public void onEventVertexStarted(final int index)
        {
          Assert.assertFalse(ver_started.get());
          ver_started.set(true);
        }

        @Override public void onEventVertexFinished(final int index)
        {
          Assert.assertFalse(ver_finished.get());
          ver_finished.set(true);
        }

        @Override public void onEventTriangle(
          final int index,
          final int v0,
          final int v1,
          final int v2)
        {
          Assert.assertFalse(tri_received.get());
          Assert.assertEquals(0L, (long) index);
          Assert.assertEquals(0L, (long) v0);
          Assert.assertEquals(1L, (long) v1);
          Assert.assertEquals(2L, (long) v2);
          tri_received.set(true);
        }

        @Override public void onEventVertexNormal(
          final int index,
          final float x,
          final float y,
          final float z)
        {
          Assert.assertFalse(nor_received.get());
          Assert.assertEquals(0L, (long) index);
          Assert.assertEquals(0.0, (double) x, 0.000001);
          Assert.assertEquals(0.0, (double) y, 0.000001);
          Assert.assertEquals(1.0, (double) z, 0.000001);
          nor_received.set(true);
        }

        @Override public void onEventVertexPosition(
          final int index,
          final float x,
          final float y,
          final float z)
        {
          Assert.assertFalse(pos_received.get());
          Assert.assertEquals(0L, (long) index);
          Assert.assertEquals(10.0, (double) x, 0.000001);
          Assert.assertEquals(20.0, (double) y, 0.000001);
          Assert.assertEquals(30.0, (double) z, 0.000001);
          pos_received.set(true);
        }

        @Override public void onEventVertexTangent(
          final int index,
          final float x,
          final float y,
          final float z,
          final float w)
        {
          Assert.assertFalse(tan_received.get());
          Assert.assertEquals(0L, (long) index);
          Assert.assertEquals(1.0, (double) x, 0.000001);
          Assert.assertEquals(0.0, (double) y, 0.000001);
          Assert.assertEquals(0.0, (double) z, 0.000001);
          Assert.assertEquals(1.0, (double) w, 0.000001);
          tan_received.set(true);
        }

        @Override public void onEventVertexUV(
          final int index,
          final float x,
          final float y)
        {
          Assert.assertFalse(uv_received.get());
          Assert.assertEquals(0L, (long) index);
          Assert.assertEquals(2.0, (double) x, 0.000001);
          Assert.assertEquals(3.0, (double) y, 0.000001);
          uv_received.set(true);
        }

        @Override public void onEventVertexCount(final long count)
        {
          ver.set(count);
        }
      });

    r.run();

    Assert.assertEquals(1L, tri.get());
    Assert.assertEquals(1L, ver.get());
    Assert.assertTrue(tri_received.get());
    Assert.assertTrue(pos_received.get());
    Assert.assertTrue(nor_received.get());
    Assert.assertTrue(tan_received.get());
    Assert.assertTrue(uv_received.get());
    Assert.assertTrue(ver_finished.get());
    Assert.assertTrue(ver_finished_all.get());
    Assert.assertTrue(ver_started.get());
    Assert.assertTrue(tri_finished_all.get());
  }

  private static final class ParseError extends RuntimeException
  {
    private static final long serialVersionUID = 1L;

    ParseError(final String message)
    {
      super(message);
    }
  }

  private static class Listener implements R2MeshParserListenerType
  {
    @Override public void onEventVertexCount(final long count)
    {

    }

    @Override public void onEventTriangleCount(final long count)
    {

    }

    @Override public final void onError(
      final Optional<Throwable> e,
      final String message)
    {
      throw new ParseError(message);
    }

    @Override public void onEventVertexStarted(final int index)
    {

    }

    @Override public void onEventVertexPosition(
      final int index,
      final float x,
      final float y,
      final float z)
    {

    }

    @Override public void onEventVertexNormal(
      final int index,
      final float x,
      final float y,
      final float z)
    {

    }

    @Override public void onEventVertexTangent(
      final int index,
      final float x,
      final float y,
      final float z,
      final float w)
    {

    }

    @Override public void onEventVertexUV(
      final int index,
      final float x,
      final float y)
    {

    }

    @Override public void onEventVertexFinished(final int index)
    {

    }

    @Override public void onEventVerticesFinished()
    {

    }

    @Override public void onEventTriangle(
      final int index,
      final int v0,
      final int v1,
      final int v2)
    {

    }

    @Override public void onEventTrianglesFinished()
    {

    }
  }
}
