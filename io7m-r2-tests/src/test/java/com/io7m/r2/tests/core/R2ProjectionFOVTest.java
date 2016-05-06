/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

package com.io7m.r2.tests.core;

import com.io7m.jcanephora.core.JCGLProjectionMatrices;
import com.io7m.jcanephora.core.JCGLProjectionMatricesType;
import com.io7m.jranges.RangeCheckException;
import com.io7m.r2.core.R2ProjectionFOV;
import com.io7m.r2.core.R2ProjectionReadableType;
import com.io7m.r2.core.R2WatchableType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicInteger;

public final class R2ProjectionFOVTest
{
  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testValues()
  {
    final JCGLProjectionMatricesType pm =
      JCGLProjectionMatrices.newMatrices();
    final R2ProjectionFOV p =
      R2ProjectionFOV.newFrustumWith(
        pm,
        1.5707963267948966f,
        1.0f,
        1.0f,
        100.0f);

    p.projectionSetZFar(100.0f);
    p.projectionSetZNear(1.0f);

    Assert.assertEquals(1.0f, p.projectionGetNearXMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, p.projectionGetNearXMinimum(), 0.0f);
    Assert.assertEquals(1.0f, p.projectionGetNearYMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, p.projectionGetNearYMinimum(), 0.0f);
    Assert.assertEquals(1.0f, p.projectionGetZNear(), 0.0f);

    Assert.assertEquals(100.0f, p.projectionGetFarXMaximum(), 0.0f);
    Assert.assertEquals(-100.0f, p.projectionGetFarXMinimum(), 0.0f);
    Assert.assertEquals(100.0f, p.projectionGetFarYMaximum(), 0.0f);
    Assert.assertEquals(-100.0f, p.projectionGetFarYMinimum(), 0.0f);
    Assert.assertEquals(100.0f, p.projectionGetZFar(), 0.0f);

    p.projectionSetZFar(200.0f);

    Assert.assertEquals(200.0f, p.projectionGetFarXMaximum(), 0.0f);
    Assert.assertEquals(-200.0f, p.projectionGetFarXMinimum(), 0.0f);
    Assert.assertEquals(200.0f, p.projectionGetFarYMaximum(), 0.0f);
    Assert.assertEquals(-200.0f, p.projectionGetFarYMinimum(), 0.0f);
    Assert.assertEquals(200.0f, p.projectionGetZFar(), 0.0f);

    p.setAspectRatio(2.0f);
    Assert.assertEquals(2.0f, p.getAspectRatio(), 0.0f);
    p.setHorizontalFOV(0.43633f);
    Assert.assertEquals(0.43633, (double) p.getHorizontalFOV(), 0.00001);
  }

  @Test
  public void testAspectNonzero()
  {
    final JCGLProjectionMatricesType pm =
      JCGLProjectionMatrices.newMatrices();
    final R2ProjectionFOV p =
      R2ProjectionFOV.newFrustumWith(
        pm,
        1.5707963267948966f,
        1.0f,
        1.0f,
        100.0f);

    this.expected.expect(RangeCheckException.class);
    p.setAspectRatio(0.0f);
  }

  @Test
  public void testWatchable()
  {
    final JCGLProjectionMatricesType pm =
      JCGLProjectionMatrices.newMatrices();
    final R2ProjectionFOV p =
      R2ProjectionFOV.newFrustumWith(
        pm,
        1.5707963267948966f,
        1.0f,
        1.0f,
        100.0f);

    final AtomicInteger called = new AtomicInteger(0);
    final R2WatchableType<R2ProjectionReadableType> w =
      p.projectionGetWatchable();
    w.watchableAdd(ww -> called.incrementAndGet());

    Assert.assertEquals(1L, (long) called.get());

    p.projectionSetZFar(100.0f);
    p.projectionSetZNear(1.0f);
    p.setAspectRatio(1.0f);
    p.setHorizontalFOV(1.0f);

    Assert.assertEquals(5L, (long) called.get());
  }
}
