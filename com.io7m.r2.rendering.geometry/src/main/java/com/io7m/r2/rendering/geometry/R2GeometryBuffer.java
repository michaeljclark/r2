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

package com.io7m.r2.rendering.geometry;

import com.io7m.jcanephora.core.JCGLClearSpecification;
import com.io7m.jcanephora.core.JCGLFramebufferBuilderType;
import com.io7m.jcanephora.core.JCGLFramebufferColorAttachmentPointType;
import com.io7m.jcanephora.core.JCGLFramebufferDrawBufferType;
import com.io7m.jcanephora.core.JCGLFramebufferType;
import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.JCGLStencilFunction;
import com.io7m.jcanephora.core.JCGLStencilOperation;
import com.io7m.jcanephora.core.JCGLTexture2DType;
import com.io7m.jcanephora.core.JCGLTextureFilterMagnification;
import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.JCGLTextureWrapT;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.renderstate.JCGLColorBufferMaskingState;
import com.io7m.jcanephora.renderstate.JCGLDepthClamping;
import com.io7m.jcanephora.renderstate.JCGLDepthState;
import com.io7m.jcanephora.renderstate.JCGLDepthStrict;
import com.io7m.jcanephora.renderstate.JCGLDepthWriting;
import com.io7m.jcanephora.renderstate.JCGLRenderState;
import com.io7m.jcanephora.renderstate.JCGLRenderStates;
import com.io7m.jcanephora.renderstate.JCGLStencilState;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jfunctional.Pair;
import com.io7m.jnull.NullCheck;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizeL;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import com.io7m.junsigned.ranges.UnsignedRangeInclusiveL;
import com.io7m.r2.core.api.R2Exception;
import com.io7m.r2.rendering.geometry.api.R2GeometryBufferDescription;
import com.io7m.r2.rendering.geometry.api.R2GeometryBufferType;
import com.io7m.r2.textures.R2Texture2DStatic;
import com.io7m.r2.textures.R2Texture2DType;
import com.io7m.r2.textures.R2Texture2DUsableType;
import com.io7m.r2.rendering.api.R2RendererExceptionFramebufferNotBound;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static com.io7m.jcanephora.core.JCGLTextureFilterMinification.TEXTURE_FILTER_LINEAR;
import static com.io7m.jcanephora.core.JCGLTextureFormat.TEXTURE_FORMAT_DEPTH_24_STENCIL_8_4BPP;
import static com.io7m.jcanephora.core.JCGLTextureFormat.TEXTURE_FORMAT_RGBA_8_4BPP;
import static com.io7m.jcanephora.core.JCGLTextureFormat.TEXTURE_FORMAT_RG_16F_4BPP;
import static com.io7m.jcanephora.core.JCGLTextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE;
import static com.io7m.r2.rendering.geometry.api.R2GeometryBufferComponents.R2_GEOMETRY_BUFFER_FULL;
import static com.io7m.r2.rendering.geometry.api.R2GeometryBufferComponents.R2_GEOMETRY_BUFFER_NO_SPECULAR;

/**
 * Default implementation of the {@link R2GeometryBufferType} interface.
 */

public final class R2GeometryBuffer implements R2GeometryBufferType
{
  private static final JCGLRenderState CLEAR_STATE;
  private static final JCGLClearSpecification CLEAR_SPEC;

  static {
    CLEAR_STATE =
      JCGLRenderState.builder()
        .setDepthState(JCGLDepthState.of(
          JCGLDepthStrict.DEPTH_STRICT_ENABLED,
          Optional.empty(),
          JCGLDepthWriting.DEPTH_WRITE_ENABLED,
          JCGLDepthClamping.DEPTH_CLAMP_ENABLED
        ))
        .setColorBufferMaskingState(
          JCGLColorBufferMaskingState.of(true, true, true, true))
        .setStencilState(JCGLStencilState.of(
          true,
          true,
          JCGLStencilFunction.STENCIL_ALWAYS,
          JCGLStencilFunction.STENCIL_ALWAYS,
          0,
          0,
          0,
          0,
          JCGLStencilOperation.STENCIL_OP_KEEP,
          JCGLStencilOperation.STENCIL_OP_KEEP,
          JCGLStencilOperation.STENCIL_OP_KEEP,
          JCGLStencilOperation.STENCIL_OP_KEEP,
          JCGLStencilOperation.STENCIL_OP_KEEP,
          JCGLStencilOperation.STENCIL_OP_KEEP,
          0b11111111,
          0b11111111
        )).build();

    CLEAR_SPEC = JCGLClearSpecification.of(
      Optional.of(Vector4D.of(0.0, 0.0, 0.0, 0.0)),
      OptionalDouble.of(1.0),
      OptionalInt.of(0),
      true);
  }

