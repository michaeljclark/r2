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

import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocator;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocatorType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizeL;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizesL;
import com.io7m.r2.core.R2GeometryBuffer;
import com.io7m.r2.core.R2GeometryBufferType;
import com.io7m.r2.core.R2IDPool;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2ProjectionReadableType;
import com.io7m.r2.core.shaders.types.R2ShaderFilterType;
import com.io7m.r2.core.shaders.types.R2ShaderPreprocessingEnvironmentType;
import com.io7m.r2.filters.R2SSAONoiseTexture;
import com.io7m.r2.filters.R2ShaderSSAO;
import com.io7m.r2.filters.R2ShaderSSAOParameters;
import com.io7m.r2.tests.core.R2EmptyObserverValues;
import com.io7m.r2.tests.core.ShaderPreprocessing;
import org.junit.Assert;
import org.junit.Test;

import static com.io7m.r2.core.R2GeometryBufferComponents.R2_GEOMETRY_BUFFER_FULL;
import static com.io7m.r2.core.R2GeometryBufferDescription.of;
import static com.io7m.r2.core.R2ProjectionOrthographic.create;
import static com.io7m.r2.filters.R2SSAOKernel.newKernel;
import static com.io7m.r2.filters.R2ShaderSSAOParameters.builder;

public abstract class R2ShaderSSAOContract extends
  R2ShaderFilterContract<R2ShaderSSAOParameters, R2ShaderSSAOParameters>
{
  @Test
  public final void testNew()
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g = c.contextGetGL33();

    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool =
      R2IDPool.newPool();

    final R2ShaderFilterType<R2ShaderSSAOParameters> s =
      R2ShaderSSAO.create(
        g.shaders(),
        sources,
        pool);

    Assert.assertFalse(s.isDeleted());
    s.delete(g);
    Assert.assertTrue(s.isDeleted());
  }

  @Override
  protected final R2ShaderSSAOParameters newParameters(
    final JCGLInterfaceGL33Type g)
  {
    final JCGLTexturesType g_tex = g.textures();
    final JCGLFramebuffersType g_fb = g.framebuffers();

    final JCGLTextureUnitAllocatorType tp =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        8,
        g_tex.textureGetUnits());
    final JCGLTextureUnitContextParentType tc_root =
      tp.rootContext();
    final JCGLTextureUnitContextType tc_alloc =
      tc_root.unitContextNew();

    try {
      final R2ProjectionReadableType proj = create();

      final AreaSizeL area = AreaSizeL.of(640L, 480L);

      final R2GeometryBufferType gb =
        R2GeometryBuffer.create(
          g.framebuffers(),
          g_tex,
          tc_alloc,
          of(area, R2_GEOMETRY_BUFFER_FULL));
      g_fb.framebufferDrawUnbind();

      return builder()
        .setGeometryBuffer(gb)
        .setKernel(newKernel(32))
        .setNoiseTexture(R2SSAONoiseTexture.create(g_tex, tc_alloc))
        .setViewport(AreaSizesL.area(area))
        .setViewMatrices(new R2EmptyObserverValues(proj))
        .build();
    } finally {
      tc_alloc.unitContextFinish(g_tex);
    }
  }

}
