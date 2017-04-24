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

package com.io7m.r2.tests.core;

import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocatorType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizeL;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizesL;
import com.io7m.jtensors.core.parameterized.matrices.PMatrices3x3D;
import com.io7m.jtensors.core.parameterized.matrices.PMatrices4x4D;
import com.io7m.r2.core.R2DepthInstances;
import com.io7m.r2.core.R2DepthInstancesType;
import com.io7m.r2.core.R2DepthVarianceBuffer;
import com.io7m.r2.core.R2DepthVarianceBufferDescription;
import com.io7m.r2.core.R2DepthVarianceBufferType;
import com.io7m.r2.core.R2DepthVarianceRendererType;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2InstanceSingle;
import com.io7m.r2.core.R2InstanceSingleType;
import com.io7m.r2.core.R2MaterialDepthSingle;
import com.io7m.r2.core.R2MaterialDepthSingleType;
import com.io7m.r2.core.R2Matrices;
import com.io7m.r2.core.R2MatricesType;
import com.io7m.r2.core.R2ProjectionOrthographic;
import com.io7m.r2.core.R2TextureDefaultsType;
import com.io7m.r2.core.R2UnitQuadType;
import com.io7m.r2.core.shaders.provided.R2DepthShaderBasicParameters;
import com.io7m.r2.core.shaders.provided.R2DepthShaderBasicSingle;
import com.io7m.r2.core.shaders.types.R2ShaderDepthSingleType;
import com.io7m.r2.core.shaders.types.R2ShaderPreprocessingEnvironmentType;
import org.junit.Test;

import static com.io7m.jcanephora.core.JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR;
import static com.io7m.jcanephora.core.JCGLTextureFilterMinification.TEXTURE_FILTER_LINEAR_MIPMAP_LINEAR;
import static com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocator.newAllocatorWithStack;
import static com.io7m.jfunctional.Unit.unit;
import static com.io7m.r2.core.R2DepthPrecision.R2_DEPTH_PRECISION_24;
import static com.io7m.r2.core.R2DepthVarianceBufferDescription.of;
import static com.io7m.r2.core.R2DepthVariancePrecision.R2_DEPTH_VARIANCE_PRECISION_16;
import static com.io7m.r2.core.R2IDPool.newPool;
import static com.io7m.r2.core.R2TextureDefaults.create;
import static com.io7m.r2.core.R2TransformIdentity.get;
import static com.io7m.r2.core.R2UnitQuad.newUnitQuad;
import static com.io7m.r2.core.shaders.provided.R2DepthShaderBasicParameters.builder;
import static com.io7m.r2.tests.core.ShaderPreprocessing.preprocessor;

public abstract class R2DepthVarianceRendererContract extends R2JCGLContract
{
  protected abstract R2DepthVarianceRendererType getRenderer(
    final JCGLInterfaceGL33Type g);

  /**
   * Just check that execution proceeds without errors.
   */

  @Test
  public final void testTrivial()
  {
    final JCGLContextType c =
      this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g =
      c.contextGetGL33();

    final R2DepthVarianceRendererType r =
      this.getRenderer(g);

    final AreaSizeL area = AreaSizeL.of(640L, 480L);

    final JCGLTextureUnitAllocatorType ta =
      newAllocatorWithStack(
        8, g.textures().textureGetUnits());
    final JCGLTextureUnitContextParentType tc =
      ta.rootContext();

    final R2TextureDefaultsType td =
      create(g.textures(), tc);

    final R2DepthVarianceBufferDescription dbd =
      of(
        area,
        TEXTURE_FILTER_LINEAR,
        TEXTURE_FILTER_LINEAR_MIPMAP_LINEAR,
        R2_DEPTH_PRECISION_24,
        R2_DEPTH_VARIANCE_PRECISION_16);

    final R2DepthVarianceBufferType db =
      R2DepthVarianceBuffer.create(
        g.framebuffers(),
        g.textures(),
        tc,
        dbd);

    final R2UnitQuadType quad =
      newUnitQuad(g);
    final R2IDPoolType id_pool =
      newPool();

    final R2InstanceSingleType i =
      R2InstanceSingle.of(
        id_pool.freshID(),
        quad.arrayObject(),
        get(),
        PMatrices3x3D.identity());

    final R2ShaderPreprocessingEnvironmentType sources =
      preprocessor();

    final R2ShaderDepthSingleType<R2DepthShaderBasicParameters> ds =
      R2DepthShaderBasicSingle.create(g.shaders(), sources, id_pool);

    final R2DepthShaderBasicParameters ds_param =
      builder().setTextureDefaults(td).build();

    final R2MaterialDepthSingleType<R2DepthShaderBasicParameters> mat =
      R2MaterialDepthSingle.of(id_pool.freshID(), ds, ds_param);

    final R2ProjectionOrthographic proj =
      R2ProjectionOrthographic.create();

    final R2DepthInstancesType di =
      R2DepthInstances.create();
    di.depthsAddSingleInstance(i, mat);

    final R2MatricesType m = R2Matrices.create();
    m.withObserver(
      PMatrices4x4D.identity(),
      proj,
      unit(),
      (x, y) -> {
        r.renderDepthVarianceWithBoundBuffer(AreaSizesL.area(area), tc, x, di);
        return unit();
      });
  }
}