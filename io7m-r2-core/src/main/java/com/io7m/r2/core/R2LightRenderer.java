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
import com.io7m.jcanephora.core.JCGLBlendFunction;
import com.io7m.jcanephora.core.JCGLDepthFunction;
import com.io7m.jcanephora.core.JCGLFaceSelection;
import com.io7m.jcanephora.core.JCGLFaceWindingOrder;
import com.io7m.jcanephora.core.JCGLFramebufferBlitBuffer;
import com.io7m.jcanephora.core.JCGLFramebufferBlitFilter;
import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.JCGLPrimitives;
import com.io7m.jcanephora.core.JCGLStencilFunction;
import com.io7m.jcanephora.core.JCGLStencilOperation;
import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.api.JCGLArrayObjectsType;
import com.io7m.jcanephora.core.api.JCGLBlendingType;
import com.io7m.jcanephora.core.api.JCGLColorBufferMaskingType;
import com.io7m.jcanephora.core.api.JCGLCullingType;
import com.io7m.jcanephora.core.api.JCGLDepthBuffersType;
import com.io7m.jcanephora.core.api.JCGLDrawType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLStencilBuffersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import org.valid4j.Assertive;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The default implementation of the {@link R2LightRendererType} interface.
 */

public final class R2LightRenderer implements R2LightRendererType
{
  private static final Set<JCGLFramebufferBlitBuffer> DEPTH_STENCIL;

  static {
    DEPTH_STENCIL = EnumSet.of(
      JCGLFramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_DEPTH,
      JCGLFramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_STENCIL);
  }

  private final LightConsumer light_consumer;
  private       boolean       deleted;

  private R2LightRenderer()
  {
    this.light_consumer = new LightConsumer();
  }

  /**
   * @return A new renderer
   */

