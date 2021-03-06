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

package com.io7m.r2.rendering.mask;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jcanephora.core.JCGLDepthFunction;
import com.io7m.jcanephora.core.JCGLFaceSelection;
import com.io7m.jcanephora.core.JCGLFaceWindingOrder;
import com.io7m.jcanephora.core.JCGLPrimitives;
import com.io7m.jcanephora.core.api.JCGLArrayObjectsType;
import com.io7m.jcanephora.core.api.JCGLDrawType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.core.api.JCGLViewportsType;
import com.io7m.jcanephora.profiler.JCGLProfilingContextType;
import com.io7m.jcanephora.renderstate.JCGLCullingState;
import com.io7m.jcanephora.renderstate.JCGLDepthClamping;
import com.io7m.jcanephora.renderstate.JCGLDepthState;
import com.io7m.jcanephora.renderstate.JCGLDepthStrict;
import com.io7m.jcanephora.renderstate.JCGLDepthWriting;
import com.io7m.jcanephora.renderstate.JCGLRenderState;
import com.io7m.jcanephora.renderstate.JCGLRenderStates;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextMutableType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jregions.core.unparameterized.areas.AreaL;
import com.io7m.jtensors.core.parameterized.matrices.PMatrices3x3D;
import com.io7m.r2.core.api.R2Exception;
import com.io7m.r2.core.api.ids.R2IDPoolType;
import com.io7m.r2.instances.R2InstanceBatchedType;
import com.io7m.r2.instances.R2InstanceSingleType;
import com.io7m.r2.matrices.R2MatricesObserverType;
import com.io7m.r2.matrices.R2MatricesValuesType;
import com.io7m.r2.rendering.mask.api.R2MaskBufferUsableType;
import com.io7m.r2.rendering.mask.api.R2MaskInstancesType;
import com.io7m.r2.rendering.mask.api.R2MaskRendererType;
import com.io7m.r2.shaders.api.R2ShaderInstanceBatchedType;
import com.io7m.r2.shaders.api.R2ShaderInstanceSingleType;
import com.io7m.r2.shaders.api.R2ShaderParametersMaterialMutable;
import com.io7m.r2.shaders.api.R2ShaderParametersMaterialType;
import com.io7m.r2.shaders.api.R2ShaderParametersViewMutable;
import com.io7m.r2.shaders.api.R2ShaderParametersViewType;
import com.io7m.r2.shaders.api.R2ShaderPreprocessingEnvironmentReadableType;
import com.io7m.r2.shaders.mask.R2MaskShaderBatched;
import com.io7m.r2.shaders.mask.R2MaskShaderSingle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * The default implementation of the {@link R2MaskRendererType} interface.
 */

