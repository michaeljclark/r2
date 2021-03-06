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

package com.io7m.r2.tests.shaders.lights;

import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.r2.core.api.ids.R2IDPool;
import com.io7m.r2.core.api.ids.R2IDPoolType;
import com.io7m.r2.lights.R2LightDirectionalScreenSingle;
import com.io7m.r2.shaders.api.R2ShaderPreprocessingEnvironmentType;
import com.io7m.r2.shaders.debug.R2DebugShaderLightDirectionalConstantSingle;
import com.io7m.r2.shaders.light.api.R2LightShaderDefines;
import com.io7m.r2.shaders.light.api.R2ShaderLightSingleType;
import com.io7m.r2.tests.R2JCGLContract;
import com.io7m.r2.tests.ShaderPreprocessing;
import org.junit.Assert;
import org.junit.Test;

public abstract class R2ShaderLightDirectionalDebugConstantSingleContract
  extends R2JCGLContract
{
  @Test
  public final void testNewDefault()
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g = c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool = R2IDPool.newPool();

    final R2ShaderLightSingleType<R2LightDirectionalScreenSingle> s =
      R2DebugShaderLightDirectionalConstantSingle.newShader(
        g.shaders(),
        sources,
        pool);

    Assert.assertFalse(s.isDeleted());
    s.delete(g);
    Assert.assertTrue(s.isDeleted());
  }

  @Test
  public final void testNewLightBuffer()
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g = c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    sources.preprocessorDefineSet(
      R2LightShaderDefines.R2_LIGHT_SHADER_OUTPUT_TARGET_DEFINE,
      R2LightShaderDefines.R2_LIGHT_SHADER_OUTPUT_TARGET_LBUFFER);

    final R2IDPoolType pool = R2IDPool.newPool();
    final R2ShaderLightSingleType<R2LightDirectionalScreenSingle> s =
      R2DebugShaderLightDirectionalConstantSingle.newShader(
        g.shaders(),
        sources,
        pool);

    Assert.assertFalse(s.isDeleted());
    s.delete(g);
    Assert.assertTrue(s.isDeleted());
  }

  @Test
  public final void testNewImageBuffer()
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g = c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    sources.preprocessorDefineSet(
      R2LightShaderDefines.R2_LIGHT_SHADER_OUTPUT_TARGET_DEFINE,
      R2LightShaderDefines.R2_LIGHT_SHADER_OUTPUT_TARGET_IBUFFER);

    final R2IDPoolType pool = R2IDPool.newPool();
    final R2ShaderLightSingleType<R2LightDirectionalScreenSingle> s =
      R2DebugShaderLightDirectionalConstantSingle.newShader(
        g.shaders(),
        sources,
        pool);

    Assert.assertFalse(s.isDeleted());
    s.delete(g);
    Assert.assertTrue(s.isDeleted());
  }
}
