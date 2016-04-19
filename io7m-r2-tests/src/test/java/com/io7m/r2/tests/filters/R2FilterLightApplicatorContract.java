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
import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.junsigned.ranges.UnsignedRangeInclusiveL;
import com.io7m.r2.core.R2CopyDepth;
import com.io7m.r2.core.R2DepthPrecision;
import com.io7m.r2.core.R2FilterType;
import com.io7m.r2.core.R2GeometryBuffer;
import com.io7m.r2.core.R2GeometryBufferComponents;
import com.io7m.r2.core.R2GeometryBufferDescription;
import com.io7m.r2.core.R2GeometryBufferDescriptionType;
import com.io7m.r2.core.R2GeometryBufferType;
import com.io7m.r2.core.R2IDPool;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2ImageBuffer;
import com.io7m.r2.core.R2ImageBufferDescription;
import com.io7m.r2.core.R2ImageBufferDescriptionType;
import com.io7m.r2.core.R2ImageBufferPool;
import com.io7m.r2.core.R2ImageBufferType;
import com.io7m.r2.core.R2ImageBufferUsableType;
import com.io7m.r2.core.R2RenderTargetPoolType;
import com.io7m.r2.core.R2TextureDefaults;
import com.io7m.r2.core.R2TextureDefaultsType;
import com.io7m.r2.core.R2TextureUnitAllocator;
import com.io7m.r2.core.R2TextureUnitAllocatorType;
import com.io7m.r2.core.R2TextureUnitContextParentType;
import com.io7m.r2.core.R2UnitQuad;
import com.io7m.r2.core.R2UnitQuadType;
import com.io7m.r2.core.profiling.R2Profiling;
import com.io7m.r2.core.profiling.R2ProfilingContextType;
import com.io7m.r2.core.profiling.R2ProfilingFrameType;
import com.io7m.r2.core.profiling.R2ProfilingType;
import com.io7m.r2.core.shaders.types.R2ShaderSourcesResources;
import com.io7m.r2.core.shaders.types.R2ShaderSourcesType;
import com.io7m.r2.filters.R2FilterLightApplicator;
import com.io7m.r2.filters.R2FilterLightApplicatorParametersMutable;
import com.io7m.r2.filters.R2FilterLightApplicatorParametersType;
import com.io7m.r2.shaders.R2Shaders;
import com.io7m.r2.tests.core.R2JCGLContract;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public abstract class R2FilterLightApplicatorContract extends R2JCGLContract
{
  @Test
  public final void testIdentities()
  {
    final JCGLContextType gc =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      gc.contextGetGL33();
    final R2ShaderSourcesType ss =
      R2ShaderSourcesResources.newSources(R2Shaders.class);
    final R2IDPoolType id =
      R2IDPool.newPool();
    final R2UnitQuadType quad =
      R2UnitQuad.newUnitQuad(g);
    final R2RenderTargetPoolType<
      R2ImageBufferDescriptionType, R2ImageBufferUsableType> rtp =
      R2ImageBufferPool.newPool(g, Long.MAX_VALUE, Long.MAX_VALUE);

    final R2FilterType<R2FilterLightApplicatorParametersType> f =
      R2FilterLightApplicator.newFilter(ss, g, id, quad);

    Assert.assertFalse(f.isDeleted());
    Assert.assertFalse(f.isDeleted());
    f.delete(g);
    Assert.assertTrue(f.isDeleted());
  }

  @Test
  public final void testFramebufferBindingCopyDepthNoDepth()
  {
    final JCGLContextType gc =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      gc.contextGetGL33();
    final R2ShaderSourcesType ss =
      R2ShaderSourcesResources.newSources(R2Shaders.class);
    final R2IDPoolType id =
      R2IDPool.newPool();
    final JCGLFramebuffersType g_fb =
      g.getFramebuffers();
    final JCGLTexturesType g_t =
      g.getTextures();
    final R2TextureUnitAllocatorType ta =
      R2TextureUnitAllocator.newAllocatorWithStack(
        8, g_t.textureGetUnits());
    final R2TextureUnitContextParentType tc =
      ta.getRootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.newDefaults(g.getTextures(), tc);
    final R2UnitQuadType quad =
      R2UnitQuad.newUnitQuad(g);
    final R2ProfilingType pro =
      R2Profiling.newProfiling(g.getTimers());
    final R2ProfilingFrameType pro_frame =
      pro.startFrame();
    final R2ProfilingContextType pro_root =
      pro_frame.getChildContext("main");

    final AreaInclusiveUnsignedLType area = AreaInclusiveUnsignedL.of(
      new UnsignedRangeInclusiveL(0L, 127L),
      new UnsignedRangeInclusiveL(0L, 127L));

    final R2ImageBufferType ib =
      R2ImageBuffer.newImageBuffer(
        g_fb, g_t, tc, R2ImageBufferDescription.of(area, Optional.empty()));

    final R2GeometryBufferDescriptionType desc =
      R2GeometryBufferDescription.of(
        area, R2GeometryBufferComponents.R2_GEOMETRY_BUFFER_FULL);
    final R2GeometryBufferType gbuffer =
      R2GeometryBuffer.newGeometryBuffer(g_fb, g_t, tc, desc);

    final R2FilterType<R2FilterLightApplicatorParametersType> f =
      R2FilterLightApplicator.newFilter(ss, g, id, quad);

    g_fb.framebufferDrawUnbind();
    g_fb.framebufferReadUnbind();

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertFalse(g_fb.framebufferDrawAnyIsBound());

    g_fb.framebufferDrawBind(ib.getPrimaryFramebuffer());

    final R2FilterLightApplicatorParametersMutable params =
      R2FilterLightApplicatorParametersMutable.create();
    params.setLightDiffuseTexture(td.getBlackTexture());
    params.setLightSpecularTexture(td.getBlackTexture());
    params.setGeometryBuffer(gbuffer);
    params.setOutputViewport(area);
    params.setCopyDepth(R2CopyDepth.R2_COPY_DEPTH_ENABLED);

    f.runFilter(pro_root, tc, params);

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertTrue(g_fb.framebufferDrawIsBound(ib.getPrimaryFramebuffer()));
  }

  @Test
  public final void testFramebufferBindingNoCopyDepthNoDepth()
  {
    final JCGLContextType gc =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      gc.contextGetGL33();
    final R2ShaderSourcesType ss =
      R2ShaderSourcesResources.newSources(R2Shaders.class);
    final R2IDPoolType id =
      R2IDPool.newPool();
    final JCGLFramebuffersType g_fb =
      g.getFramebuffers();
    final JCGLTexturesType g_t =
      g.getTextures();
    final R2TextureUnitAllocatorType ta =
      R2TextureUnitAllocator.newAllocatorWithStack(
        8, g_t.textureGetUnits());
    final R2TextureUnitContextParentType tc =
      ta.getRootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.newDefaults(g.getTextures(), tc);
    final R2UnitQuadType quad =
      R2UnitQuad.newUnitQuad(g);
    final R2ProfilingType pro =
      R2Profiling.newProfiling(g.getTimers());
    final R2ProfilingFrameType pro_frame =
      pro.startFrame();
    final R2ProfilingContextType pro_root =
      pro_frame.getChildContext("main");

    final AreaInclusiveUnsignedLType area = AreaInclusiveUnsignedL.of(
      new UnsignedRangeInclusiveL(0L, 127L),
      new UnsignedRangeInclusiveL(0L, 127L));

    final R2ImageBufferType ib =
      R2ImageBuffer.newImageBuffer(
        g_fb, g_t, tc, R2ImageBufferDescription.of(area, Optional.empty()));

    final R2GeometryBufferDescriptionType desc =
      R2GeometryBufferDescription.of(
        area, R2GeometryBufferComponents.R2_GEOMETRY_BUFFER_FULL);
    final R2GeometryBufferType gbuffer =
      R2GeometryBuffer.newGeometryBuffer(g_fb, g_t, tc, desc);

    final R2FilterType<R2FilterLightApplicatorParametersType> f =
      R2FilterLightApplicator.newFilter(ss, g, id, quad);

    g_fb.framebufferDrawUnbind();
    g_fb.framebufferReadUnbind();

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertFalse(g_fb.framebufferDrawAnyIsBound());

    g_fb.framebufferDrawBind(ib.getPrimaryFramebuffer());

    final R2FilterLightApplicatorParametersMutable params =
      R2FilterLightApplicatorParametersMutable.create();
    params.setLightDiffuseTexture(td.getBlackTexture());
    params.setLightSpecularTexture(td.getBlackTexture());
    params.setGeometryBuffer(gbuffer);
    params.setOutputViewport(area);
    params.setCopyDepth(R2CopyDepth.R2_COPY_DEPTH_DISABLED);

    f.runFilter(pro_root, tc, params);

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertTrue(g_fb.framebufferDrawIsBound(ib.getPrimaryFramebuffer()));
  }


  @Test
  public final void testFramebufferBindingCopyDepthWithDepth()
  {
    final JCGLContextType gc =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      gc.contextGetGL33();
    final R2ShaderSourcesType ss =
      R2ShaderSourcesResources.newSources(R2Shaders.class);
    final R2IDPoolType id =
      R2IDPool.newPool();
    final JCGLFramebuffersType g_fb =
      g.getFramebuffers();
    final JCGLTexturesType g_t =
      g.getTextures();
    final R2TextureUnitAllocatorType ta =
      R2TextureUnitAllocator.newAllocatorWithStack(
        8, g_t.textureGetUnits());
    final R2TextureUnitContextParentType tc =
      ta.getRootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.newDefaults(g.getTextures(), tc);
    final R2UnitQuadType quad =
      R2UnitQuad.newUnitQuad(g);
    final R2ProfilingType pro =
      R2Profiling.newProfiling(g.getTimers());
    final R2ProfilingFrameType pro_frame =
      pro.startFrame();
    final R2ProfilingContextType pro_root =
      pro_frame.getChildContext("main");

    final AreaInclusiveUnsignedLType area = AreaInclusiveUnsignedL.of(
      new UnsignedRangeInclusiveL(0L, 127L),
      new UnsignedRangeInclusiveL(0L, 127L));

    final R2ImageBufferType ib =
      R2ImageBuffer.newImageBuffer(
        g_fb, g_t, tc,
        R2ImageBufferDescription.of(area, Optional.of(
          R2DepthPrecision.R2_DEPTH_PRECISION_24)));

    final R2GeometryBufferDescriptionType desc =
      R2GeometryBufferDescription.of(
        area, R2GeometryBufferComponents.R2_GEOMETRY_BUFFER_FULL);
    final R2GeometryBufferType gbuffer =
      R2GeometryBuffer.newGeometryBuffer(g_fb, g_t, tc, desc);

    final R2FilterType<R2FilterLightApplicatorParametersType> f =
      R2FilterLightApplicator.newFilter(ss, g, id, quad);

    g_fb.framebufferDrawUnbind();
    g_fb.framebufferReadUnbind();

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertFalse(g_fb.framebufferDrawAnyIsBound());

    g_fb.framebufferDrawBind(ib.getPrimaryFramebuffer());

    final R2FilterLightApplicatorParametersMutable params =
      R2FilterLightApplicatorParametersMutable.create();
    params.setLightDiffuseTexture(td.getBlackTexture());
    params.setLightSpecularTexture(td.getBlackTexture());
    params.setGeometryBuffer(gbuffer);
    params.setOutputViewport(area);
    params.setCopyDepth(R2CopyDepth.R2_COPY_DEPTH_ENABLED);

    f.runFilter(pro_root, tc, params);

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertTrue(g_fb.framebufferDrawIsBound(ib.getPrimaryFramebuffer()));
  }

  @Test
  public final void testFramebufferBindingNoCopyDepthWithDepth()
  {
    final JCGLContextType gc =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      gc.contextGetGL33();
    final R2ShaderSourcesType ss =
      R2ShaderSourcesResources.newSources(R2Shaders.class);
    final R2IDPoolType id =
      R2IDPool.newPool();
    final JCGLFramebuffersType g_fb =
      g.getFramebuffers();
    final JCGLTexturesType g_t =
      g.getTextures();
    final R2TextureUnitAllocatorType ta =
      R2TextureUnitAllocator.newAllocatorWithStack(
        8, g_t.textureGetUnits());
    final R2TextureUnitContextParentType tc =
      ta.getRootContext();
    final R2TextureDefaultsType td =
      R2TextureDefaults.newDefaults(g.getTextures(), tc);
    final R2UnitQuadType quad =
      R2UnitQuad.newUnitQuad(g);
    final R2ProfilingType pro =
      R2Profiling.newProfiling(g.getTimers());
    final R2ProfilingFrameType pro_frame =
      pro.startFrame();
    final R2ProfilingContextType pro_root =
      pro_frame.getChildContext("main");

    final AreaInclusiveUnsignedLType area = AreaInclusiveUnsignedL.of(
      new UnsignedRangeInclusiveL(0L, 127L),
      new UnsignedRangeInclusiveL(0L, 127L));

    final R2ImageBufferType ib =
      R2ImageBuffer.newImageBuffer(
        g_fb, g_t, tc,
        R2ImageBufferDescription.of(area, Optional.of(
          R2DepthPrecision.R2_DEPTH_PRECISION_24)));

    final R2GeometryBufferDescriptionType desc =
      R2GeometryBufferDescription.of(
        area, R2GeometryBufferComponents.R2_GEOMETRY_BUFFER_FULL);
    final R2GeometryBufferType gbuffer =
      R2GeometryBuffer.newGeometryBuffer(g_fb, g_t, tc, desc);

    final R2FilterType<R2FilterLightApplicatorParametersType> f =
      R2FilterLightApplicator.newFilter(ss, g, id, quad);

    g_fb.framebufferDrawUnbind();
    g_fb.framebufferReadUnbind();

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertFalse(g_fb.framebufferDrawAnyIsBound());

    g_fb.framebufferDrawBind(ib.getPrimaryFramebuffer());

    final R2FilterLightApplicatorParametersMutable params =
      R2FilterLightApplicatorParametersMutable.create();
    params.setLightDiffuseTexture(td.getBlackTexture());
    params.setLightSpecularTexture(td.getBlackTexture());
    params.setGeometryBuffer(gbuffer);
    params.setOutputViewport(area);
    params.setCopyDepth(R2CopyDepth.R2_COPY_DEPTH_DISABLED);

    f.runFilter(pro_root, tc, params);

    Assert.assertFalse(g_fb.framebufferReadAnyIsBound());
    Assert.assertTrue(g_fb.framebufferDrawIsBound(ib.getPrimaryFramebuffer()));
  }
}
