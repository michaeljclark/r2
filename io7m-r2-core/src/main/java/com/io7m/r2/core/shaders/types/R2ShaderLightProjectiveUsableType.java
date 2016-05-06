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

package com.io7m.r2.core.shaders.types;

import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.r2.core.R2LightProjectiveReadableType;
import com.io7m.r2.core.R2MatricesProjectiveLightValuesType;

/**
 * The type of usable single-instance projective light shaders.
 *
 * @param <M> The type of shader parameters
 */

public interface R2ShaderLightProjectiveUsableType<
  M extends R2LightProjectiveReadableType>
  extends R2ShaderLightVolumeSingleUsableType<M>
{
  /**
   * <p>Set shader values that are derived from the current projective
   * light.</p>
   *
   * <p>This method will be called once for each projective light that uses the
   * current shader.</p>
   *
   * <p>This method will be called after a call to {@link
   * #onActivate(JCGLShadersType)} and before a call to {@link
   * #onValidate()}.</p>
   *
   * @param g_sh An OpenGL interface
   * @param m    The instance matrices
   */

  void onReceiveProjectiveLight(
    JCGLShadersType g_sh,
    R2MatricesProjectiveLightValuesType m);
}