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

import com.io7m.jareas.core.AreaInclusiveUnsignedL;
import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.junsigned.ranges.UnsignedRangeInclusiveL;
import org.valid4j.Assertive;

import java.util.Objects;

/**
 * Functions over render target descriptions.
 */

public final class R2RenderTargetDescriptions
{
  private R2RenderTargetDescriptions()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Scale the given render target description by {@code scale}. The resulting
   * description area will have the same lower corner - only the upper corner is
   * scaled.
   *
   * @param a     A description
   * @param scale The scale value
   * @param <T>   The precise type of render target description
   *
   * @return A new description
   */

  @SuppressWarnings("unchecked")
  public static <T extends R2RenderTargetDescriptionType> T scale(
    final T a,
    final double scale)
  {
    NullCheck.notNull(a);

    final AreaInclusiveUnsignedLType ao = a.getArea();
    final UnsignedRangeInclusiveL rx_o = ao.getRangeX();
    final UnsignedRangeInclusiveL ry_o = ao.getRangeY();

    final UnsignedRangeInclusiveL rx =
      new UnsignedRangeInclusiveL(
        rx_o.getLower(), (long) ((double) rx_o.getUpper() * scale));
    final UnsignedRangeInclusiveL ry =
      new UnsignedRangeInclusiveL(
        ry_o.getLower(), (long) ((double) ry_o.getUpper() * scale));

    final AreaInclusiveUnsignedL b = AreaInclusiveUnsignedL.of(rx, ry);
    final R2RenderTargetDescriptionType r = a.withArea(b);
    Assertive.ensure(Objects.equals(a.getClass(), r.getClass()));

    return (T) r;
  }
}