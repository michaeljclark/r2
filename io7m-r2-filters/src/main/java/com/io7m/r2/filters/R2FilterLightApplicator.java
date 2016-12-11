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

package com.io7m.r2.filters;

import com.io7m.jcanephora.core.JCGLFramebufferBlitBuffer;
import com.io7m.jcanephora.core.JCGLFramebufferBlitFilter;
import com.io7m.jcanephora.core.JCGLPrimitives;
import com.io7m.jcanephora.core.api.JCGLArrayObjectsType;
import com.io7m.jcanephora.core.api.JCGLDrawType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.core.api.JCGLViewportsType;
import com.io7m.jcanephora.profiler.JCGLProfilingContextType;
import com.io7m.jcanephora.renderstate.JCGLRenderStateMutable;
import com.io7m.jcanephora.renderstate.JCGLRenderStates;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jnull.NullCheck;
import com.io7m.r2.core.R2Exception;
import com.io7m.r2.core.R2FilterType;
import com.io7m.r2.core.R2GeometryBufferUsableType;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2Texture2DUsableType;
import com.io7m.r2.core.R2UnitQuadUsableType;
import com.io7m.r2.core.shaders.types.R2ShaderFilterType;
import com.io7m.r2.core.shaders.types.R2ShaderPreprocessingEnvironmentReadableType;

import java.util.EnumSet;
import java.util.Set;

/**
 * A trivial filter that combines a geometry buffer and a light buffer into a
 * lit image.
 *
 * @see com.io7m.r2.core.R2GeometryBufferUsableType
 * @see com.io7m.r2.core.R2LightBufferUsableType
 */

public final class R2FilterLightApplicator implements
  R2FilterType<R2FilterLightApplicatorParametersType>
{
  private static final Set<JCGLFramebufferBlitBuffer> BLIT_BUFFERS;

  static {
    BLIT_BUFFERS = EnumSet.of(
      JCGLFramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_DEPTH);
  }

  private final
  R2ShaderFilterType<R2ShaderFilterLightApplicatorParametersType> shader;

  private final JCGLInterfaceGL33Type g;
  private final R2UnitQuadUsableType quad;
  private final R2ShaderFilterLightApplicatorParametersMutable shader_params;
  private final JCGLRenderStateMutable render_state;

  private R2FilterLightApplicator(
    final JCGLInterfaceGL33Type in_g,
    final R2ShaderFilterType<R2ShaderFilterLightApplicatorParametersType>
      in_shader,
    final R2UnitQuadUsableType in_quad)
  {
    this.g = NullCheck.notNull(in_g);
    this.shader = NullCheck.notNull(in_shader);
    this.quad = NullCheck.notNull(in_quad);

    this.shader_params =
      R2ShaderFilterLightApplicatorParametersMutable.create();
    this.render_state = JCGLRenderStateMutable.create();
  }

  /**
   * Construct a new filter.
   *
   * @param in_shader_env Shader sources
   * @param in_g          A GL interface
   * @param in_pool       An ID pool
   * @param in_quad       A unit quad
   *
   * @return A new filter
   */

  public static R2FilterType<R2FilterLightApplicatorParametersType>
  newFilter(
    final R2ShaderPreprocessingEnvironmentReadableType in_shader_env,
    final JCGLInterfaceGL33Type in_g,
    final R2IDPoolType in_pool,
    final R2UnitQuadUsableType in_quad)
  {
    NullCheck.notNull(in_shader_env);
    NullCheck.notNull(in_g);
    NullCheck.notNull(in_pool);
    NullCheck.notNull(in_quad);

    final R2ShaderFilterType<R2ShaderFilterLightApplicatorParametersType> s =
      R2ShaderFilterLightApplicator.newShader(
        in_g.getShaders(),
        in_shader_env,
        in_pool);

    return new R2FilterLightApplicator(in_g, s, in_quad);
  }

  @Override
  public void delete(final JCGLInterfaceGL33Type gx)
    throws R2Exception
  {
    if (!this.isDeleted()) {
      this.shader.delete(gx);
    }
  }

  @Override
  public boolean isDeleted()
  {
    return this.shader.isDeleted();
  }


  @Override
  public void runFilter(
    final JCGLProfilingContextType pc,
    final JCGLTextureUnitContextParentType uc,
    final R2FilterLightApplicatorParametersType parameters)
  {
    NullCheck.notNull(uc);
    NullCheck.notNull(parameters);

    final JCGLProfilingContextType pc_base =
      pc.getChildContext("light-applicator");
    pc_base.startMeasuringIfEnabled();
    try {
      this.run(uc, parameters);
    } finally {
      pc_base.stopMeasuringIfEnabled();
    }
  }

  private void run(
    final JCGLTextureUnitContextParentType uc,
    final R2FilterLightApplicatorParametersType parameters)
  {
    final R2Texture2DUsableType ldiff =
      parameters.lightDiffuseTexture();
    final R2Texture2DUsableType lspec =
      parameters.lightSpecularTexture();
    final R2GeometryBufferUsableType gb =
      parameters.geometryBuffer();

    final JCGLShadersType g_sh = this.g.getShaders();
    final JCGLDrawType g_dr = this.g.getDraw();
    final JCGLArrayObjectsType g_ao = this.g.getArrayObjects();
    final JCGLTexturesType g_tx = this.g.getTextures();
    final JCGLViewportsType g_v = this.g.getViewports();
    final JCGLFramebuffersType g_fb = this.g.getFramebuffers();

    switch (parameters.copyDepth()) {
      case R2_COPY_DEPTH_ENABLED: {
        g_fb.framebufferReadBind(gb.primaryFramebuffer());
        g_fb.framebufferBlit(
          gb.area(),
          parameters.outputViewport(),
          R2FilterLightApplicator.BLIT_BUFFERS,
          JCGLFramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST);
        g_fb.framebufferReadUnbind();
        break;
      }
      case R2_COPY_DEPTH_DISABLED: {
        break;
      }
    }

    g_v.viewportSet(parameters.outputViewport());
    JCGLRenderStates.activate(this.g, this.render_state);

    final JCGLTextureUnitContextType c = uc.unitContextNew();
    try {
      this.shader_params.setAlbedoTexture(gb.albedoEmissiveTexture());
      this.shader_params.setDiffuseTexture(ldiff);
      this.shader_params.setSpecularTexture(lspec);

      try {
        this.shader.onActivate(g_sh);
        this.shader.onReceiveFilterValues(g_tx, g_sh, c, this.shader_params);
        this.shader.onValidate();

        g_ao.arrayObjectBind(this.quad.arrayObject());
        g_dr.drawElements(JCGLPrimitives.PRIMITIVE_TRIANGLES);
      } finally {
        g_ao.arrayObjectUnbind();
        this.shader.onDeactivate(g_sh);
      }

    } finally {
      c.unitContextFinish(g_tx);
    }
  }
}
