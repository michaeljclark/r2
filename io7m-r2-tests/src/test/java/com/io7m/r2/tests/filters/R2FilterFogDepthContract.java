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

package com.io7m.r2.tests.filters;

import com.io7m.jareas.core.AreaInclusiveUnsignedL;
import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.jcanephora.core.JCGLProjectionMatrices;
import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.profiler.JCGLProfiling;
import com.io7m.jcanephora.profiler.JCGLProfilingContextType;
import com.io7m.jcanephora.profiler.JCGLProfilingFrameType;
import com.io7m.jcanephora.profiler.JCGLProfilingType;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitAllocator;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitAllocatorType;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.junsigned.ranges.UnsignedRangeInclusiveL;
import com.io7m.r2.core.R2FilterType;
import com.io7m.r2.core.R2IDPool;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2ProjectionFOV;
import com.io7m.r2.core.R2TextureDefaults;
import com.io7m.r2.core.R2TextureDefaultsType;
import com.io7m.r2.core.R2UnitQuad;
import com.io7m.r2.core.R2UnitQuadType;
import com.io7m.r2.core.shaders.types.R2ShaderPreprocessingEnvironmentType;
import com.io7m.r2.filters.R2FilterFogDepth;
import com.io7m.r2.filters.R2FilterFogParameters;
import com.io7m.r2.filters.R2FilterFogProgression;
import com.io7m.r2.tests.core.R2EmptyObserverValues;
import com.io7m.r2.tests.core.R2JCGLContract;
import com.io7m.r2.tests.core.ShaderPreprocessing;
import org.junit.Assert;
import org.junit.Test;

public abstract class R2FilterFogDepthContract extends R2JCGLContract
{
  @Test
  public final void testIdentities()
  {
    final JCGLContextType gc =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      gc.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType id =
      R2IDPool.newPool();
    final JCGLTexturesType g_t =
      g.getTextures();
    final R2UnitQuadType quad =
      R2UnitQuad.newUnitQuad(g);

    final R2FilterType<R2FilterFogParameters> f =
      R2FilterFogDepth.newFilter(g, sources, id, quad);

    Assert.assertFalse(f.isDeleted());
    f.delete(g);
    Assert.assertTrue(f.isDeleted());
  }

  @Test
  public final void testRunLinear()
  {
    final JCGLContextType gc =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      gc.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType id =
      R2IDPool.newPool();
    final JCGLFramebuffersType g_fb =
      g.getFramebuffers();
    final JCGLTexturesType g_t =
      g.getTextures();
    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        8, g_t.textureGetUnits());
    final JCGLTextureUnitContextParentType tc =
      ta.getRootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.newDefaults(g.getTextures(), tc);
    final R2UnitQuadType quad =
      R2UnitQuad.newUnitQuad(g);
    final JCGLProfilingType pro =
      JCGLProfiling.newProfiling(g.getTimers());
    final JCGLProfilingFrameType pro_frame =
      pro.startFrame();
    final JCGLProfilingContextType pro_root =
      pro_frame.getChildContext("main");

    final R2FilterType<R2FilterFogParameters> f =
      R2FilterFogDepth.newFilter(g, sources, id, quad);

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertFalse(g_fb.framebufferDrawAnyIsBound());

    final AreaInclusiveUnsignedLType area = AreaInclusiveUnsignedL.of(
      new UnsignedRangeInclusiveL(0L, 127L),
      new UnsignedRangeInclusiveL(0L, 127L));

    final R2FilterFogParameters params =
      R2FilterFogParameters.builder()
        .setProgression(R2FilterFogProgression.FOG_LINEAR)
        .setFogNearPositiveZ(0.0f)
        .setFogFarPositiveZ(1.0f)
        .setFogColor(new PVectorI3F<>(1.0f, 0.0f, 0.0f))
        .setObserverValues(new R2EmptyObserverValues(
          R2ProjectionFOV.newFrustumWith(
            JCGLProjectionMatrices.newMatrices(),
            (float) Math.toRadians(90.0f), 1.0f, 0.0f, 100.0f)))
        .setImageTexture(td.texture2DWhite())
        .setImageDepthTexture(td.texture2DWhite())
        .setViewport(area)
        .build();

    f.runFilter(pro_root, tc, params);

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertFalse(g_fb.framebufferDrawAnyIsBound());
  }

  @Test
  public final void testRunQuadratic()
  {
    final JCGLContextType gc =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      gc.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType id =
      R2IDPool.newPool();
    final JCGLFramebuffersType g_fb =
      g.getFramebuffers();
    final JCGLTexturesType g_t =
      g.getTextures();
    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        8, g_t.textureGetUnits());
    final JCGLTextureUnitContextParentType tc =
      ta.getRootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.newDefaults(g.getTextures(), tc);
    final R2UnitQuadType quad =
      R2UnitQuad.newUnitQuad(g);
    final JCGLProfilingType pro =
      JCGLProfiling.newProfiling(g.getTimers());
    final JCGLProfilingFrameType pro_frame =
      pro.startFrame();
    final JCGLProfilingContextType pro_root =
      pro_frame.getChildContext("main");

    final R2FilterType<R2FilterFogParameters> f =
      R2FilterFogDepth.newFilter(g, sources, id, quad);

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertFalse(g_fb.framebufferDrawAnyIsBound());

    final AreaInclusiveUnsignedLType area = AreaInclusiveUnsignedL.of(
      new UnsignedRangeInclusiveL(0L, 127L),
      new UnsignedRangeInclusiveL(0L, 127L));

    final R2FilterFogParameters params = R2FilterFogParameters.builder()
      .setProgression(R2FilterFogProgression.FOG_QUADRATIC)
      .setFogNearPositiveZ(0.0f)
      .setFogFarPositiveZ(1.0f)
      .setFogColor(new PVectorI3F<>(1.0f, 0.0f, 0.0f))
      .setObserverValues(new R2EmptyObserverValues(
        R2ProjectionFOV.newFrustumWith(
          JCGLProjectionMatrices.newMatrices(),
          (float) Math.toRadians(90.0f), 1.0f, 0.0f, 100.0f)))
      .setImageTexture(td.texture2DWhite())
      .setImageDepthTexture(td.texture2DWhite())
      .setViewport(area)
      .build();

    f.runFilter(pro_root, tc, params);

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertFalse(g_fb.framebufferDrawAnyIsBound());
  }
}