  public static R2LightRendererType newRenderer()
  {
    return new R2LightRenderer();
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
  public void renderLights(
    final JCGLInterfaceGL33Type g,
    final R2GeometryBufferUsableType gbuffer,
    final R2LightBufferUsableType lbuffer,
    final R2MatricesObserverType m,
    final R2SceneOpaqueLightsType s)
  {
    NullCheck.notNull(g);
    NullCheck.notNull(gbuffer);
    NullCheck.notNull(lbuffer);
    NullCheck.notNull(m);
    NullCheck.notNull(s);

    Assertive.require(!this.isDeleted(), "Renderer not deleted");

    final JCGLFramebufferUsableType lb_fb = lbuffer.getFramebuffer();
    final JCGLFramebuffersType g_fb = g.getFramebuffers();

    try {
      g_fb.framebufferDrawBind(lb_fb);
      this.renderLightsWithBoundBuffer(g, gbuffer, lbuffer.getArea(), m, s);
    } finally {
      g_fb.framebufferDrawUnbind();
    }
  }

  @Override
  public void renderLightsWithBoundBuffer(
    final JCGLInterfaceGL33Type g,
    final R2GeometryBufferUsableType gbuffer,
    final AreaInclusiveUnsignedLType lbuffer_area,
    final R2MatricesObserverType m,
    final R2SceneOpaqueLightsType s)
  {
    NullCheck.notNull(g);
    NullCheck.notNull(gbuffer);
    NullCheck.notNull(lbuffer_area);
    NullCheck.notNull(m);
    NullCheck.notNull(s);

    Assertive.require(!this.isDeleted(), "Renderer not deleted");

    final JCGLFramebufferUsableType gb_fb = gbuffer.getFramebuffer();
    final JCGLFramebuffersType g_fb = g.getFramebuffers();

    /**
     * Copy the contents of the depth/stencil attachment of the G-Buffer to
     * the current depth/stencil buffer.
     */

    g_fb.framebufferReadBind(gb_fb);
    g_fb.framebufferBlit(
      gbuffer.getArea(),
      lbuffer_area,
      R2LightRenderer.DEPTH_STENCIL,
      JCGLFramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST);
    g_fb.framebufferReadUnbind();

    if (s.opaqueLightsCount() > 0L) {

      /**
       * Configure state for light geometry rendering.
       */

      final JCGLDepthBuffersType g_db = g.getDepthBuffers();
      final JCGLBlendingType g_b = g.getBlending();
      final JCGLColorBufferMaskingType g_cm = g.getColorBufferMasking();

      g_b.blendingEnable(
        JCGLBlendFunction.BLEND_ONE, JCGLBlendFunction.BLEND_ONE);
      g_cm.colorBufferMask(true, true, true, true);
      g_db.depthClampingEnable();
      g_db.depthBufferWriteDisable();
      g_db.depthBufferTestEnable(JCGLDepthFunction.DEPTH_GREATER_THAN_OR_EQUAL);

      this.light_consumer.g33 = g;
      this.light_consumer.gbuffer = gbuffer;
      this.light_consumer.matrices = m;
      try {
        s.opaqueLightsExecute(this.light_consumer);
      } finally {
        this.light_consumer.matrices = null;
        this.light_consumer.gbuffer = null;
        this.light_consumer.g33 = null;
      }
    }
  }

  private static final class LightConsumer implements
    R2SceneOpaqueLightsConsumerType
  {
    private @Nullable JCGLInterfaceGL33Type      g33;
    private @Nullable JCGLCullingType            culling;
    private @Nullable R2MatricesObserverType     matrices;
    private @Nullable JCGLShadersType            shaders;
    private @Nullable JCGLTexturesType           textures;
    private @Nullable JCGLArrayObjectsType       array_objects;
    private @Nullable JCGLDrawType               draw;
    private @Nullable JCGLStencilBuffersType     stencils;
    private @Nullable R2GeometryBufferUsableType gbuffer;
    private           JCGLTextureUnitType        unit_albedo;
    private           JCGLTextureUnitType        unit_normals;
    private           JCGLTextureUnitType        unit_specular;
    private           JCGLTextureUnitType        unit_depth;

    LightConsumer()
    {

    }

    @Override
    public void onStart()
    {
      Assertive.require(this.g33 != null);

      this.shaders = this.g33.getShaders();
      this.textures = this.g33.getTextures();
      this.array_objects = this.g33.getArrayObjects();
      this.draw = this.g33.getDraw();
      this.stencils = this.g33.getStencilBuffers();
      this.culling = this.g33.getCulling();

      final List<JCGLTextureUnitType> units = this.textures.textureGetUnits();
      this.unit_albedo = units.get(0);
      this.unit_normals = units.get(1);
      this.unit_specular = units.get(2);
      this.unit_depth = units.get(3);

      this.textures.texture2DBind(
        this.unit_albedo, this.gbuffer.getAlbedoEmissiveTexture().get());
      this.textures.texture2DBind(
        this.unit_normals, this.gbuffer.getNormalTexture().get());
      this.textures.texture2DBind(
        this.unit_depth, this.gbuffer.getDepthTexture().get());
      this.textures.texture2DBind(
        this.unit_specular, this.gbuffer.getSpecularTexture().get());
    }

    @Override
    public void onFinish()
    {
      this.textures.textureUnitUnbind(this.unit_albedo);
      this.textures.textureUnitUnbind(this.unit_normals);
      this.textures.textureUnitUnbind(this.unit_depth);
      this.textures.textureUnitUnbind(this.unit_specular);

      this.array_objects = null;
      this.shaders = null;
      this.draw = null;
      this.textures = null;
    }

    @Override
    public void onStartGroup(final int group)
    {
      this.stencils.stencilBufferEnable();
      this.stencils.stencilBufferOperation(
        JCGLFaceSelection.FACE_FRONT_AND_BACK,
        JCGLStencilOperation.STENCIL_OP_KEEP,
        JCGLStencilOperation.STENCIL_OP_KEEP,
        JCGLStencilOperation.STENCIL_OP_KEEP);
      this.stencils.stencilBufferMask(
        JCGLFaceSelection.FACE_FRONT_AND_BACK,
        0);
      this.stencils.stencilBufferFunction(
        JCGLFaceSelection.FACE_FRONT_AND_BACK,
        JCGLStencilFunction.STENCIL_EQUAL,
        group,
        R2Stencils.GROUP_BITS);
    }

    @Override
    public <M extends R2LightSingleType> void onLightSingleShaderStart(
      final R2ShaderLightSingleUsableType<M> s)
    {
      this.shaders.shaderActivateProgram(s.getShaderProgram());
      s.setGBuffer(
        this.shaders,
        this.textures,
        this.gbuffer,
        this.unit_albedo,
        this.unit_specular,
        this.unit_depth,
        this.unit_normals);
    }

    @Override
    public void onLightSingleArrayStart(final R2LightSingleType i)
    {
      this.array_objects.arrayObjectBind(i.getArrayObject());
    }

    @Override
    public <M extends R2LightSingleType> void onLightSingle(
      final R2ShaderLightSingleUsableType<M> s,
      final M i)
    {
      /**
       * For full-screen quads, the front faces should be rendered. For
       * everything else, render only back faces.
       */

      if (i instanceof R2LightScreenSingleType) {
        this.culling.cullingEnable(
          JCGLFaceSelection.FACE_BACK,
          JCGLFaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
      } else {
        this.culling.cullingEnable(
          JCGLFaceSelection.FACE_FRONT,
          JCGLFaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
      }

      s.setLightViewDependentValues(this.shaders, this.matrices, i);
      s.setLightValues(this.shaders, this.textures, i);

      final R2TransformReadableType tr = R2TransformOST.newTransform();
      this.matrices.withTransform(tr, PMatrixI3x3F.identity(), mi -> {
        this.draw.drawElements(JCGLPrimitives.PRIMITIVE_TRIANGLES);
        return Unit.unit();
      });
    }

    @Override
    public <M extends R2LightSingleType> void onLightSingleShaderFinish(
      final R2ShaderLightSingleUsableType<M> s)
    {
      this.shaders.shaderDeactivateProgram();
    }

    @Override
    public void onFinishGroup(final int group)
    {

    }
  }
}