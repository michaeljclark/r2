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

package com.io7m.r2.filters.debug.eye_position;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jcanephora.core.JCGLFramebufferBlitBuffer;
import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.JCGLPrimitives;
import com.io7m.jcanephora.core.api.JCGLArrayObjectsType;
import com.io7m.jcanephora.core.api.JCGLDrawType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.core.api.JCGLViewportsType;
import com.io7m.jcanephora.profiler.JCGLProfilingContextType;
import com.io7m.jcanephora.renderstate.JCGLRenderState;
import com.io7m.jcanephora.renderstate.JCGLRenderStates;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jregions.core.unparameterized.areas.AreaL;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizesL;
import com.io7m.jtensors.core.parameterized.matrices.PMatrices3x3D;
import com.io7m.r2.core.api.R2Exception;
import com.io7m.r2.core.api.ids.R2IDPoolType;
import com.io7m.r2.filters.api.R2FilterType;
import com.io7m.r2.filters.debug.eye_position.api.R2EyePositionBufferUsableType;
import com.io7m.r2.filters.debug.eye_position.api.R2FilterDebugEyePositionParameters;
import com.io7m.r2.filters.debug.eye_position.api.R2FilterDebugEyePositionType;
import com.io7m.r2.matrices.R2MatricesObserverType;
import com.io7m.r2.rendering.geometry.api.R2GeometryBufferUsableType;
import com.io7m.r2.shaders.filter.api.R2ShaderParametersFilterMutable;
import com.io7m.r2.transforms.R2TransformIdentity;
import com.io7m.r2.shaders.api.R2ShaderPreprocessingEnvironmentReadableType;
import com.io7m.r2.unit_quads.R2UnitQuadUsableType;

import java.util.EnumSet;
import java.util.Set;

import static com.io7m.jcanephora.core.JCGLFramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST;

/**
 * A filter that recovers the eye-space position of the pixels in the given
 * geometry buffer.
 */

public final class R2FilterDebugEyePosition
  implements R2FilterDebugEyePositionType
{
  private static final Set<JCGLFramebufferBlitBuffer> DEPTH_STENCIL;

  static {
    DEPTH_STENCIL = EnumSet.of(
      JCGLFramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_DEPTH,
      JCGLFramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_STENCIL);
  }

  private final R2ShaderFilterDebugEyePosition shader;
  private final JCGLInterfaceGL33Type g;
  private final JCGLRenderState render_state;
  private final R2UnitQuadUsableType quad;
  private final R2ShaderParametersFilterMutable<R2FilterDebugEyePositionParameters> values;

  private R2FilterDebugEyePosition(
    final JCGLInterfaceGL33Type in_g,
    final R2ShaderPreprocessingEnvironmentReadableType in_shader_env,
    final R2IDPoolType in_pool,
    final R2UnitQuadUsableType in_quad)
  {
    this.g = NullCheck.notNull(in_g, "G33");
    this.quad = NullCheck.notNull(in_quad, "Quad");

    this.shader = R2ShaderFilterDebugEyePosition.newShader(
      this.g.shaders(), in_shader_env, in_pool);
    this.render_state = JCGLRenderState.builder().build();

    this.values = R2ShaderParametersFilterMutable.create();
  }

  /**
   * @param in_g          An OpenGL interface
   * @param in_shader_env Shader sources
   * @param in_pool       The ID pool
   * @param in_quad       A unit quad
   *
   * @return A new renderer
   */

  public static R2FilterType<R2FilterDebugEyePositionParameters>
  newRenderer(
    final JCGLInterfaceGL33Type in_g,
    final R2ShaderPreprocessingEnvironmentReadableType in_shader_env,
    final R2IDPoolType in_pool,
    final R2UnitQuadUsableType in_quad)
  {
    return new R2FilterDebugEyePosition(in_g, in_shader_env, in_pool, in_quad);
  }

  @Override
  public void delete(final JCGLInterfaceGL33Type g33)
    throws R2Exception
  {
    if (!this.isDeleted()) {
      this.shader.delete(g33);
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
    final R2FilterDebugEyePositionParameters parameters)
  {
    NullCheck.notNull(pc, "Profiling");
    NullCheck.notNull(uc, "Texture context");
    NullCheck.notNull(parameters, "Filter parameters");

    Preconditions.checkPrecondition(
      !this.isDeleted(), "Filter must not be deleted");

    final JCGLProfilingContextType pc_base =
      pc.childContext("debug-eye-position");
    pc_base.startMeasuringIfEnabled();
    try {
      this.run(uc, parameters);
    } finally {
      pc_base.stopMeasuringIfEnabled();
    }
  }

  private void run(
    final JCGLTextureUnitContextParentType uc,
    final R2FilterDebugEyePositionParameters parameters)
  {
    final R2GeometryBufferUsableType gbuffer =
      parameters.geometryBuffer();
    final R2EyePositionBufferUsableType ebuffer =
      parameters.eyePositionBuffer();
    final JCGLFramebufferUsableType gb_fb =
      gbuffer.primaryFramebuffer();

    final JCGLArrayObjectsType g_ao = this.g.arrayObjects();
    final JCGLFramebuffersType g_fb = this.g.framebuffers();
    final JCGLTexturesType g_tex = this.g.textures();
    final JCGLShadersType g_sh = this.g.shaders();
    final JCGLDrawType g_dr = this.g.drawing();
    final JCGLViewportsType g_v = this.g.viewports();

    try {
      g_fb.framebufferDrawBind(ebuffer.framebuffer());
      final AreaL viewport = AreaSizesL.area(ebuffer.area());

      /*
       * Copy the contents of the depth/stencil attachment of the G-Buffer to
       * the current depth/stencil buffer.
       */

      g_fb.framebufferReadBind(gb_fb);
      g_fb.framebufferBlit(
        gbuffer.sizeAsViewport(),
        viewport,
        DEPTH_STENCIL,
        FRAMEBUFFER_BLIT_FILTER_NEAREST);
      g_fb.framebufferReadUnbind();

      final JCGLTextureUnitContextType tc = uc.unitContextNew();

      try {
        JCGLRenderStates.activate(this.g, this.render_state);
        g_v.viewportSet(viewport);

        try {
          g_ao.arrayObjectBind(this.quad.arrayObject());

          this.values.setValues(parameters);
          this.values.setTextureUnitContext(tc);

          this.shader.onActivate(this.g);
          this.shader.onReceiveFilterValues(this.g, this.values);
          this.shader.onValidate();

          final R2MatricesObserverType m = parameters.observerValues();

          m.withTransform(
            R2TransformIdentity.get(),
            PMatrices3x3D.identity(),
            this,
            (mi, t) -> {
              g_dr.drawElements(JCGLPrimitives.PRIMITIVE_TRIANGLES);
              return Unit.unit();
            });

        } finally {
          g_ao.arrayObjectUnbind();
          g_sh.shaderDeactivateProgram();
        }

      } finally {
        tc.unitContextFinish(g_tex);
      }

    } finally {
      g_fb.framebufferDrawUnbind();
    }
  }
}
