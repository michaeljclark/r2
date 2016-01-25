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

package com.io7m.r2.core;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.parameterized.PMatrixWritable4x4FType;
import com.io7m.jtensors.parameterized.PVector3FType;
import com.io7m.jtensors.parameterized.PVectorM3F;
import com.io7m.r2.spaces.R2SpaceObjectType;
import com.io7m.r2.spaces.R2SpaceWorldType;

/**
 * <p>A transform represented by a translation.</p>
 *
 * <p>The transform does not allow independent scaling on each axis and will
 * therefore produce matrices that are guaranteed to be orthogonal.</p>
 */

public final class R2TransformT implements
  R2TransformOrthogonalReadableType
{
  private final PVector3FType<R2SpaceWorldType> translation;
  private final Runnable                        changed;

  private R2TransformT(
    final Runnable in_changed,
    final PVector3FType<R2SpaceWorldType> in_translation)
  {
    NullCheck.notNull(in_changed);
    NullCheck.notNull(in_translation);

    this.changed = in_changed;
    this.translation =
      new R2TransformNotifyingTensors.R2PVectorM3F<>(
        in_changed, in_translation);
  }

  /**
   * Construct a transform using the given initial values. The given vectors are
   * not copied; any modifications made to them will be reflected in the
   * transform.
   *
   * @param in_translation The translation
   *
   * @return A new transform
   */

  public static R2TransformT newTransformWithValues(
    final PVector3FType<R2SpaceWorldType> in_translation)
  {
    return new R2TransformT(() -> {
    }, in_translation);
  }

  /**
   * Construct a transform using the default values: The translation {@code (0,
   * 0, 0)}.
   *
   * @return A new transform
   */

  public static R2TransformT newTransform()
  {
    return new R2TransformT(() -> {
    }, new PVectorM3F<>(0.0f, 0.0f, 0.0f));
  }

  /**
   * <p>Construct a transform using the default values: The translation {@code
   * (0, 0, 0)}.</p>
   *
   * @param in_changed The procedure that will be executed every time the value
   *                   of this transform is changed
   *
   * @return A new transform
   */

  public static R2TransformT newTransformWithNotifier(
    final Runnable in_changed)
  {
    return new R2TransformT(in_changed, new PVectorM3F<>(0.0f, 0.0f, 0.0f));
  }

  /**
   * @return A translation in world-space
   */

  public PVector3FType<R2SpaceWorldType> getTranslation()
  {
    return this.translation;
  }

  @Override
  public void transformMakeMatrix4x4F(
    final R2TransformContextType context,
    final PMatrixWritable4x4FType<R2SpaceObjectType, R2SpaceWorldType> m)
  {
    NullCheck.notNull(context);
    NullCheck.notNull(m);
    MatrixM4x4F.makeTranslation3F(this.translation, m);
  }
}
