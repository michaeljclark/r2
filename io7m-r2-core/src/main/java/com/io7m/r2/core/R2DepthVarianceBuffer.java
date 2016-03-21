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
import com.io7m.jcanephora.core.JCGLFramebufferBuilderType;
import com.io7m.jcanephora.core.JCGLFramebufferColorAttachmentPointType;
import com.io7m.jcanephora.core.JCGLFramebufferDrawBufferType;
import com.io7m.jcanephora.core.JCGLFramebufferType;
import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.JCGLTextureFilterMagnification;
import com.io7m.jcanephora.core.JCGLTextureFilterMinification;
import com.io7m.jcanephora.core.JCGLTextureFormat;
import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.JCGLTextureWrapS;
import com.io7m.jcanephora.core.JCGLTextureWrapT;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jfunctional.Pair;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.junsigned.ranges.UnsignedRangeInclusiveL;

import java.util.List;

/**
 * Default implementation of the {@link R2DepthVarianceBufferType} interface.
 */

public final class R2DepthVarianceBuffer implements R2DepthVarianceBufferType
{
  private final R2Texture2DType                      t_depth;
  private final R2Texture2DType                      t_variance;
  private final JCGLFramebufferType                  framebuffer;
  private final UnsignedRangeInclusiveL              range;
  private final R2DepthVarianceBufferDescriptionType desc;

  private R2DepthVarianceBuffer(
    final JCGLFramebufferType in_framebuffer,
    final R2DepthVarianceBufferDescriptionType in_desc,
    final R2Texture2DType in_t_depth,
    final R2Texture2DType in_t_variance)
  {
    this.framebuffer = NullCheck.notNull(in_framebuffer);
    this.desc = NullCheck.notNull(in_desc);
    this.t_depth = NullCheck.notNull(in_t_depth);
    this.t_variance = NullCheck.notNull(in_t_variance);

    long size = 0L;
    size += this.t_depth.get().getRange().getInterval();
    size += this.t_variance.get().getRange().getInterval();
    this.range = new UnsignedRangeInclusiveL(0L, size - 1L);
  }

  /**
   * Construct a new depth-only buffer.
   *
   * @param g_fb A framebuffer interface
   * @param g_t  A texture interface
   * @param tc   A texture unit context
   * @param desc The geometry buffer description
   *
   * @return A new geometry buffer
   */

  public static R2DepthVarianceBufferType newDepthVarianceBuffer(
    final JCGLFramebuffersType g_fb,
    final JCGLTexturesType g_t,
    final R2TextureUnitContextParentType tc,
    final R2DepthVarianceBufferDescriptionType desc)
  {
    NullCheck.notNull(g_fb);
    NullCheck.notNull(g_t);
    NullCheck.notNull(tc);
    NullCheck.notNull(desc);

    final AreaInclusiveUnsignedLType area = desc.getArea();
    final UnsignedRangeInclusiveL range_x = area.getRangeX();
    final UnsignedRangeInclusiveL range_y = area.getRangeY();

    final List<JCGLFramebufferColorAttachmentPointType> points =
      g_fb.framebufferGetColorAttachments();
    final List<JCGLFramebufferDrawBufferType> buffers =
      g_fb.framebufferGetDrawBuffers();

    final R2TextureUnitContextType cc = tc.unitContextNewWithReserved(4);
    try {
      final Pair<JCGLTextureUnitType, R2Texture2DType> p_depth =
        cc.unitContextAllocateTexture2D(
          g_t,
          range_x.getInterval(),
          range_y.getInterval(),
          R2DepthVarianceBuffer.formatDepthForPrecision(desc
                                                          .getDepthPrecision()),
          JCGLTextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          JCGLTextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          JCGLTextureFilterMinification.TEXTURE_FILTER_NEAREST,
          JCGLTextureFilterMagnification.TEXTURE_FILTER_NEAREST);

      final Pair<JCGLTextureUnitType, R2Texture2DType> p_variance =
        cc.unitContextAllocateTexture2D(
          g_t,
          range_x.getInterval(),
          range_y.getInterval(),
          R2DepthVarianceBuffer.formatVarianceForPrecision(
            desc.getDepthVariancePrecision()),
          JCGLTextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          JCGLTextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          JCGLTextureFilterMinification.TEXTURE_FILTER_LINEAR,
          JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR);

      final R2Texture2DType rt_depth = p_depth.getRight();
      final R2Texture2DType rt_variance = p_variance.getRight();

      final JCGLFramebufferBuilderType fbb = g_fb.framebufferNewBuilder();
      fbb.attachDepthTexture2D(rt_depth.get());
      fbb.attachColorTexture2DAt(
        points.get(0), buffers.get(0), rt_variance.get());

      final JCGLFramebufferType fb = g_fb.framebufferAllocate(fbb);
      return new R2DepthVarianceBuffer(fb, desc, rt_depth, rt_variance);
    } finally {
      cc.unitContextFinish(g_t);
    }
  }

  private static JCGLTextureFormat formatVarianceForPrecision(
    final R2DepthVariancePrecision p)
  {
    switch (p) {
      case R2_DEPTH_VARIANCE_PRECISION_16:
        return JCGLTextureFormat.TEXTURE_FORMAT_RG_16F_4BPP;
      case R2_DEPTH_VARIANCE_PRECISION_32:
        return JCGLTextureFormat.TEXTURE_FORMAT_RG_32F_8BPP;
    }

    throw new UnreachableCodeException();
  }

  private static JCGLTextureFormat formatDepthForPrecision(
    final R2DepthPrecision p)
  {
    switch (p) {
      case R2_DEPTH_PRECISION_16:
        return JCGLTextureFormat.TEXTURE_FORMAT_DEPTH_16_2BPP;
      case R2_DEPTH_PRECISION_24:
        return JCGLTextureFormat.TEXTURE_FORMAT_DEPTH_24_4BPP;
      case R2_DEPTH_PRECISION_32F:
        return JCGLTextureFormat.TEXTURE_FORMAT_DEPTH_32F_4BPP;
    }

    throw new UnreachableCodeException();
  }

  @Override
  public JCGLFramebufferUsableType getPrimaryFramebuffer()
  {
    return this.framebuffer;
  }

  @Override
  public AreaInclusiveUnsignedLType getArea()
  {
    return this.t_depth.get().textureGetArea();
  }

  @Override
  public R2DepthVarianceBufferDescriptionType getDescription()
  {
    return this.desc;
  }

  @Override
  public UnsignedRangeInclusiveL getRange()
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
      this.t_depth.delete(g);
      this.t_variance.delete(g);

      final JCGLFramebuffersType g_fb = g.getFramebuffers();
      g_fb.framebufferDelete(this.framebuffer);
    }
  }

  @Override
  public R2Texture2DUsableType getDepthVarianceTexture()
  {
    return this.t_variance;
  }
}
