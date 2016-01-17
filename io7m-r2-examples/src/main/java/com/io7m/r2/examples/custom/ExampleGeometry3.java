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

package com.io7m.r2.examples.custom;

import com.io7m.jareas.core.AreaInclusiveUnsignedIType;
import com.io7m.jcanephora.core.JCGLClearSpecification;
import com.io7m.jcanephora.core.JCGLFaceSelection;
import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.api.JCGLClearType;
import com.io7m.jcanephora.core.api.JCGLColorBufferMaskingType;
import com.io7m.jcanephora.core.api.JCGLDepthBuffersType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLStencilBuffersType;
import com.io7m.jfunctional.Unit;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.parameterized.PMatrix4x4FType;
import com.io7m.jtensors.parameterized.PMatrixHeapArrayM4x4F;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r2.core.R2DeferredSurfaceShaderBasicParameters;
import com.io7m.r2.core.R2DeferredSurfaceShaderBasicSingle;
import com.io7m.r2.core.R2GeometryBuffer;
import com.io7m.r2.core.R2GeometryBufferType;
import com.io7m.r2.core.R2GeometryRendererType;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2InstanceSingle;
import com.io7m.r2.core.R2InstanceSingleType;
import com.io7m.r2.core.R2MaterialOpaqueSingle;
import com.io7m.r2.core.R2MaterialOpaqueSingleType;
import com.io7m.r2.core.R2MatricesType;
import com.io7m.r2.core.R2ProjectionFOV;
import com.io7m.r2.core.R2SceneOpaques;
import com.io7m.r2.core.R2SceneOpaquesType;
import com.io7m.r2.core.R2SceneStencils;
import com.io7m.r2.core.R2SceneStencilsMode;
import com.io7m.r2.core.R2SceneStencilsType;
import com.io7m.r2.core.R2ShaderInstanceSingleType;
import com.io7m.r2.core.R2ShaderSourcesResources;
import com.io7m.r2.core.R2ShaderSourcesType;
import com.io7m.r2.core.R2StencilRendererType;
import com.io7m.r2.core.R2TransformOSiT;
import com.io7m.r2.core.R2UnitQuad;
import com.io7m.r2.core.R2UnitQuadType;
import com.io7m.r2.examples.R2ExampleCustomType;
import com.io7m.r2.examples.R2ExampleServicesType;
import com.io7m.r2.main.R2MainType;
import com.io7m.r2.shaders.R2Shaders;
import com.io7m.r2.spaces.R2SpaceEyeType;
import com.io7m.r2.spaces.R2SpaceWorldType;

// CHECKSTYLE_JAVADOC:OFF

public final class ExampleGeometry3 implements R2ExampleCustomType
{
  private final PMatrix4x4FType<R2SpaceWorldType, R2SpaceEyeType> view;

  private R2SceneStencilsType    stencils;
  private R2StencilRendererType  stencil_renderer;
  private R2GeometryRendererType geom_renderer;
  private R2MatricesType         matrices;
  private R2ProjectionFOV        projection;
  private R2UnitQuadType         quad;
  private R2SceneOpaquesType     opaques;
  private R2GeometryBufferType   gbuffer;
  private JCGLClearSpecification clear_spec;

  private R2ShaderInstanceSingleType<R2DeferredSurfaceShaderBasicParameters>
                                 shader;
  private R2DeferredSurfaceShaderBasicParameters
                                 shader_params;
  private R2MaterialOpaqueSingleType<R2DeferredSurfaceShaderBasicParameters>
                                 material;
  private R2InstanceSingleType[] instances;

  public ExampleGeometry3()
  {
    this.view = PMatrixHeapArrayM4x4F.newMatrix();
  }

