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

package com.io7m.r2.core;

import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.jcanephora.core.JCGLClearSpecification;
import com.io7m.jcanephora.core.JCGLDepthFunction;
import com.io7m.jcanephora.core.JCGLFaceSelection;
import com.io7m.jcanephora.core.JCGLFaceWindingOrder;
import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.JCGLPrimitives;
import com.io7m.jcanephora.core.api.JCGLArrayObjectsType;
import com.io7m.jcanephora.core.api.JCGLDrawType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.core.api.JCGLViewportsType;
import com.io7m.jcanephora.renderstate.JCGLColorBufferMaskingState;
import com.io7m.jcanephora.renderstate.JCGLCullingState;
import com.io7m.jcanephora.renderstate.JCGLCullingStateType;
import com.io7m.jcanephora.renderstate.JCGLDepthClamping;
import com.io7m.jcanephora.renderstate.JCGLDepthState;
import com.io7m.jcanephora.renderstate.JCGLDepthStrict;
import com.io7m.jcanephora.renderstate.JCGLDepthWriting;
import com.io7m.jcanephora.renderstate.JCGLRenderStateMutable;
import com.io7m.jcanephora.renderstate.JCGLRenderStates;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.VectorI4F;
import com.io7m.r2.core.shaders.types.R2ShaderDepthBatchedUsableType;
import com.io7m.r2.core.shaders.types.R2ShaderDepthSingleUsableType;
import org.valid4j.Assertive;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * The default implementation of the {@link R2DepthRendererType} interface.
 */

