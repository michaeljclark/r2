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

package com.io7m.r2.core.shaders.provided;

import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.jcanephora.core.JCGLProgramShaderUsableType;
import com.io7m.jcanephora.core.JCGLProgramUniformType;
import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.JCGLType;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitContextMutableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r2.core.R2AbstractShader;
import com.io7m.r2.core.R2ExceptionShaderValidationFailed;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2MatricesObserverValuesType;
import com.io7m.r2.core.R2Projections;
import com.io7m.r2.core.shaders.types.R2ShaderInstanceBatchedType;
import com.io7m.r2.core.shaders.types.R2ShaderInstanceBatchedVerifier;
import com.io7m.r2.core.shaders.types.R2ShaderParameters;
import com.io7m.r2.core.shaders.types.R2ShaderPreprocessingEnvironmentReadableType;

import java.util.Optional;

/**
 * Basic deferred surface shader for batched instances.
 */

public final class R2SurfaceShaderBasicBatched extends
  R2AbstractShader<R2SurfaceShaderBasicParameters>
  implements R2ShaderInstanceBatchedType<R2SurfaceShaderBasicParameters>
{
  private final JCGLProgramUniformType u_depth_coefficient;
  private final JCGLProgramUniformType u_transform_view;
  private final JCGLProgramUniformType u_transform_projection;
  private final JCGLProgramUniformType u_emission_amount;
  private final JCGLProgramUniformType u_albedo_color;
  private final JCGLProgramUniformType u_albedo_mix;
  private final JCGLProgramUniformType u_specular_color;
  private final JCGLProgramUniformType u_specular_exponent;
  private final JCGLProgramUniformType u_texture_albedo;
  private final JCGLProgramUniformType u_texture_normal;
  private final JCGLProgramUniformType u_texture_specular;
  private final JCGLProgramUniformType u_texture_emission;
  private final JCGLProgramUniformType u_alpha_discard_threshold;

  private R2SurfaceShaderBasicBatched(
    final JCGLShadersType in_shaders,
    final R2ShaderPreprocessingEnvironmentReadableType in_shader_env,
    final R2IDPoolType in_pool)
  {
    super(
      in_shaders,
      in_shader_env,
      in_pool,
      "com.io7m.r2.shaders.core.R2SurfaceShaderBasicBatched",
      "com.io7m.r2.shaders.core/R2SurfaceBatched.vert",
      Optional.empty(),
      "com.io7m.r2.shaders.core/R2SurfaceBasicBatched.frag");

    final JCGLProgramShaderUsableType p = this.getShaderProgram();
    R2ShaderParameters.checkUniformParameterCount(p, 13);

    this.u_transform_projection =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view.transform_projection", JCGLType.TYPE_FLOAT_MATRIX_4);
    this.u_transform_view =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view.transform_view", JCGLType.TYPE_FLOAT_MATRIX_4);
    this.u_depth_coefficient =
      R2ShaderParameters.getUniformChecked(
        p, "R2_view.depth_coefficient", JCGLType.TYPE_FLOAT);

    this.u_emission_amount =
      R2ShaderParameters.getUniformChecked(
        p, "R2_basic_surface_parameters.emission_amount", JCGLType.TYPE_FLOAT);
    this.u_albedo_color =
      R2ShaderParameters.getUniformChecked(
        p,
        "R2_basic_surface_parameters.albedo_color",
        JCGLType.TYPE_FLOAT_VECTOR_4);
    this.u_albedo_mix =
      R2ShaderParameters.getUniformChecked(
        p, "R2_basic_surface_parameters.albedo_mix", JCGLType.TYPE_FLOAT);
    this.u_specular_color =
      R2ShaderParameters.getUniformChecked(
        p,
        "R2_basic_surface_parameters.specular_color",
        JCGLType.TYPE_FLOAT_VECTOR_3);
    this.u_specular_exponent =
      R2ShaderParameters.getUniformChecked(
        p,
        "R2_basic_surface_parameters.specular_exponent",
        JCGLType.TYPE_FLOAT);
    this.u_alpha_discard_threshold = R2ShaderParameters.getUniformChecked(
      p,
      "R2_basic_surface_parameters.alpha_discard_threshold",
      JCGLType.TYPE_FLOAT);

    this.u_texture_normal =
      R2ShaderParameters.getUniformChecked(
        p, "R2_surface_textures.normal", JCGLType.TYPE_SAMPLER_2D);

    this.u_texture_albedo =
      R2ShaderParameters.getUniformChecked(
        p, "R2_basic_surface_textures.albedo", JCGLType.TYPE_SAMPLER_2D);
    this.u_texture_specular =
      R2ShaderParameters.getUniformChecked(
        p, "R2_basic_surface_textures.specular", JCGLType.TYPE_SAMPLER_2D);
    this.u_texture_emission =
      R2ShaderParameters.getUniformChecked(
        p, "R2_basic_surface_textures.emission", JCGLType.TYPE_SAMPLER_2D);
  }

  /**
   * Construct a new shader.
   *
   * @param in_shaders    A shader interface
   * @param in_shader_env A shader preprocessing environment
   * @param in_pool       The ID pool
   *
   * @return A new shader
   */

  public static R2ShaderInstanceBatchedType<R2SurfaceShaderBasicParameters>
  newShader(
    final JCGLShadersType in_shaders,
    final R2ShaderPreprocessingEnvironmentReadableType in_shader_env,
    final R2IDPoolType in_pool)
  {
    return R2ShaderInstanceBatchedVerifier.newVerifier(
      new R2SurfaceShaderBasicBatched(in_shaders, in_shader_env, in_pool));
  }

  @Override
  public Class<R2SurfaceShaderBasicParameters> getShaderParametersType()
  {
    return R2SurfaceShaderBasicParameters.class;
  }

  @Override
  public void onValidate()
    throws R2ExceptionShaderValidationFailed
  {
    // Nothing
  }

  @Override
  public void onReceiveViewValues(
    final JCGLShadersType g_sh,
    final R2MatricesObserverValuesType m,
    final AreaInclusiveUnsignedLType viewport)
  {
    NullCheck.notNull(g_sh);
    NullCheck.notNull(m);
    NullCheck.notNull(viewport);

    g_sh.shaderUniformPutFloat(
      this.u_depth_coefficient,
      (float) R2Projections.getDepthCoefficient(m.projection()));
    g_sh.shaderUniformPutMatrix4x4f(
      this.u_transform_view, m.matrixView());
    g_sh.shaderUniformPutMatrix4x4f(
      this.u_transform_projection, m.matrixProjection());
  }

  @Override
  public void onReceiveMaterialValues(
    final JCGLTexturesType g_tex,
    final JCGLShadersType g_sh,
    final JCGLTextureUnitContextMutableType tc,
    final R2SurfaceShaderBasicParameters values)
  {
    NullCheck.notNull(g_tex);
    NullCheck.notNull(g_sh);
    NullCheck.notNull(tc);
    NullCheck.notNull(values);

    final JCGLTextureUnitType unit_albedo = tc.unitContextBindTexture2D(
      g_tex,
      values.albedoTexture().texture());
    final JCGLTextureUnitType unit_emission = tc.unitContextBindTexture2D(
      g_tex,
      values.emissionTexture().texture());
    final JCGLTextureUnitType unit_normal = tc.unitContextBindTexture2D(
      g_tex,
      values.normalTexture().texture());
    final JCGLTextureUnitType unit_specular = tc.unitContextBindTexture2D(
      g_tex,
      values.specularTexture().texture());

    g_sh.shaderUniformPutTexture2DUnit(
      this.u_texture_albedo, unit_albedo);
    g_sh.shaderUniformPutTexture2DUnit(
      this.u_texture_emission, unit_emission);
    g_sh.shaderUniformPutTexture2DUnit(
      this.u_texture_normal, unit_normal);
    g_sh.shaderUniformPutTexture2DUnit(
      this.u_texture_specular, unit_specular);

    g_sh.shaderUniformPutVector4f(
      this.u_albedo_color, values.albedoColor());
    g_sh.shaderUniformPutFloat(
      this.u_albedo_mix, values.albedoMix());

    g_sh.shaderUniformPutFloat(
      this.u_emission_amount, values.emission());

    g_sh.shaderUniformPutVector3f(
      this.u_specular_color, values.specularColor());
    g_sh.shaderUniformPutFloat(
      this.u_specular_exponent, values.specularExponent());

    g_sh.shaderUniformPutFloat(
      this.u_alpha_discard_threshold, values.alphaDiscardThreshold());
  }
}
