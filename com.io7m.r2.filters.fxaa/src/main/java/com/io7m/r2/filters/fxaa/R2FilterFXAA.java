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

package com.io7m.r2.filters.fxaa;

import com.io7m.jcanephora.core.JCGLPrimitives;
import com.io7m.jcanephora.core.api.JCGLArrayObjectsType;
import com.io7m.jcanephora.core.api.JCGLDrawType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.core.api.JCGLViewportsType;
import com.io7m.jcanephora.profiler.JCGLProfilingContextType;
import com.io7m.jcanephora.renderstate.JCGLRenderState;
import com.io7m.jcanephora.renderstate.JCGLRenderStates;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jnull.NullCheck;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizesL;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r2.core.api.R2Exception;
import com.io7m.r2.core.api.ids.R2IDPoolType;
import com.io7m.r2.filters.fxaa.api.R2FilterFXAAParameters;
import com.io7m.r2.filters.fxaa.api.R2FilterFXAAQuality;
import com.io7m.r2.filters.fxaa.api.R2FilterFXAAType;
import com.io7m.r2.shaders.api.R2ShaderPreprocessingEnvironmentReadableType;
import com.io7m.r2.shaders.filter.api.R2ShaderParametersFilterMutable;
import com.io7m.r2.shaders.fxaa.R2ShaderFilterFXAA;
import com.io7m.r2.shaders.fxaa.R2ShaderFilterFXAAParameters;
import com.io7m.r2.shaders.fxaa.RShaderFXAAQuality;
import com.io7m.r2.textures.R2Texture2DUsableType;
import com.io7m.r2.unit_quads.R2UnitQuadUsableType;

import java.util.EnumMap;
import java.util.Map;

/**
 * <p>An FXAA filter.</p>
 *
 * <p>The filter takes a texture as input and writes a filtered image to
 * the currently bound framebuffer.</p>
 *
 * @see com.io7m.r2.filters.fxaa.api.R2FilterFXAAParametersType#texture()
 */

public final class R2FilterFXAA implements R2FilterFXAAType
{
  private final JCGLInterfaceGL33Type g;
  private final Map<R2FilterFXAAQuality, R2ShaderFilterFXAA> shaders;
  private final R2UnitQuadUsableType quad;
  private final JCGLRenderState render_state;
  private final R2ShaderParametersFilterMutable<R2ShaderFilterFXAAParameters> values;
  private boolean deleted;

  private R2FilterFXAA(
    final JCGLInterfaceGL33Type in_g,
    final EnumMap<R2FilterFXAAQuality, R2ShaderFilterFXAA> in_shaders,
    final R2UnitQuadUsableType in_quad)
  {
    this.g =
      NullCheck.notNull(in_g, "G33");
    this.shaders =
      NullCheck.notNull(in_shaders, "Shaders");
    this.quad =
      NullCheck.notNull(in_quad, "Quad");

    this.render_state = JCGLRenderState.builder().build();
    this.values = R2ShaderParametersFilterMutable.create();
  }

  /**
   * Create a new FXAA filter.
   *
   * @param in_g          A GL interface
   * @param in_shader_env Shader sources
   * @param in_id_pool    An ID pool
   * @param in_quad       A unit quad
   *
   * @return A new filter
   */

  public static R2FilterFXAAType newFilter(
    final JCGLInterfaceGL33Type in_g,
    final R2ShaderPreprocessingEnvironmentReadableType in_shader_env,
    final R2IDPoolType in_id_pool,
    final R2UnitQuadUsableType in_quad)
  {
    final EnumMap<R2FilterFXAAQuality, R2ShaderFilterFXAA> sh =
      new EnumMap<>(R2FilterFXAAQuality.class);

    final JCGLShadersType g_sh = in_g.shaders();
    for (final R2FilterFXAAQuality c : R2FilterFXAAQuality.values()) {
      sh.put(
        c,
        R2ShaderFilterFXAA.create(
          g_sh,
          in_shader_env,
          in_id_pool,
          shaderQuality(c)));
    }

    return new R2FilterFXAA(in_g, sh, in_quad);
  }