public final class R2MaskRenderer implements R2MaskRendererType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(R2MaskRenderer.class);
  }

  private final JCGLInterfaceGL33Type g;
  private final JCGLRenderState render_state;
  private final R2ShaderInstanceSingleType<Unit> shader_single;
  private final R2ShaderInstanceBatchedType<Unit> shader_batched;
  private final R2ShaderParametersViewMutable params_view;
  private final R2ShaderParametersMaterialMutable<Object> params_material;
  private boolean deleted;
  private @Nullable AreaL viewport;
  private @Nullable R2MatricesObserverType matrices;

  private R2MaskRenderer(
    final JCGLInterfaceGL33Type in_g,
    final R2ShaderInstanceSingleType<Unit> in_shader_single,
    final R2ShaderInstanceBatchedType<Unit> in_shader_batched)
  {
    this.g =
      NullCheck.notNull(in_g, "G33");
    this.shader_single =
      NullCheck.notNull(in_shader_single, "Shader single");
    this.shader_batched =
      NullCheck.notNull(in_shader_batched, "Shader batched");

    {
      final JCGLRenderState.Builder b = JCGLRenderState.builder();

      /*
       * Only front faces are rendered.
       */

      b.setCullingState(Optional.of(JCGLCullingState.of(
        JCGLFaceSelection.FACE_BACK,
        JCGLFaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE)));

      /*
       * Enable depth testing and clamping.
       */

      b.setDepthState(JCGLDepthState.of(
        JCGLDepthStrict.DEPTH_STRICT_ENABLED,
        Optional.of(JCGLDepthFunction.DEPTH_LESS_THAN),
        JCGLDepthWriting.DEPTH_WRITE_DISABLED,
        JCGLDepthClamping.DEPTH_CLAMP_ENABLED));

      this.render_state = b.build();
    }

    this.params_view = R2ShaderParametersViewMutable.create();
    this.params_material = R2ShaderParametersMaterialMutable.create();
  }

  /**
   * Construct a new renderer.
   *
   * @param in_g          An OpenGL interface
   * @param in_shader_env A shader preprocessing environment
   * @param in_pool       An ID pool
   *
   * @return A new renderer
   */

  public static R2MaskRenderer create(
    final JCGLInterfaceGL33Type in_g,
    final R2ShaderPreprocessingEnvironmentReadableType in_shader_env,
    final R2IDPoolType in_pool)
  {
    final R2ShaderInstanceSingleType<Unit> shader_single =
      R2MaskShaderSingle.create(in_g.shaders(), in_shader_env, in_pool);
    final R2ShaderInstanceBatchedType<Unit> shader_batched =
      R2MaskShaderBatched.create(in_g.shaders(), in_shader_env, in_pool);
    return new R2MaskRenderer(in_g, shader_single, shader_batched);
  }

  @Override
  public void renderMask(
    final AreaL area,
    final Optional<R2MaskBufferUsableType> mbuffer,
    final JCGLProfilingContextType pc,
    final JCGLTextureUnitContextParentType tucp,
    final R2MatricesObserverType m,
    final R2MaskInstancesType s)
  {
    NullCheck.notNull(area, "Area");
    NullCheck.notNull(mbuffer, "Mask buffer");
    NullCheck.notNull(pc, "Profiling");
    NullCheck.notNull(tucp, "Texture context");
    NullCheck.notNull(m, "Matrices");
    NullCheck.notNull(s, "Instances");

    Preconditions.checkPrecondition(
      !this.isDeleted(), "Renderer must not be deleted");

    final JCGLProfilingContextType p_mask = pc.childContext("mask");
    p_mask.startMeasuringIfEnabled();
    try {
      final JCGLFramebuffersType g_fb = this.g.framebuffers();
      final JCGLViewportsType g_v = this.g.viewports();

      mbuffer.ifPresent(mbu -> g_fb.framebufferDrawBind(mbu.primaryFramebuffer()));

      g_v.viewportSet(area);
      this.viewport = area;
      this.matrices = m;

      JCGLRenderStates.activate(this.g, this.render_state);
      final JCGLTexturesType g_tx = this.g.textures();
      final JCGLTextureUnitContextType up = tucp.unitContextNew();
      try {
        this.renderSingles(m, s, up);
        this.renderBatches(m, s, up);
      } finally {
        up.unitContextFinish(g_tx);
      }

    } finally {
      p_mask.stopMeasuringIfEnabled();
      this.viewport = null;
      this.matrices = null;
    }
  }

  @SuppressWarnings("unchecked")
  private <M> R2ShaderParametersMaterialType<M> configureMaterialParameters(
    final JCGLTextureUnitContextMutableType tc,
    final M p)
  {
    this.params_material.clear();
    this.params_material.setTextureUnitContext(tc);
    this.params_material.setValues(p);
    Invariants.checkInvariant(
      this.params_material.isInitialized(),
      "Material parameters must be initialized");
    return (R2ShaderParametersMaterialType<M>) this.params_material;
  }

  private R2ShaderParametersViewType configureViewParameters()
  {
    this.params_view.clear();
    this.params_view.setViewport(this.viewport);
    this.params_view.setObserverMatrices(this.matrices);
    Invariants.checkInvariant(
      this.params_view.isInitialized(),
      "View parameters must be initialized");
    return this.params_view;
  }

  private void renderBatches(
    final R2MatricesValuesType m,
    final R2MaskInstancesType s,
    final JCGLTextureUnitContextType up)
  {
    final JCGLArrayObjectsType g_a = this.g.arrayObjects();
    final JCGLDrawType g_d = this.g.drawing();

    final List<R2InstanceBatchedType> batches = s.batched();
    if (!batches.isEmpty()) {
      this.shader_batched.onActivate(this.g);
      try {
        this.shader_batched.onReceiveViewValues(
          this.g, this.configureViewParameters());
        this.shader_batched.onReceiveMaterialValues(
          this.g, this.configureMaterialParameters(up, Unit.unit()));

        for (int index = 0; index < batches.size(); ++index) {
          final R2InstanceBatchedType instance = batches.get(index);
          if (instance.updateRequired()) {
            instance.update(this.g);
          }
          g_a.arrayObjectBind(instance.arrayObject());

          this.shader_batched.onValidate();
          g_d.drawElementsInstanced(
            JCGLPrimitives.PRIMITIVE_TRIANGLES, instance.renderCount());
        }
      } finally {
        this.shader_batched.onDeactivate(this.g);
        g_a.arrayObjectUnbind();
      }
    }
  }

  private void renderSingles(
    final R2MatricesObserverType m,
    final R2MaskInstancesType s,
    final JCGLTextureUnitContextType up)
  {
    final JCGLArrayObjectsType g_a = this.g.arrayObjects();

    final List<R2InstanceSingleType> singles = s.singles();
    if (!singles.isEmpty()) {
      this.shader_single.onActivate(this.g);
      try {
        this.shader_single.onReceiveViewValues(
          this.g, this.configureViewParameters());
        this.shader_single.onReceiveMaterialValues(
          this.g, this.configureMaterialParameters(up, Unit.unit()));

        for (int index = 0; index < singles.size(); ++index) {
          final R2InstanceSingleType instance = singles.get(index);
          g_a.arrayObjectBind(instance.arrayObject());
          m.withTransform(
            instance.transform(),
            PMatrices3x3D.identity(),
            this,
            (mi, tt) -> {
              tt.shader_single.onReceiveInstanceTransformValues(tt.g, mi);
              tt.shader_single.onValidate();
              tt.g.drawing().drawElements(
                JCGLPrimitives.PRIMITIVE_TRIANGLES);
              return Unit.unit();
            });
        }
      } finally {
        this.shader_single.onDeactivate(this.g);
        g_a.arrayObjectUnbind();
      }
    }
  }

  @Override
  public void delete(final JCGLInterfaceGL33Type g3)
    throws R2Exception
  {
    LOG.debug("delete");

    if (!this.shader_single.isDeleted()) {
      try {
        this.shader_single.delete(this.g);
        this.shader_batched.delete(this.g);
      } finally {
        this.deleted = true;
      }
    }
  }

  @Override
  public boolean isDeleted()
  {
    return this.deleted;
  }
}