  private final R2Texture2DType t_rgba;
  private final R2Texture2DType t_norm;
  private final R2Texture2DType t_spec;
  private final Optional<R2Texture2DUsableType> t_spec_opt;
  private final R2Texture2DType t_depth;
  private final JCGLFramebufferType framebuffer;
  private final UnsignedRangeInclusiveL range;
  private final R2GeometryBufferDescription desc;

  private R2GeometryBuffer(
    final JCGLFramebufferType in_framebuffer,
    final R2GeometryBufferDescription in_desc,
    final R2Texture2DType in_t_rgba,
    final R2Texture2DType in_t_norm,
    final R2Texture2DType in_t_spec,
    final R2Texture2DType in_t_depth)
  {
    this.framebuffer = NullCheck.notNull(in_framebuffer, "Framebuffer");
    this.desc = NullCheck.notNull(in_desc, "Description");
    this.t_rgba = NullCheck.notNull(in_t_rgba, "RGBA");
    this.t_norm = NullCheck.notNull(in_t_norm, "Normals");
    this.t_depth = NullCheck.notNull(in_t_depth, "Depth");

    if (in_t_spec != null) {
      this.t_spec = in_t_spec;
      this.t_spec_opt = Optional.of(in_t_spec);
    } else {
      this.t_spec = null;
      this.t_spec_opt = Optional.empty();
    }

    long size = 0L;
    size += this.t_rgba.texture().byteRange().getInterval();
    size += this.t_norm.texture().byteRange().getInterval();

    if (this.t_spec != null) {
      size += this.t_spec.texture().byteRange().getInterval();
    }

    size += this.t_depth.texture().byteRange().getInterval();
    this.range = new UnsignedRangeInclusiveL(0L, size - 1L);
  }

  /**
   * Construct a new geometry buffer.
   *
   * @param g_fb A framebuffer interface
   * @param g_t  A texture interface
   * @param tc   A texture unit context
   * @param desc The geometry buffer description
   *
   * @return A new geometry buffer
   */

