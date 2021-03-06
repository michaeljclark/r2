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

package com.io7m.r2.tests.shaders.lights.api;

import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocator;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocatorType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jfsm.core.FSMTransitionException;
import com.io7m.jfunctional.Unit;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizeL;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizesL;
import com.io7m.jtensors.core.parameterized.matrices.PMatrices4x4D;
import com.io7m.jtensors.core.parameterized.matrices.PMatrix4x4D;
import com.io7m.r2.core.api.ids.R2IDPool;
import com.io7m.r2.core.api.ids.R2IDPoolType;
import com.io7m.r2.lights.R2LightProjectiveReadableType;
import com.io7m.r2.matrices.R2Matrices;
import com.io7m.r2.matrices.R2MatricesType;
import com.io7m.r2.projections.R2ProjectionOrthographic;
import com.io7m.r2.projections.R2ProjectionReadableType;
import com.io7m.r2.rendering.geometry.R2GeometryBuffer;
import com.io7m.r2.rendering.geometry.api.R2GeometryBufferComponents;
import com.io7m.r2.rendering.geometry.api.R2GeometryBufferDescription;
import com.io7m.r2.rendering.geometry.api.R2GeometryBufferType;
import com.io7m.r2.shaders.api.R2ShaderPreprocessingEnvironmentType;
import com.io7m.r2.shaders.light.api.R2LightShaderDefines;
import com.io7m.r2.shaders.light.api.R2ShaderLightProjectiveType;
import com.io7m.r2.shaders.light.api.R2ShaderLightVolumeSingleType;
import com.io7m.r2.shaders.light.api.R2ShaderParametersLight;
import com.io7m.r2.spaces.R2SpaceEyeType;
import com.io7m.r2.spaces.R2SpaceWorldType;
import com.io7m.r2.tests.R2JCGLContract;
import com.io7m.r2.tests.ShaderPreprocessing;
import com.io7m.r2.textures.R2TextureDefaults;
import com.io7m.r2.textures.R2TextureDefaultsType;
import com.io7m.r2.transforms.R2TransformT;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class R2ShaderLightProjectiveContract<
  T extends R2LightProjectiveReadableType>
  extends R2JCGLContract
{
  @Rule public ExpectedException expected = ExpectedException.none();

  private static R2GeometryBufferType newGeometryBuffer(
    final JCGLFramebuffersType g_fb,
    final JCGLTexturesType g_tex,
    final JCGLTextureUnitContextParentType tr)
  {
    final JCGLTextureUnitContextType tc =
      tr.unitContextNew();

    try {
      final R2GeometryBufferDescription gbuffer_desc =
        R2GeometryBufferDescription.of(
          AreaSizeL.of(4L, 4L),
          R2GeometryBufferComponents.R2_GEOMETRY_BUFFER_FULL);
      final R2GeometryBufferType gb = R2GeometryBuffer.create(
        g_fb, g_tex, tc, gbuffer_desc);
      g_fb.framebufferDrawUnbind();
      return gb;
    } finally {
      tc.unitContextFinish(g_tex);
    }
  }

  protected abstract R2ShaderLightProjectiveType<T> newShaderWithVerifier(
    JCGLInterfaceGL33Type g,
    R2ShaderPreprocessingEnvironmentType sources,
    R2IDPoolType pool);

  protected abstract T newLight(
    JCGLInterfaceGL33Type g,
    R2IDPoolType pool,
    JCGLTextureUnitContextType uc,
    R2TextureDefaultsType td);

  @Test
  public final void testCorrect()
    throws Exception
  {
    final JCGLContextType c =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool =
      R2IDPool.newPool();

    final JCGLFramebuffersType g_fb =
      g.framebuffers();
    final JCGLTexturesType g_tex =
      g.textures();
    final JCGLShadersType g_sh =
      g.shaders();

    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        32, g_tex.textureGetUnits());
    final JCGLTextureUnitContextParentType tr =
      ta.rootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.create(g_tex, tr);

    final R2GeometryBufferType gbuffer =
      newGeometryBuffer(g_fb, g_tex, tr);

    final JCGLTextureUnitContextType tc = tr.unitContextNew();
    final JCGLTextureUnitType ua =
      tc.unitContextBindTexture2D(
        g_tex,
        gbuffer.albedoEmissiveTexture().texture());
    final JCGLTextureUnitType un =
      tc.unitContextBindTexture2D(g_tex, gbuffer.normalTexture().texture());
    final JCGLTextureUnitType us =
      tc.unitContextBindTexture2D(
        g_tex,
        gbuffer.specularTextureOrDefault(td).texture());
    final JCGLTextureUnitType ud =
      tc.unitContextBindTexture2D(g_tex, gbuffer.depthTexture().texture());

    final R2ShaderLightProjectiveType<T> f =
      this.newShaderWithVerifier(g, sources, pool);
    final T params =
      this.newLight(g, pool, tc, td);

    final R2ProjectionReadableType proj =
      R2ProjectionOrthographic.create();
    final R2MatricesType mat =
      R2Matrices.create();
    final PMatrix4x4D<R2SpaceWorldType, R2SpaceEyeType> view =
      PMatrices4x4D.identity();
    final R2TransformT trans =
      R2TransformT.create();

    mat.withObserver(view, proj, this, (mo, x) -> {
      f.onActivate(g);
      f.onReceiveBoundGeometryBufferTextures(g, gbuffer, ua, us, ud, un);
      f.onReceiveValues(
        g,
        R2ShaderParametersLight.of(
          tc,
          params,
          mo,
          AreaSizesL.area(gbuffer.size())));

      return mo.withProjectiveLight(trans, proj, this, (mp, y) -> {
        f.onReceiveVolumeLightTransform(g, mp);
        f.onReceiveProjectiveLight(g, mp);
        f.onValidate();
        f.onDeactivate(g);
        return Unit.unit();
      });
    });
  }

  @Test
  public final void testMissedGeometryBuffer()
    throws Exception
  {
    final JCGLContextType c =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool =
      R2IDPool.newPool();

    final JCGLFramebuffersType g_fb =
      g.framebuffers();
    final JCGLTexturesType g_tex =
      g.textures();
    final JCGLShadersType g_sh =
      g.shaders();
    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        32,
        g_tex.textureGetUnits());
    final JCGLTextureUnitContextParentType tr =
      ta.rootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.create(g_tex, tr);

    final R2GeometryBufferType gbuffer =
      newGeometryBuffer(g_fb, g_tex, tr);

    final JCGLTextureUnitContextType tc = tr.unitContextNew();

    final R2ShaderLightVolumeSingleType<T> f =
      this.newShaderWithVerifier(g, sources, pool);
    final T params =
      this.newLight(g, pool, tc, td);

    final R2ProjectionReadableType proj =
      R2ProjectionOrthographic.create();
    final R2MatricesType mat =
      R2Matrices.create();
    final PMatrix4x4D<R2SpaceWorldType, R2SpaceEyeType> view =
      PMatrices4x4D.identity();

    f.onActivate(g);

    this.expected.expect(FSMTransitionException.class);
    mat.withObserver(view, proj, this, (mo, x) -> {
      f.onReceiveValues(
        g,
        R2ShaderParametersLight.of(
          tc,
          params,
          mo,
          AreaSizesL.area(gbuffer.size())));
      return Unit.unit();
    });
  }

  @Test
  public final void testNewDefault()
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g = c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool =
      R2IDPool.newPool();

    final JCGLTexturesType g_tex =
      g.textures();
    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        32,
        g_tex.textureGetUnits());
    final JCGLTextureUnitContextParentType tr =
      ta.rootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.create(g_tex, tr);
    final JCGLTextureUnitContextType tc =
      tr.unitContextNew();

    final R2ShaderLightProjectiveType<T> s =
      this.newShaderWithVerifier(g, sources, pool);

    final T light =
      this.newLight(g, pool, tc, td);

    final Class<?> s_class = s.shaderParametersType();
    final Class<?> l_class = light.getClass();
    Assert.assertTrue(s_class.isAssignableFrom(l_class));
    Assert.assertTrue(light.lightID() >= 0L);

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

    final R2IDPoolType pool =
      R2IDPool.newPool();

    final JCGLTexturesType g_tex =
      g.textures();
    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        32,
        g_tex.textureGetUnits());
    final JCGLTextureUnitContextParentType tr =
      ta.rootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.create(g_tex, tr);
    final JCGLTextureUnitContextType tc =
      tr.unitContextNew();

    final R2ShaderLightProjectiveType<T> s =
      this.newShaderWithVerifier(g, sources, pool);

    final T light =
      this.newLight(g, pool, tc, td);

    final Class<?> s_class = s.shaderParametersType();
    final Class<?> l_class = light.getClass();
    Assert.assertTrue(s_class.isAssignableFrom(l_class));
    Assert.assertTrue(light.lightID() >= 0L);

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

    final R2IDPoolType pool =
      R2IDPool.newPool();

    final JCGLTexturesType g_tex =
      g.textures();
    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        32,
        g_tex.textureGetUnits());
    final JCGLTextureUnitContextParentType tr =
      ta.rootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.create(g_tex, tr);
    final JCGLTextureUnitContextType tc =
      tr.unitContextNew();

    final R2ShaderLightProjectiveType<T> s =
      this.newShaderWithVerifier(g, sources, pool);

    final T light =
      this.newLight(g, pool, tc, td);

    final Class<?> s_class = s.shaderParametersType();
    final Class<?> l_class = light.getClass();
    Assert.assertTrue(s_class.isAssignableFrom(l_class));
    Assert.assertTrue(light.lightID() >= 0L);

    Assert.assertFalse(s.isDeleted());
    s.delete(g);
    Assert.assertTrue(s.isDeleted());
  }
}
