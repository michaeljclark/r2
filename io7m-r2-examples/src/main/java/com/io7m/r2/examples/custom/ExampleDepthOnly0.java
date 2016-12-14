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

import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PMatrix4x4FType;
import com.io7m.jtensors.parameterized.PMatrixHeapArrayM4x4F;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r2.core.R2DepthInstances;
import com.io7m.r2.core.R2DepthInstancesType;
import com.io7m.r2.core.R2DepthOnlyBuffer;
import com.io7m.r2.core.R2DepthOnlyBufferDescription;
import com.io7m.r2.core.R2DepthOnlyBufferType;
import com.io7m.r2.core.R2DepthPrecision;
import com.io7m.r2.core.R2DepthRendererType;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2InstanceSingle;
import com.io7m.r2.core.R2InstanceSingleType;
import com.io7m.r2.core.R2MaterialDepthSingle;
import com.io7m.r2.core.R2MaterialDepthSingleType;
import com.io7m.r2.core.R2MaterialOpaqueSingle;
import com.io7m.r2.core.R2MaterialOpaqueSingleType;
import com.io7m.r2.core.R2MatricesType;
import com.io7m.r2.core.R2ProjectionFOV;
import com.io7m.r2.core.R2TransformReadableType;
import com.io7m.r2.core.R2TransformSOT;
import com.io7m.r2.core.R2UnitSphereType;
import com.io7m.r2.core.shaders.provided.R2DepthShaderBasicParameters;
import com.io7m.r2.core.shaders.provided.R2DepthShaderBasicSingle;
import com.io7m.r2.core.shaders.provided.R2SurfaceShaderBasicParameters;
import com.io7m.r2.core.shaders.provided.R2SurfaceShaderBasicSingle;
import com.io7m.r2.core.shaders.types.R2ShaderDepthSingleType;
import com.io7m.r2.core.shaders.types.R2ShaderInstanceSingleType;
import com.io7m.r2.core.shaders.types.R2ShaderPreprocessingEnvironment;
import com.io7m.r2.core.shaders.types.R2ShaderPreprocessingEnvironmentType;
import com.io7m.r2.examples.R2ExampleCustomType;
import com.io7m.r2.examples.R2ExampleServicesType;
import com.io7m.r2.main.R2MainType;
import com.io7m.r2.meshes.defaults.R2UnitSphere;
import com.io7m.r2.spaces.R2SpaceEyeType;
import com.io7m.r2.spaces.R2SpaceWorldType;
import com.io7m.sombrero.core.SoShaderPreprocessorConfig;
import com.io7m.sombrero.core.SoShaderPreprocessorType;
import com.io7m.sombrero.core.SoShaderResolver;
import com.io7m.sombrero.jcpp.SoShaderPreprocessorJCPP;

import java.util.OptionalInt;

// CHECKSTYLE_JAVADOC:OFF

public final class ExampleDepthOnly0 implements R2ExampleCustomType
{
  private final PMatrix4x4FType<R2SpaceWorldType, R2SpaceEyeType> view;

  private R2ProjectionFOV projection;

  private R2ShaderInstanceSingleType<R2SurfaceShaderBasicParameters> shader;
  private R2SurfaceShaderBasicParameters shader_params;
  private R2MaterialOpaqueSingleType<R2SurfaceShaderBasicParameters> material;

  private R2UnitSphereType sphere;
  private R2InstanceSingleType instance;

  private R2DepthOnlyBufferType dbuffer;
  private R2DepthRendererType depth_renderer;
  private R2DepthInstancesType depth_instances;
  private R2DepthShaderBasicParameters depth_shader_params;

  private R2ShaderDepthSingleType<R2DepthShaderBasicParameters> depth_shader;
  private R2MaterialDepthSingleType<R2DepthShaderBasicParameters> depth_material;

  private R2MainType main;
  private JCGLInterfaceGL33Type g33;

  public ExampleDepthOnly0()
  {
    this.view = PMatrixHeapArrayM4x4F.newMatrix();
  }

