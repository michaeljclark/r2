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

package com.io7m.r2.core.filters;

import com.io7m.jcanephora.core.JCGLTexture2DUpdateType;
import com.io7m.jcanephora.core.JCGLTextureFilterMagnification;
import com.io7m.jcanephora.core.JCGLTextureFilterMinification;
import com.io7m.jcanephora.core.JCGLTextureFormat;
import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.JCGLTextureUpdates;
import com.io7m.jcanephora.core.JCGLTextureWrapS;
import com.io7m.jcanephora.core.JCGLTextureWrapT;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.cursors.JCGLRGB8ByteBuffered;
import com.io7m.jcanephora.cursors.JCGLRGB8Type;
import com.io7m.jfunctional.Pair;
import com.io7m.jnull.NullCheck;
import com.io7m.jpra.runtime.java.JPRACursor2DByteBufferedUnchecked;
import com.io7m.jpra.runtime.java.JPRACursor2DType;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r2.core.R2Texture2DType;
import com.io7m.r2.core.R2TextureUnitContextParentType;
import com.io7m.r2.core.R2TextureUnitContextType;

/**
 * Functions for allocating 4x4 noise textures for rotating SSAO kernels.
 */

public final class R2SSAONoiseTexture
{
  private R2SSAONoiseTexture()
  {
    throw new UnimplementedCodeException();
  }

  /**
   * Construct a new 4x4 noise texture.
   *
   * @param gt A texture interface
   * @param tc A texture unit allocator
   *
   * @return A new texture
   */

  public static R2Texture2DType new4x4Noise(
    final JCGLTexturesType gt,
    final R2TextureUnitContextParentType tc)
  {
    NullCheck.notNull(gt);
    NullCheck.notNull(tc);

    final R2TextureUnitContextType cc = tc.unitContextNew();
    try {
      final Pair<JCGLTextureUnitType, R2Texture2DType> p =
        cc.unitContextAllocateTexture2D(
          gt,
          4L,
          4L,
          JCGLTextureFormat.TEXTURE_FORMAT_RGB_8_3BPP,
          JCGLTextureWrapS.TEXTURE_WRAP_REPEAT,
          JCGLTextureWrapT.TEXTURE_WRAP_REPEAT,
          JCGLTextureFilterMinification.TEXTURE_FILTER_LINEAR,
          JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR);
      final R2Texture2DType rt = p.getRight();

      final JCGLTexture2DUpdateType tu =
        JCGLTextureUpdates.newUpdateReplacingAll2D(rt.get());
      final JPRACursor2DType<JCGLRGB8Type> c =
        JPRACursor2DByteBufferedUnchecked.newCursor(
          tu.getData(), 4, 4, JCGLRGB8ByteBuffered::newValueWithOffset);

      final JCGLRGB8Type uv = c.getElementView();
      for (int y = 0; y < 4; ++y) {
        for (int x = 0; x < 4; ++x) {
          c.setElementPosition(x, y);
          uv.setR((Math.random() * 2.0) - 1.0);
          uv.setG((Math.random() * 2.0) - 1.0);
          uv.setB(0.0);
        }
      }

      gt.texture2DUpdate(p.getLeft(), tu);
      return rt;
    } finally {
      cc.unitContextFinish(gt);
    }
  }
}
