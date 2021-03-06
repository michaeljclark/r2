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

package com.io7m.r2.tests.rendering.depth.api;

import com.io7m.jcanephora.core.JCGLFramebufferUsableType;
import com.io7m.jcanephora.core.JCGLTextureFormat;
import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocator;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocatorType;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizeL;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r2.rendering.depth.R2DepthOnlyBuffer;
import com.io7m.r2.rendering.depth.api.R2DepthOnlyBufferDescription;
import com.io7m.r2.rendering.depth.api.R2DepthOnlyBufferType;
import com.io7m.r2.rendering.depth.api.R2DepthPrecision;
import com.io7m.r2.tests.R2JCGLContract;
import com.io7m.r2.textures.R2Texture2DUsableType;
import org.junit.Assert;
import org.junit.Test;

public abstract class R2DepthOnlyBufferContract extends R2JCGLContract
{
  private static JCGLTextureFormat formatForPrecision(
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

  @Test
  public final void testIdentities()
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g = c.contextGetGL33();
    final JCGLTextureUnitAllocatorType tc =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        3, g.textures().textureGetUnits());

    final AreaSizeL area = AreaSizeL.of(640L, 480L);

    for (final R2DepthPrecision p : R2DepthPrecision.values()) {
      final JCGLTextureFormat f =
        formatForPrecision(p);

      final R2DepthOnlyBufferDescription.Builder db =
        R2DepthOnlyBufferDescription.builder();
      db.setDepthPrecision(p);
      db.setArea(area);

      final R2DepthOnlyBufferDescription desc = db.build();
      final R2DepthOnlyBufferType gb =
        R2DepthOnlyBuffer.create(
          g.framebuffers(),
          g.textures(),
          tc.rootContext(),
          desc);

      Assert.assertEquals(
        640L * 480L * (long) f.getBytesPerPixel(),
        gb.byteRange().getInterval());
      Assert.assertFalse(gb.isDeleted());

      final R2Texture2DUsableType t_dept =
        gb.depthTexture();
      final JCGLFramebufferUsableType fb =
        gb.primaryFramebuffer();

      Assert.assertEquals(desc, gb.description());
      Assert.assertEquals(area, gb.size());
      Assert.assertEquals(
        f,
        t_dept.texture().format());

      gb.delete(g);
      Assert.assertTrue(fb.isDeleted());
      Assert.assertTrue(gb.isDeleted());
    }
  }
}
