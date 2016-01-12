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

import com.io7m.jcanephora.core.api.JCGLShadersType;

import java.util.Optional;

/**
 * Directional light shader for single lights.
 */

public final class R2DeferredLightShaderDirectionalSingle extends
  R2AbstractShader<R2DeferredLightShaderDirectionalParameters>
  implements R2ShaderScreenType<R2DeferredLightShaderDirectionalParameters>
{
  private R2DeferredLightShaderDirectionalSingle(
    final JCGLShadersType in_shaders,
    final R2ShaderSourcesType in_sources,
    final R2IDPoolType in_pool)
  {
    super(
      in_shaders,
      in_sources,
      in_pool,
      "R2DeferredLightShaderDirectionalSingle",
      "R2DeferredLightShaderDirectionalSingle.vert",
      Optional.empty(),
      "R2DeferredLightShaderDirectionalSingle.frag");
  }

  /**
   * Construct a new shader.
   *
   * @param in_shaders A shader interface
   * @param in_sources Shader sources
   * @param in_pool    The ID pool
   *
   * @return A new shader
   */

  public static R2ShaderScreenType<R2DeferredLightShaderDirectionalParameters>
  newShader(
    final JCGLShadersType in_shaders,
    final R2ShaderSourcesType in_sources,
    final R2IDPoolType in_pool)
  {
    return new R2DeferredLightShaderDirectionalSingle(
      in_shaders, in_sources, in_pool);
  }

  @Override
  public Class<R2DeferredLightShaderDirectionalParameters>
  getShaderParametersType()
  {
    return R2DeferredLightShaderDirectionalParameters.class;
  }
}
