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

package com.io7m.r2.tests.jogl.shader_sanity;

import com.io7m.jcanephora.core.JCGLArrayBufferType;
import com.io7m.jcanephora.core.JCGLArrayObjectBuilderType;
import com.io7m.jcanephora.core.JCGLArrayObjectType;
import com.io7m.jcanephora.core.JCGLBufferUpdateType;
import com.io7m.jcanephora.core.JCGLBufferUpdates;
import com.io7m.jcanephora.core.JCGLFragmentShaderType;
import com.io7m.jcanephora.core.JCGLFramebufferBuilderType;
import com.io7m.jcanephora.core.JCGLFramebufferColorAttachmentPointType;
import com.io7m.jcanephora.core.JCGLFramebufferDrawBufferType;
import com.io7m.jcanephora.core.JCGLFramebufferType;
import com.io7m.jcanephora.core.JCGLIndexBufferType;
import com.io7m.jcanephora.core.JCGLProgramShaderType;
import com.io7m.jcanephora.core.JCGLScalarType;
import com.io7m.jcanephora.core.JCGLTexture2DType;
import com.io7m.jcanephora.core.JCGLTextureFilterMagnification;
import com.io7m.jcanephora.core.JCGLTextureFilterMinification;
import com.io7m.jcanephora.core.JCGLTextureFormat;
import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.JCGLTextureWrapS;
import com.io7m.jcanephora.core.JCGLTextureWrapT;
import com.io7m.jcanephora.core.JCGLUnsignedType;
import com.io7m.jcanephora.core.JCGLUsageHint;
import com.io7m.jcanephora.core.JCGLVertexShaderType;
import com.io7m.jcanephora.core.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.core.api.JCGLArrayObjectsType;
import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLFramebuffersType;
import com.io7m.jcanephora.core.api.JCGLIndexBuffersType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.jtensors.storage.api.unparameterized.vectors.VectorStorageFloating2Type;
import com.io7m.jtensors.storage.api.unparameterized.vectors.VectorStorageFloating3Type;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r2.cursors.vertex.R2VertexPU32ByteBuffered;
import com.io7m.r2.cursors.vertex.R2VertexPU32Type;
import com.io7m.r2.shaders.api.R2ShaderPreprocessingEnvironmentReadableType;
import com.io7m.sombrero.core.SoShaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class R2ShadersTestUtilities
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(R2ShadersTestUtilities.class);
  }

  private R2ShadersTestUtilities()
  {
    throw new UnreachableCodeException();
  }

  static JCGLInterfaceGL33Type getGL()
  {
    final JCGLContextType c = R2TestContexts.newGL33Context("main", 24, 8);
    return c.contextGetGL33();
  }

  static JCGLArrayObjectType newScreenQuad(
    final JCGLInterfaceGL33Type g)
  {
    final JCGLArrayObjectsType g_ao = g.arrayObjects();
    final JCGLIndexBuffersType g_ib = g.indexBuffers();
    final JCGLArrayBuffersType g_ab = g.arrayBuffers();

    final JCGLIndexBufferType ib =
      g_ib.indexBufferAllocate(
        6L,
        JCGLUnsignedType.TYPE_UNSIGNED_INT,
        JCGLUsageHint.USAGE_STATIC_DRAW);

    {
      final JCGLBufferUpdateType<JCGLIndexBufferType> u =
        JCGLBufferUpdates.newUpdateReplacingAll(ib);
      final IntBuffer i = u.data().asIntBuffer();

      i.put(0, 0);
      i.put(1, 1);
      i.put(2, 2);

      i.put(3, 0);
      i.put(4, 2);
      i.put(5, 3);

      g_ib.indexBufferUpdate(u);
      g_ib.indexBufferUnbind();
    }

    final int vertex_size = R2VertexPU32ByteBuffered.sizeInOctets();
    final JCGLArrayBufferType ab =
      g_ab.arrayBufferAllocate(
        (long) vertex_size * 4L, JCGLUsageHint.USAGE_STATIC_DRAW);

    {
      final JCGLBufferUpdateType<JCGLArrayBufferType> u =
        JCGLBufferUpdates.newUpdateReplacingAll(ab);

      final JPRACursor1DType<R2VertexPU32Type> c =
        JPRACursor1DByteBufferedChecked.newCursor(
          u.data(), R2VertexPU32ByteBuffered::newValueWithOffset);
      final R2VertexPU32Type v = c.getElementView();
      final VectorStorageFloating3Type pv = v.getPositionWritable();
      final VectorStorageFloating2Type uv = v.getUvWritable();

      c.setElementIndex(0);
      pv.setXYZ(-1.0, 1.0, 0.0);
      uv.setXY(0.0, 1.0);

      c.setElementIndex(1);
      pv.setXYZ(-1.0, -1.0, 0.0);
      uv.setXY(0.0, 0.0);

      c.setElementIndex(2);
      pv.setXYZ(1.0, -1.0, 0.0);
      uv.setXY(1.0, 0.0);

      c.setElementIndex(3);
      pv.setXYZ(1.0, 1.0, 0.0);
      uv.setXY(1.0, 1.0);

      g_ab.arrayBufferUpdate(u);
      g_ab.arrayBufferUnbind();
    }

    final JCGLArrayObjectBuilderType aob = g_ao.arrayObjectNewBuilder();
    aob.setIndexBuffer(ib);
    aob.setAttributeFloatingPoint(
      0,
      ab,
      3,
      JCGLScalarType.TYPE_FLOAT,
      vertex_size,
      (long) R2VertexPU32ByteBuffered.metaPositionStaticOffsetFromType(),
      false);
    aob.setAttributeFloatingPoint(
      1,
      ab,
      2,
      JCGLScalarType.TYPE_FLOAT,
      vertex_size,
      (long) R2VertexPU32ByteBuffered.metaUvStaticOffsetFromType(),
      false);

    final JCGLArrayObjectType ao = g_ao.arrayObjectAllocate(aob);
    g_ao.arrayObjectUnbind();
    return ao;
  }

  static JCGLFramebufferType newColorFramebuffer(
    final JCGLInterfaceGL33Type g,
    final JCGLTextureFormat f,
    final int w,
    final int h)
  {
    final JCGLTexturesType gt = g.textures();
    final JCGLFramebuffersType fb = g.framebuffers();
    final JCGLFramebufferBuilderType fbb = fb.framebufferNewBuilder();
    final List<JCGLFramebufferColorAttachmentPointType> points =
      fb.framebufferGetColorAttachments();
    final List<JCGLFramebufferDrawBufferType> buffers =
      fb.framebufferGetDrawBuffers();
    final List<JCGLTextureUnitType> units = gt.textureGetUnits();
    final JCGLTextureUnitType u = units.get(0);
    final JCGLTexture2DType t =
      newTexture2D(g, f, w, h, u);

    fbb.attachColorTexture2DAt(points.get(0), buffers.get(0), t);
    return fb.framebufferAllocate(fbb);
  }

  static JCGLTexture2DType newTexture2D(
    final JCGLInterfaceGL33Type g,
    final JCGLTextureFormat f,
    final int w,
    final int h,
    final JCGLTextureUnitType u)
  {
    final JCGLTexturesType gt = g.textures();
    final JCGLTexture2DType t =
      gt.texture2DAllocate(
        u, (long) w, (long) h, f,
        JCGLTextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
        JCGLTextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
        JCGLTextureFilterMinification.TEXTURE_FILTER_NEAREST,
        JCGLTextureFilterMagnification.TEXTURE_FILTER_NEAREST);
    gt.textureUnitUnbind(u);
    return t;
  }

  static JCGLProgramShaderType compilerShaderVF(
    final JCGLShadersType gs,
    final R2ShaderPreprocessingEnvironmentReadableType env,
    final String v_path,
    final String f_path)
    throws IOException
  {
    try {
      final List<String> v_lines =
        env.preprocessor().preprocessFile(Collections.emptyMap(), v_path);
      final List<String> f_lines =
        env.preprocessor().preprocessFile(Collections.emptyMap(), f_path);
      final JCGLVertexShaderType v =
        gs.shaderCompileVertex(v_path, v_lines);
      final JCGLFragmentShaderType f =
        gs.shaderCompileFragment(f_path, f_lines);
      return gs.shaderLinkProgram(
        "(" + v_path + " + " + f_path + ")", v, Optional.empty(), f);

    } catch (final SoShaderException e) {
      throw new IOException(e);
    }
  }
}
