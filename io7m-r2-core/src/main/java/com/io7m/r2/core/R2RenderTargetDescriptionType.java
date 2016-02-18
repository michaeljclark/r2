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

import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import org.immutables.value.Value;

/**
 * The type of render target descriptions.
 */

public interface R2RenderTargetDescriptionType
{
  /**
   * @return The inclusive area of the framebuffer.
   */

  @Value.Parameter
  AreaInclusiveUnsignedLType getArea();

  /**
   * Return a new description with the given area.
   *
   * <i>Note: Implementations are NOT permitted to return a different concrete
   * class. In other words, {@code c.getClass() == c.withArea(a).getClass()}
   * must hold!</i>
   *
   * @param a The new area
   *
   * @return A copy of this description with the given area
   */

  R2RenderTargetDescriptionType withArea(AreaInclusiveUnsignedLType a);
}