public final class R2DepthVarianceRenderer implements
  R2DepthVarianceRendererType
{
  private final DepthConsumer depth_consumer;
  private final JCGLInterfaceGL33Type g;
  private final JCGLClearSpecification clear;
  private boolean deleted;

  private R2DepthVarianceRenderer(final JCGLInterfaceGL33Type in_g)
  {
    this.g = NullCheck.notNull(in_g);
    this.depth_consumer = new DepthConsumer(this.g);
    this.clear = JCGLClearSpecification.of(
      Optional.of(new VectorI4F(1.0f, 1.0f, 1.0f, 1.0f)),
      OptionalDouble.of(1.0),
      OptionalInt.empty(),
      true);
  }

  /**
   * Construct a new renderer.
   *
   * @param in_g An OpenGL interface
   *
   * @return A new renderer
   */

  public static R2DepthVarianceRendererType newRenderer(
    final JCGLInterfaceGL33Type in_g)
  {
    return new R2DepthVarianceRenderer(in_g);
  }

  @Override
  public void delete(final JCGLInterfaceGL33Type g3)
    throws R2Exception
  {
    this.deleted = true;
  }

  @Override
  public boolean isDeleted()
  {
    return this.deleted;
  }

  @Override
  public void renderDepthVariance(
    final R2DepthVarianceBufferUsableType dbuffer,
    final JCGLTextureUnitContextParentType uc,
    final R2MatricesObserverType m,
    final R2DepthInstancesType s)
  {
    NullCheck.notNull(dbuffer);
    NullCheck.notNull(uc);
    NullCheck.notNull(m);
    NullCheck.notNull(s);

    Assertive.require(!this.isDeleted(), "Renderer not deleted");

    final JCGLFramebufferUsableType gb_fb = dbuffer.getPrimaryFramebuffer();
    final JCGLFramebuffersType g_fb = this.g.getFramebuffers();

    try {
      g_fb.framebufferDrawBind(gb_fb);
      this.renderDepthVarianceWithBoundBuffer(dbuffer.getArea(), uc, m, s);
    } finally {
      g_fb.framebufferDrawUnbind();
    }
  }

  @Override
  public void renderDepthVarianceWithBoundBuffer(
    final AreaInclusiveUnsignedLType area,
    final JCGLTextureUnitContextParentType uc,
    final R2MatricesObserverType m,
    final R2DepthInstancesType s)
  {
    NullCheck.notNull(uc);
    NullCheck.notNull(m);
    NullCheck.notNull(s);

    Assertive.require(!this.isDeleted(), "Renderer not deleted");

    final JCGLFramebuffersType g_fb = this.g.getFramebuffers();
    Assertive.require(g_fb.framebufferDrawAnyIsBound());
    final JCGLViewportsType g_v = this.g.getViewports();

    if (s.depthsCount() > 0L) {
      g_v.viewportSet(area);

      this.depth_consumer.matrices = m;
      this.depth_consumer.texture_context = uc;
      this.depth_consumer.culling = s.depthsGetFaceCulling();
      try {
        s.depthsExecute(this.depth_consumer);
      } finally {
        this.depth_consumer.texture_context = null;
        this.depth_consumer.matrices = null;
      }
    }
  }

  private static final class DepthConsumer implements
    R2DepthInstancesConsumerType
  {
    private static final Optional<JCGLCullingStateType> CULL_BACK;
    private static final Optional<JCGLCullingStateType> CULL_FRONT;
    private static final Optional<JCGLCullingStateType> CULL_BOTH;

    static {
      CULL_BACK = Optional.of(
        JCGLCullingState.of(
          JCGLFaceSelection.FACE_BACK,
          JCGLFaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE));

      CULL_FRONT = Optional.of(
        JCGLCullingState.of(
          JCGLFaceSelection.FACE_FRONT,
          JCGLFaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE));

      CULL_BOTH = Optional.of(
        JCGLCullingState.of(
          JCGLFaceSelection.FACE_FRONT_AND_BACK,
          JCGLFaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE));
    }

    private final JCGLInterfaceGL33Type g33;
    private final JCGLShadersType shaders;
    private final JCGLTexturesType textures;
    private final JCGLArrayObjectsType array_objects;
    private final JCGLDrawType draw;
    private final JCGLRenderStateMutable render_state;

    private @Nullable R2MatricesObserverType matrices;
    private @Nullable JCGLTextureUnitContextParentType texture_context;
    private @Nullable JCGLTextureUnitContextType material_texture_context;
    private @Nullable R2MaterialDepthSingleType<?> material_single;
    private @Nullable JCGLFaceSelection culling;

    private DepthConsumer(
      final JCGLInterfaceGL33Type ig)
    {
      this.g33 = NullCheck.notNull(ig);
      this.shaders = this.g33.getShaders();
      this.textures = this.g33.getTextures();
      this.array_objects = this.g33.getArrayObjects();
      this.draw = this.g33.getDraw();

      {
        this.render_state = JCGLRenderStateMutable.create();

        /**
         * Enable color buffer rendering to the first two channels.
         */

        this.render_state.setColorBufferMaskingState(
          JCGLColorBufferMaskingState.of(true, true, false, false));

        /**
         * Enable depth testing, writing, and clamping.
         */

        this.render_state.setDepthState(JCGLDepthState.of(
          JCGLDepthStrict.DEPTH_STRICT_ENABLED,
          Optional.of(JCGLDepthFunction.DEPTH_LESS_THAN),
          JCGLDepthWriting.DEPTH_WRITE_ENABLED,
          JCGLDepthClamping.DEPTH_CLAMP_ENABLED
        ));
      }
    }

    @Override
    public void onStart()
    {
      Assertive.require(this.g33 != null);

      switch (this.culling) {
        case FACE_BACK:
          this.render_state.setCullingState(DepthConsumer.CULL_BACK);
          break;
        case FACE_FRONT:
          this.render_state.setCullingState(DepthConsumer.CULL_FRONT);
          break;
        case FACE_FRONT_AND_BACK:
          this.render_state.setCullingState(DepthConsumer.CULL_BOTH);
          break;
      }

      JCGLRenderStates.activate(this.g33, this.render_state);
    }

    @Override
    public void onInstanceBatchedUpdate(
      final R2InstanceBatchedType i)
    {
      i.update(this.g33, this.matrices.getTransformContext());
    }

    @Override
    public <M> void onInstanceBatchedShaderStart(
      final R2ShaderDepthBatchedUsableType<M> s)
    {
      this.shaders.shaderActivateProgram(s.getShaderProgram());
      s.onReceiveViewValues(this.shaders, this.matrices);
    }

    @Override
    public <M> void onInstanceBatchedMaterialStart(
      final R2MaterialDepthBatchedType<M> material)
    {
      this.material_texture_context = this.texture_context.unitContextNew();

      final R2ShaderDepthBatchedUsableType<M> s = material.getShader();
      final M p = material.getShaderParameters();
      s.onReceiveMaterialValues(
        this.textures, this.shaders, this.material_texture_context, p);
    }

    @Override
    public <M> void onInstanceBatched(
      final R2MaterialDepthBatchedType<M> material,
      final R2InstanceBatchedType i)
    {
      material.getShader().onValidate();

      this.array_objects.arrayObjectBind(i.getArrayObject());
      this.draw.drawElementsInstanced(
        JCGLPrimitives.PRIMITIVE_TRIANGLES, i.getRenderCount());
    }

    @Override
    public <M> void onInstanceBatchedMaterialFinish(
      final R2MaterialDepthBatchedType<M> material)
    {
      this.material_texture_context.unitContextFinish(this.textures);
      this.material_texture_context = null;
    }

    @Override
    public <M> void onInstanceBatchedShaderFinish(
      final R2ShaderDepthBatchedUsableType<M> s)
    {
      s.onDeactivate(this.shaders);
    }

    @Override
    public <M> void onInstanceSingleShaderStart(
      final R2ShaderDepthSingleUsableType<M> s)
    {
      s.onActivate(this.shaders);
      s.onReceiveViewValues(this.shaders, this.matrices);
    }

    @Override
    public <M> void onInstanceSingleMaterialStart(
      final R2MaterialDepthSingleType<M> material)
    {
      this.material_single = material;
      this.material_texture_context = this.texture_context.unitContextNew();

      final R2ShaderDepthSingleUsableType<M> s = material.getShader();
      final M p = material.getShaderParameters();

      s.onReceiveMaterialValues(
        this.textures, this.shaders, this.material_texture_context, p);
    }

    @Override
    public void onInstanceSingleArrayStart(
      final R2InstanceSingleType i)
    {
      this.array_objects.arrayObjectBind(i.getArrayObject());
    }

    @Override
    public <M> void onInstanceSingle(
      final R2MaterialDepthSingleType<M> material,
      final R2InstanceSingleType i)
    {
      this.matrices.withTransform(
        i.getTransform(),
        i.getUVMatrix(),
        this,
        (mi, t) -> {
          final R2ShaderDepthSingleUsableType<?> s =
            t.material_single.getShader();
          s.onReceiveInstanceTransformValues(t.shaders, mi);
          s.onValidate();

          t.draw.drawElements(JCGLPrimitives.PRIMITIVE_TRIANGLES);
          return Unit.unit();
        });
    }

    @Override
    public <M> void onInstanceSingleMaterialFinish(
      final R2MaterialDepthSingleType<M> material)
    {
      this.material_texture_context.unitContextFinish(this.textures);
      this.material_texture_context = null;
    }

    @Override
    public <M> void onInstanceSingleShaderFinish(
      final R2ShaderDepthSingleUsableType<M> s)
    {
      s.onDeactivate(this.shaders);
    }

    @Override
    public void onFinish()
    {
      this.material_single = null;
    }
  }
}