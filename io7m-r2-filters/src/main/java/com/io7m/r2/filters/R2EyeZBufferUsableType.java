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

package com.io7m.r2.filters;

import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.JCGLResourceSizedType;
import com.io7m.jcanephora.core.JCGLResourceUsableType;
import com.io7m.r2.core.R2Texture2DUsableType;

/**
 * The type of usable eye-Z buffers.
 */

public interface R2EyeZBufferUsableType extends JCGLResourceSizedType,
  JCGLResourceUsableType
{
  /**
   * @return The eye-Z texture
   */

  R2Texture2DUsableType getEyeZTexture();

  /**
   * @return The framebuffer
   */

  JCGLFramebufferUsableType getFramebuffer();

  /**
   * @return The viewport area
   */

  AreaInclusiveUnsignedLType getArea();
}