  private static RShaderFXAAQuality shaderQuality(
    final R2FilterFXAAQuality c)
  {
    switch (c) {
      case R2_FXAA_QUALITY_10:
        return RShaderFXAAQuality.R2_FXAA_QUALITY_10;
      case R2_FXAA_QUALITY_15:
        return RShaderFXAAQuality.R2_FXAA_QUALITY_15;
      case R2_FXAA_QUALITY_20:
        return RShaderFXAAQuality.R2_FXAA_QUALITY_20;
      case R2_FXAA_QUALITY_25:
        return RShaderFXAAQuality.R2_FXAA_QUALITY_25;
      case R2_FXAA_QUALITY_29:
        return RShaderFXAAQuality.R2_FXAA_QUALITY_29;
      case R2_FXAA_QUALITY_39:
        return RShaderFXAAQuality.R2_FXAA_QUALITY_39;
    }

    throw new UnreachableCodeException();
  }

  @Override
  public void delete(final JCGLInterfaceGL33Type gx)
    throws R2Exception
  {
    NullCheck.notNull(gx, "G33");

    if (!this.deleted) {
      for (final R2FilterFXAAQuality c : R2FilterFXAAQuality.values()) {
        final R2ShaderFilterFXAA sh = this.shaders.get(c);
        sh.delete(gx);
      }
      this.deleted = true;
    }
  }

  @Override
  public boolean isDeleted()
  {
    return this.deleted;
  }

  @Override
  public void runFilter(
    final JCGLProfilingContextType pc,
    final JCGLTextureUnitContextParentType uc,
    final R2FilterFXAAParameters parameters)
  {
    NullCheck.notNull(pc, "Profiling");
    NullCheck.notNull(uc, "Texture context");
    NullCheck.notNull(parameters, "Filter parameters");

    final JCGLProfilingContextType pc_base = pc.childContext("fxaa");
    pc_base.startMeasuringIfEnabled();
    try {
      this.run(uc, parameters);
    } finally {
      pc_base.stopMeasuringIfEnabled();
    }
  }

  private void run(
    final JCGLTextureUnitContextParentType uc,
    final R2FilterFXAAParameters parameters)
  {
    final JCGLShadersType g_sh = this.g.shaders();
    final JCGLDrawType g_dr = this.g.drawing();
    final JCGLArrayObjectsType g_ao = this.g.arrayObjects();
    final JCGLTexturesType g_tx = this.g.textures();
    final JCGLViewportsType g_v = this.g.viewports();

    final R2Texture2DUsableType t = parameters.texture();
    final JCGLTextureUnitContextType c = uc.unitContextNew();

    try {
      this.values.setTextureUnitContext(c);
      this.values.setValues(
        R2ShaderFilterFXAAParameters.builder()
          .setEdgeThreshold(parameters.edgeThreshold())
          .setEdgeThresholdMinimum(parameters.edgeThresholdMinimum())
          .setSubPixelAliasingRemoval(parameters.subPixelAliasingRemoval())
          .setTexture(parameters.texture())
          .build());

      final R2ShaderFilterFXAA sh = this.shaders.get(parameters.quality());

      g_v.viewportSet(AreaSizesL.area(t.texture().size()));
      JCGLRenderStates.activate(this.g, this.render_state);

      try {
        sh.onActivate(this.g);
        sh.onReceiveFilterValues(this.g, this.values);
        sh.onValidate();
        g_ao.arrayObjectBind(this.quad.arrayObject());
        g_dr.drawElements(JCGLPrimitives.PRIMITIVE_TRIANGLES);
      } finally {
        g_ao.arrayObjectUnbind();
        sh.onDeactivate(this.g);
      }

    } finally {
      c.unitContextFinish(g_tx);
    }
  }
}
