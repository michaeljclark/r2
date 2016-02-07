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
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;

/**
 * The type of usable filters that do not require access to the view values that
 * created the filtered scene.
 *
 * @param <T> The type of render target to which the filter expects to render
 * @param <P> The type of filter parameters
 */

public interface R2FilterWithoutViewUsableType<T extends
  R2RenderTargetUsableType, P> extends
  R2FilterUsableType<T, P>
{
  /**
   * Run the filter for the given parameters. The output will be written to
   * {@code target}.
   *
   * @param g          A GL interface
   * @param uc         A texture unit context
   * @param parameters The filter parameters
   * @param target     The render target
   */

  void runFilter(
    JCGLInterfaceGL33Type g,
    R2TextureUnitContextParentType uc,
    P parameters,
    T target);

  /**
   * Run the filter for the given parameters. The output will be written to the
   * currently bound render target (which is expected to be of type {@code T}
   * for correct results).
   *
   * @param g          A GL interface
   * @param uc         A texture unit context
   * @param parameters The filter parameters
   * @param area       The size of the current viewport
   */

  void runFilterWithBoundBuffer(
    JCGLInterfaceGL33Type g,
    R2TextureUnitContextParentType uc,
    P parameters,
    AreaInclusiveUnsignedLType area);
}
