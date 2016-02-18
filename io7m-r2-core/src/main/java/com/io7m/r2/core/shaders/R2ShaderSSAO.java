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

package com.io7m.r2.core.shaders;

import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.jcanephora.core.JCGLProgramShaderUsableType;
import com.io7m.jcanephora.core.JCGLProgramUniformType;
import com.io7m.jcanephora.core.JCGLTexture2DUsableType;
import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorM2F;
import com.io7m.junsigned.ranges.UnsignedRangeInclusiveL;
import com.io7m.r2.core.R2AbstractShader;
import com.io7m.r2.core.R2GeometryBufferUsableType;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2MatricesObserverValuesType;
import com.io7m.r2.core.R2Projections;
import com.io7m.r2.core.R2ShaderGBufferConsumerType;
import com.io7m.r2.core.R2ShaderParameters;
import com.io7m.r2.core.R2ShaderScreenType;
import com.io7m.r2.core.R2ShaderSourcesType;
import com.io7m.r2.core.R2TextureUnitContextMutableType;
import com.io7m.r2.core.R2ViewRaysReadableType;
import com.io7m.r2.core.filters.R2SSAOKernelReadableType;
import org.valid4j.Assertive;

import java.util.Map;
import java.util.Optional;

/**
 * An SSAO shader.
 */