  @Override
  public void onInitialize(
    final R2ExampleServicesType serv,
    final JCGLInterfaceGL33Type g,
    final AreaInclusiveUnsignedIType area,
    final R2MainType m)
  {
    this.opaques = R2SceneOpaques.newOpaques();
    this.stencils = R2SceneStencils.newMasks();
    this.stencil_renderer = m.getStencilRenderer();
    this.geom_renderer = m.getGeometryRenderer();
    this.matrices = m.getMatrices();
    this.quad = R2UnitQuad.newUnitQuad(g);
    this.gbuffer = R2GeometryBuffer.newGeometryBuffer(g, area);

    this.projection = R2ProjectionFOV.newFrustumWith(
      m.getProjectionMatrices(),
      (float) Math.toRadians(90.0f), 640.0f / 480.0f, 0.01f, 1000.0f);

    m.getViewMatrices().lookAt(
      this.view,
      new VectorI3F(0.0f, 0.0f, 5.0f),
      new VectorI3F(0.0f, 0.0f, 0.0f),
      new VectorI3F(0.0f, 1.0f, 0.0f));

    final R2IDPoolType id_pool = m.getIDPool();

    this.instances = new R2InstanceSingleType[8];
    for (int index = 0; index < this.instances.length; ++index) {
      final R2TransformOSiT tr = R2TransformOSiT.newTransform();

      tr.getScale().set3F(0.25f, 0.25f, 0.25f);
      tr.getTranslation().set3F(
        index - (this.instances.length / 2), 0.0f, 0.0f);

      this.instances[index] = R2InstanceSingle.newInstance(
        id_pool,
        this.quad.getArrayObject(),
        tr,
        PMatrixI3x3F.identity());
    }

    final R2ShaderSourcesType sources =
      R2ShaderSourcesResources.newSources(R2Shaders.class);
    this.shader =
      R2DeferredSurfaceShaderBasicSingle.newShader(
        g.getShaders(),
        sources,
        id_pool);
    this.shader_params =
      R2DeferredSurfaceShaderBasicParameters.newParameters(m.getTextureDefaults());

    this.material = R2MaterialOpaqueSingle.newMaterial(
      id_pool,
      this.shader,
      this.shader_params);

    final JCGLClearSpecification.Builder csb = JCGLClearSpecification.builder();
    csb.setStencilBufferClear(0);
    csb.setDepthBufferClear(1.0);
    csb.setColorBufferClear(new VectorI4F(0.0f, 0.0f, 0.0f, 0.0f));
    this.clear_spec = csb.build();
  }

  @Override
  public void onRender(
    final R2ExampleServicesType serv,
    final JCGLInterfaceGL33Type g,
    final AreaInclusiveUnsignedIType area,
    final R2MainType m,
    final int frame)
  {
    this.stencils.stencilsReset();
    this.stencils.stencilsSetMode(
      R2SceneStencilsMode.STENCIL_MODE_INSTANCES_ARE_NEGATIVE);

    this.opaques.opaquesReset();

    for (int index = 0; index < this.instances.length; ++index) {
      this.opaques.opaquesAddSingleInstanceInGroup(
        this.instances[index], this.material, index + 1);
    }

    {
      final JCGLFramebufferUsableType fb = this.gbuffer.getFramebuffer();
      final JCGLFramebuffersType g_fb = g.getFramebuffers();
      final JCGLClearType g_cl = g.getClear();
      final JCGLColorBufferMaskingType g_cb = g.getColorBufferMasking();
      final JCGLStencilBuffersType g_sb = g.getStencilBuffers();
      final JCGLDepthBuffersType g_db = g.getDepthBuffers();

      g_cb.colorBufferMask(true, true, true, true);
      g_db.depthBufferWriteEnable();
      g_sb.stencilBufferMask(JCGLFaceSelection.FACE_FRONT_AND_BACK, 0b11111111);

      g_fb.framebufferDrawBind(fb);
      g_cl.clear(this.clear_spec);

      this.matrices.withObserver(this.view, this.projection, mo -> {
        this.stencil_renderer.renderStencilsWithBoundBuffer(
          g, mo, this.stencils);
        this.geom_renderer.renderGeometryWithBoundBuffer(
          g, mo, this.opaques);
        return Unit.unit();
      });

      g_fb.framebufferDrawUnbind();
    }
  }

  @Override
  public void onFinish(
    final JCGLInterfaceGL33Type g,
    final R2MainType m)
  {
    this.quad.delete(g);
    this.shader.delete(g);
    this.stencil_renderer.delete(g);
  }
}