  @Override
  public void onInitialize(
    final R2ExampleServicesType serv,
    final JCGLInterfaceGL33Type g,
    final AreaInclusiveUnsignedLType area,
    final R2MainType m)
  {
    this.main = NullCheck.notNull(m);

    this.depth_instances = R2DepthInstances.newDepthInstances();

    {
      final R2DepthOnlyBufferDescription.Builder desc =
        R2DepthOnlyBufferDescription.builder();
      desc.setArea(area);
      desc.setDepthPrecision(R2DepthPrecision.R2_DEPTH_PRECISION_24);

      this.dbuffer = R2DepthOnlyBuffer.newDepthOnlyBuffer(
        g.getFramebuffers(),
        g.getTextures(),
        m.getTextureUnitAllocator().getRootContext(),
        desc.build());
    }

    this.depth_renderer =
      m.getDepthRenderer();

    this.projection = R2ProjectionFOV.newFrustumWith(
      m.getProjectionMatrices(),
      (float) Math.toRadians(90.0f), 640.0f / 480.0f, 0.01f, 1000.0f);

    m.getViewMatrices().lookAt(
      this.view,
      new VectorI3F(0.0f, 0.0f, 5.0f),
      new VectorI3F(0.0f, 0.0f, 0.0f),
      new VectorI3F(0.0f, 1.0f, 0.0f));

    final R2IDPoolType id_pool = m.getIDPool();

    this.sphere = R2UnitSphere.newUnitSphere8(g);

    final R2TransformReadableType tr = R2TransformSOT.newTransform();
    this.instance = R2InstanceSingle.of(
      id_pool.freshID(),
      this.sphere.arrayObject(),
      tr,
      PMatrixI3x3F.identity());

    final SoShaderPreprocessorConfig.Builder b =
      SoShaderPreprocessorConfig.builder();
    b.setResolver(SoShaderResolver.create());
    b.setVersion(OptionalInt.of(330));
    final SoShaderPreprocessorType p =
      SoShaderPreprocessorJCPP.create(b.build());
    final R2ShaderPreprocessingEnvironmentType sources =
      R2ShaderPreprocessingEnvironment.create(p);

    this.depth_shader = R2DepthShaderBasicSingle.newShader(
      g.getShaders(), m.getShaderPreprocessingEnvironment(), m.getIDPool());
    this.depth_shader_params = R2DepthShaderBasicParameters.of(
      m.getTextureDefaults(), m.getTextureDefaults().texture2DWhite(), 0.1f);
    this.depth_material = R2MaterialDepthSingle.of(
      id_pool.freshID(), this.depth_shader, this.depth_shader_params);

    this.shader =
      R2SurfaceShaderBasicSingle.newShader(g.getShaders(), sources, id_pool);
    this.shader_params =
      R2SurfaceShaderBasicParameters.builder()
        .setTextureDefaults(m.getTextureDefaults())
        .build();
    this.material = R2MaterialOpaqueSingle.of(
      id_pool.freshID(), this.shader, this.shader_params);
  }

  @Override
  public void onRender(
    final R2ExampleServicesType serv,
    final JCGLInterfaceGL33Type g,
    final AreaInclusiveUnsignedLType area,
    final R2MainType m,
    final int frame)
  {
    this.g33 = g;

    this.depth_instances.depthsReset();
    this.depth_instances.depthsAddSingleInstance(
      this.instance, this.depth_material);

    if (serv.isFreeCameraEnabled()) {
      MatrixM4x4F.copy(serv.getFreeCameraViewMatrix(), this.view);
    } else {
      m.getViewMatrices().lookAt(
        this.view,
        new VectorI3F(0.0f, 0.0f, 5.0f),
        new VectorI3F(0.0f, 0.0f, 0.0f),
        new VectorI3F(0.0f, 1.0f, 0.0f));
    }

    final R2MatricesType matrices = m.getMatrices();
    matrices.withObserver(this.view, this.projection, this, (mo, t) -> {
      final JCGLFramebufferUsableType dbuffer_fb =
        t.dbuffer.primaryFramebuffer();

      final JCGLFramebuffersType g_fb = t.g33.getFramebuffers();

      g_fb.framebufferDrawBind(dbuffer_fb);
      this.dbuffer.clearBoundPrimaryFramebuffer(t.g33);
      t.depth_renderer.renderDepthWithBoundBuffer(
        t.dbuffer.area(),
        t.main.getTextureUnitAllocator().getRootContext(),
        mo,
        t.depth_instances);
      g_fb.framebufferDrawUnbind();
      return Unit.unit();
    });
  }

  @Override
  public void onFinish(
    final JCGLInterfaceGL33Type g,
    final R2MainType m)
  {
    this.shader.delete(g);
  }
}