public final class R2ShaderSSAO extends
  R2AbstractShader<R2ShaderSSAOParameters>
  implements R2ShaderScreenType<R2ShaderSSAOParameters>,
  R2ShaderGBufferConsumerType
{
  private final VectorM2F              noise_uv_scale;
  private final JCGLProgramUniformType u_ssao_noise_uv_scale;
  private final JCGLProgramUniformType u_ssao_kernel;
  private final JCGLProgramUniformType u_ssao_kernel_size;
  private final JCGLProgramUniformType u_ssao_sample_radius;
  private final JCGLProgramUniformType u_ssao_texture_noise;
  private final JCGLProgramUniformType u_ssao_transform_projection;
  private final JCGLProgramUniformType u_ssao_power;
  private final JCGLProgramUniformType u_depth_coefficient;
  private final JCGLProgramUniformType u_view_rays_origin_x0y0;
  private final JCGLProgramUniformType u_view_rays_origin_x1y0;
  private final JCGLProgramUniformType u_view_rays_origin_x0y1;
  private final JCGLProgramUniformType u_view_rays_origin_x1y1;
  private final JCGLProgramUniformType u_view_rays_ray_x0y0;
  private final JCGLProgramUniformType u_view_rays_ray_x1y0;
  private final JCGLProgramUniformType u_view_rays_ray_x0y1;
  private final JCGLProgramUniformType u_view_rays_ray_x1y1;
  private final JCGLProgramUniformType u_gbuffer_albedo;
  private final JCGLProgramUniformType u_gbuffer_normal;
  private final JCGLProgramUniformType u_gbuffer_specular;
  private final JCGLProgramUniformType u_gbuffer_depth;

  private JCGLTextureUnitType      unit_noise;
  private long                     kernel_version;
  private R2SSAOKernelReadableType kernel_last;

  private R2ShaderSSAO(
    final JCGLShadersType in_shaders,
    final R2ShaderSourcesType in_sources,
    final R2IDPoolType in_pool)
  {
    super(
      in_shaders,
      in_sources,
      in_pool,
      "R2SSAO",
      "R2SSAO.vert",
      Optional.empty(),
      "R2SSAO.frag");

    this.noise_uv_scale = new VectorM2F();

    final JCGLProgramShaderUsableType p = this.getShaderProgram();
    final Map<String, JCGLProgramUniformType> us = p.getUniforms();
    Assertive.ensure(
      us.size() == 20,
      "Expected number of parameters is 20 (got %d)",
      Integer.valueOf(us.size()));

    this.u_gbuffer_albedo =
      R2ShaderParameters.getUniformChecked(
        p, "R2_gbuffer.albedo");
    this.u_gbuffer_normal =
      R2ShaderParameters.getUniformChecked(
        p, "R2_gbuffer.normal");
    this.u_gbuffer_specular =
      R2ShaderParameters.getUniformChecked(
        p, "R2_gbuffer.specular");
    this.u_gbuffer_depth =
      R2ShaderParameters.getUniformChecked(
        p, "R2_gbuffer.depth");

    this.u_ssao_noise_uv_scale =
      R2ShaderParameters.getUniformChecked(
        p, "R2_ssao_noise_uv_scale");
    this.u_ssao_kernel =
      R2ShaderParameters.getUniformChecked(
        p, "R2_ssao_kernel[0]");
    this.u_ssao_kernel_size =
      R2ShaderParameters.getUniformChecked(
        p, "R2_ssao_kernel_size");
    this.u_ssao_texture_noise =
      R2ShaderParameters.getUniformChecked(
        p, "R2_ssao_noise");
    this.u_ssao_sample_radius =
      R2ShaderParameters.getUniformChecked(
        p, "R2_ssao_sample_radius");
    this.u_ssao_power =
      R2ShaderParameters.getUniformChecked(
        p, "R2_ssao_power");
    this.u_ssao_transform_projection =
      R2ShaderParameters.getUniformChecked(
        p, "R2_ssao_transform_projection");

    this.u_depth_coefficient =
      R2ShaderParameters.getUniformChecked(
        p, "R2_depth_coefficient");

    this.u_view_rays_origin_x0y0 =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view_rays.origin_x0y0");
    this.u_view_rays_origin_x1y0 =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view_rays.origin_x1y0");
    this.u_view_rays_origin_x0y1 =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view_rays.origin_x0y1");
    this.u_view_rays_origin_x1y1 =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view_rays.origin_x1y1");

    this.u_view_rays_ray_x0y0 =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view_rays.ray_x0y0");
    this.u_view_rays_ray_x1y0 =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view_rays.ray_x1y0");
    this.u_view_rays_ray_x0y1 =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view_rays.ray_x0y1");
    this.u_view_rays_ray_x1y1 =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view_rays.ray_x1y1");
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

  public static R2ShaderSSAO
  newShader(
    final JCGLShadersType in_shaders,
    final R2ShaderSourcesType in_sources,
    final R2IDPoolType in_pool)
  {
    return new R2ShaderSSAO(in_shaders, in_sources, in_pool);
  }

  @Override
  public Class<R2ShaderSSAOParameters>
  getShaderParametersType()
  {
    return R2ShaderSSAOParameters.class;
  }

  /**
   * Bind any textures needed for execution.
   *
   * @param g_tex  A texture interface
   * @param uc     A texture interface
   * @param values The parameters
   */

  public void setTextures(
    final JCGLTexturesType g_tex,
    final R2TextureUnitContextMutableType uc,
    final R2ShaderSSAOParameters values)
  {
    NullCheck.notNull(uc);
    NullCheck.notNull(values);

    this.unit_noise = uc.unitContextBindTexture2D(
      g_tex,
      values.getNoiseTexture());
  }

  /**
   * Set the view-dependent values for the shader.
   *
   * @param g_sh A shader interface
   * @param m    The view matrices
   */

  public void setViewDependentValues(
    final JCGLShadersType g_sh,
    final R2MatricesObserverValuesType m)
  {
    NullCheck.notNull(g_sh);
    NullCheck.notNull(m);

    /**
     * Upload the current view rays.
     */

    final R2ViewRaysReadableType view_rays = m.getViewRays();
    g_sh.shaderUniformPutVector3f(
      this.u_view_rays_origin_x0y0, view_rays.getOriginX0Y0());
    g_sh.shaderUniformPutVector3f(
      this.u_view_rays_origin_x1y0, view_rays.getOriginX1Y0());
    g_sh.shaderUniformPutVector3f(
      this.u_view_rays_origin_x0y1, view_rays.getOriginX0Y1());
    g_sh.shaderUniformPutVector3f(
      this.u_view_rays_origin_x1y1, view_rays.getOriginX1Y1());

    g_sh.shaderUniformPutVector3f(
      this.u_view_rays_ray_x0y0, view_rays.getRayX0Y0());
    g_sh.shaderUniformPutVector3f(
      this.u_view_rays_ray_x1y0, view_rays.getRayX1Y0());
    g_sh.shaderUniformPutVector3f(
      this.u_view_rays_ray_x0y1, view_rays.getRayX0Y1());
    g_sh.shaderUniformPutVector3f(
      this.u_view_rays_ray_x1y1, view_rays.getRayX1Y1());

    /**
     * Upload the projections for the light volume.
     */

    g_sh.shaderUniformPutMatrix4x4f(
      this.u_ssao_transform_projection, m.getMatrixProjection());

    /**
     * Upload the scene's depth coefficient.
     */

    g_sh.shaderUniformPutFloat(
      this.u_depth_coefficient,
      (float) R2Projections.getDepthCoefficient(m.getProjection()));
  }

  /**
   * Set any shader parameters needed for execution.
   *
   * @param g_sh   A shader interface
   * @param values The parameters
   */

  public void setValues(
    final JCGLShadersType g_sh,
    final R2ShaderSSAOParameters values)
  {
    NullCheck.notNull(g_sh);
    NullCheck.notNull(values);

    final AreaInclusiveUnsignedLType viewport_area = values.getViewport();
    final UnsignedRangeInclusiveL range_x = viewport_area.getRangeX();
    final UnsignedRangeInclusiveL range_y = viewport_area.getRangeY();

    final JCGLTexture2DUsableType noise = values.getNoiseTexture().get();
    this.noise_uv_scale.set2F(
      (float) (range_x.getInterval() / noise.textureGetWidth()),
      (float) (range_y.getInterval() / noise.textureGetHeight())
    );

    g_sh.shaderUniformPutVector2f(
      this.u_ssao_noise_uv_scale, this.noise_uv_scale);
    g_sh.shaderUniformPutTexture2DUnit(
      this.u_ssao_texture_noise, this.unit_noise);
    g_sh.shaderUniformPutFloat(
      this.u_ssao_sample_radius, values.getSampleRadius());
    g_sh.shaderUniformPutFloat(
      this.u_ssao_power, values.getPower());

    final R2SSAOKernelReadableType k = values.getKernel();
    if (this.shouldSetKernel(k)) {
      g_sh.shaderUniformPutVectorf(this.u_ssao_kernel, k.getFloatBuffer());
      g_sh.shaderUniformPutInteger(this.u_ssao_kernel_size, k.getSize());
      this.kernel_last = k;
      this.kernel_version = k.getVersion();
    }
  }

  private boolean shouldSetKernel(
    final R2SSAOKernelReadableType k)
  {
    if (this.kernel_last == null) {
      return true;
    }

    return this.kernel_version != k.getVersion();
  }

  @Override
  public void setGBuffer(
    final JCGLShadersType g_sh,
    final R2GeometryBufferUsableType g,
    final JCGLTextureUnitType unit_albedo,
    final JCGLTextureUnitType unit_specular,
    final JCGLTextureUnitType unit_depth,
    final JCGLTextureUnitType unit_normals)
  {
    /**
     * Set each of the required G-Buffer textures.
     */

    g_sh.shaderUniformPutTexture2DUnit(this.u_gbuffer_albedo, unit_albedo);
    g_sh.shaderUniformPutTexture2DUnit(this.u_gbuffer_normal, unit_normals);
    g_sh.shaderUniformPutTexture2DUnit(this.u_gbuffer_specular, unit_specular);
    g_sh.shaderUniformPutTexture2DUnit(this.u_gbuffer_depth, unit_depth);
  }
}