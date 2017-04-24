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

package com.io7m.r2.core.cursors;

import com.io7m.jcanephora.core.JCGLScalarType;
import com.io7m.jnull.NullCheck;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;

import java.nio.ByteBuffer;

/**
 * A cursor for the {@link R2VertexP32UNT16ByteBuffered} type.
 */

public final class R2VertexCursorP32UNT16 implements
  R2VertexCursorProducerType<ByteBuffer>,
  R2VertexCursorProducerInfoType
{
  private static final R2VertexCursorP32UNT16 INSTANCE;

  static {
    INSTANCE = new R2VertexCursorP32UNT16();
  }

  private R2VertexCursorP32UNT16()
  {

  }

  /**
   * @return Access to a {@link R2VertexCursorP32UNT16} instance
   */

  public static R2VertexCursorP32UNT16 getInstance()
  {
    return INSTANCE;
  }

  @Override
  public JCGLScalarType positionElementType()
  {
    return JCGLScalarType.TYPE_FLOAT;
  }

  @Override
  public long positionOffset()
  {
    return (long) R2VertexP32UNT16ByteBuffered
      .metaPositionStaticOffsetFromType();
  }

  @Override
  public JCGLScalarType normalElementType()
  {
    return JCGLScalarType.TYPE_HALF_FLOAT;
  }

  @Override
  public long normalOffset()
  {
    return (long) R2VertexP32UNT16ByteBuffered.metaNormalStaticOffsetFromType();
  }

  @Override
  public JCGLScalarType uvElementType()
  {
    return JCGLScalarType.TYPE_HALF_FLOAT;
  }

  @Override
  public long uvOffset()
  {
    return (long) R2VertexP32UNT16ByteBuffered.metaUvStaticOffsetFromType();
  }

  @Override
  public JCGLScalarType tangent4ElementType()
  {
    return JCGLScalarType.TYPE_HALF_FLOAT;
  }

  @Override
  public long tangent4Offset()
  {
    return (long) R2VertexP32UNT16ByteBuffered
      .metaTangentStaticOffsetFromType();
  }

  @Override
  public long vertexSize()
  {
    return (long) R2VertexP32UNT16ByteBuffered.sizeInOctets();
  }

  @Override
  public R2VertexCursorType newCursor(final ByteBuffer ctx)
  {
    final JPRACursor1DType<R2VertexP32UNT16Type> c =
      JPRACursor1DByteBufferedChecked.newCursor(
        ctx,
        R2VertexP32UNT16ByteBuffered::newValueWithOffset);
    return new Cursor(c);
  }

  private static final class Cursor implements R2VertexCursorType
  {
    private final JPRACursor1DType<R2VertexP32UNT16Type> cursor;
    private final R2VertexP32UNT16Type view;

    private Cursor(final JPRACursor1DType<R2VertexP32UNT16Type> c)
    {
      this.cursor = NullCheck.notNull(c, "Cursor");
      this.view = c.getElementView();
    }

    @Override
    public void setPosition(
      final long index,
      final double x,
      final double y,
      final double z)
    {
      this.cursor.setElementIndex((int) index);
      this.view.getPositionWritable().setXYZ(x, y, z);
    }

    @Override
    public void setNormal(
      final long index,
      final double x,
      final double y,
      final double z)
    {
      this.cursor.setElementIndex((int) index);
      this.view.getNormalWritable().setXYZ(x, y, z);
    }

    @Override
    public void setUV(
      final long index,
      final double x,
      final double y)
    {
      this.cursor.setElementIndex((int) index);
      this.view.getUvWritable().setXY(x, y);
    }

    @Override
    public void setTangent4(
      final long index,
      final double x,
      final double y,
      final double z,
      final double w)
    {
      this.cursor.setElementIndex((int) index);
      this.view.getTangentWritable().setXYZW(x, y, z, w);
    }
  }
}