  public static R2GeometryBuffer create(
    final JCGLFramebuffersType g_fb,
    final JCGLTexturesType g_t,
    final JCGLTextureUnitContextParentType tc,
    final R2GeometryBufferDescription desc)
  {
    NullCheck.notNull(g_fb, "Framebuffers");
    NullCheck.notNull(g_t, "Textures");
    NullCheck.notNull(tc, "Texture context");
    NullCheck.notNull(desc, "Description");

    final List<JCGLFramebufferColorAttachmentPointType> points =
      g_fb.framebufferGetColorAttachments();
    final List<JCGLFramebufferDrawBufferType> buffers =
      g_fb.framebufferGetDrawBuffers();

    final JCGLTextureUnitContextType cc = tc.unitContextNewWithReserved(4);
    try {
      final AreaSizeL area = desc.area();

      final Pair<JCGLTextureUnitType, JCGLTexture2DType> p_rgba =
        cc.unitContextAllocateTexture2D(
          g_t,
          area.sizeX(),
          area.sizeY(),
          TEXTURE_FORMAT_RGBA_8_4BPP,
          TEXTURE_WRAP_CLAMP_TO_EDGE,
          JCGLTextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TEXTURE_FILTER_LINEAR,
          JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR);

      final Pair<JCGLTextureUnitType, JCGLTexture2DType> p_depth =
        cc.unitContextAllocateTexture2D(
          g_t,
          area.sizeX(),
          area.sizeY(),
          TEXTURE_FORMAT_DEPTH_24_STENCIL_8_4BPP,
          TEXTURE_WRAP_CLAMP_TO_EDGE,
          JCGLTextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TEXTURE_FILTER_LINEAR,
          JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR);

      final Pair<JCGLTextureUnitType, JCGLTexture2DType> p_norm =
        cc.unitContextAllocateTexture2D(
          g_t,
          area.sizeX(),
          area.sizeY(),
          TEXTURE_FORMAT_RG_16F_4BPP,
          TEXTURE_WRAP_CLAMP_TO_EDGE,
          JCGLTextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TEXTURE_FILTER_LINEAR,
          JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR);

      Pair<JCGLTextureUnitType, JCGLTexture2DType> p_spec = null;
      switch (desc.components()) {
        case R2_GEOMETRY_BUFFER_FULL:
          p_spec = cc.unitContextAllocateTexture2D(
            g_t,
            area.sizeX(),
            area.sizeY(),
            TEXTURE_FORMAT_RGBA_8_4BPP,
            TEXTURE_WRAP_CLAMP_TO_EDGE,
            JCGLTextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TEXTURE_FILTER_LINEAR,
            JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR);
          break;
        case R2_GEOMETRY_BUFFER_NO_SPECULAR:
          break;
      }


      final R2Texture2DType rt_rgba = R2Texture2DStatic.of(p_rgba.getRight());
      final R2Texture2DType rt_depth = R2Texture2DStatic.of(p_depth.getRight());
      final R2Texture2DType rt_norm = R2Texture2DStatic.of(p_norm.getRight());
      R2Texture2DType rt_spec = null;

      final JCGLFramebufferBuilderType fbb = g_fb.framebufferNewBuilder();
      fbb.attachColorTexture2DAt(
        points.get(0),
        buffers.get(0),
        rt_rgba.texture());
      fbb.attachColorTexture2DAt(
        points.get(1),
        buffers.get(1),
        rt_norm.texture());
      if (p_spec != null) {
        rt_spec = R2Texture2DStatic.of(p_spec.getRight());
        fbb.attachColorTexture2DAt(
          points.get(2),
          buffers.get(2),
          rt_spec.texture());
      }
      fbb.attachDepthStencilTexture2D(rt_depth.texture());

      final JCGLFramebufferType fb = g_fb.framebufferAllocate(fbb);
      return new R2GeometryBuffer(
        fb, desc, rt_rgba, rt_norm, rt_spec, rt_depth);
    } finally {
      cc.unitContextFinish(g_t);
    }
  }

  @Override
  public R2Texture2DUsableType albedoEmissiveTexture()
  {
    return this.t_rgba;
  }

  @Override
  public R2Texture2DUsableType normalTexture()
  {
    return this.t_norm;
  }

  @Override
  public Optional<R2Texture2DUsableType> specularTexture()
  {
    return this.t_spec_opt;
  }

  @Override
  public R2Texture2DUsableType depthTexture()
  {
    return this.t_depth;
  }

  @Override
  public JCGLFramebufferUsableType primaryFramebuffer()
  {
    return this.framebuffer;
  }

  @Override
  public AreaSizeL size()
  {
    return this.t_rgba.texture().size();
  }

  @Override
  public R2GeometryBufferDescription description()
  {
    return this.desc;
  }

  @Override
  public UnsignedRangeInclusiveL byteRange()
  {
    return this.range;
  }

  @Override
  public boolean isDeleted()
  {
    return this.framebuffer.isDeleted();
  }

  @Override
  public void delete(final JCGLInterfaceGL33Type g)
    throws R2Exception
  {
    if (!this.isDeleted()) {
      this.t_rgba.delete(g);
      this.t_depth.delete(g);
      this.t_norm.delete(g);
      if (this.t_spec != null) {
        this.t_spec.delete(g);
      }

      final JCGLFramebuffersType g_fb = g.framebuffers();
      g_fb.framebufferDelete(this.framebuffer);
    }
  }

  @Override
  public void clearBoundPrimaryFramebuffer(
    final JCGLInterfaceGL33Type g)
    throws R2RendererExceptionFramebufferNotBound
  {
    final JCGLFramebuffersType g_fb = g.framebuffers();

    if (!g_fb.framebufferDrawIsBound(this.framebuffer)) {
      final StringBuilder sb = new StringBuilder(128);
      sb.append("Expected a framebuffer to be bound.");
      sb.append(System.lineSeparator());
      sb.append("Framebuffer: ");
      sb.append(this.framebuffer);
      sb.append(System.lineSeparator());
      throw new R2RendererExceptionFramebufferNotBound(sb.toString());
    }

    JCGLRenderStates.activate(g, CLEAR_STATE);
    g.clearing().clear(CLEAR_SPEC);
  }

}
