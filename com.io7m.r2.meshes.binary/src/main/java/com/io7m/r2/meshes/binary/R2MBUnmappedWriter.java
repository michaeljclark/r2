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

package com.io7m.r2.meshes.binary;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jnull.NullCheck;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.jtensors.core.parameterized.vectors.PVector2D;
import com.io7m.jtensors.core.parameterized.vectors.PVector3D;
import com.io7m.jtensors.core.parameterized.vectors.PVector4D;
import com.io7m.jtensors.storage.api.unparameterized.vectors.VectorStorageFloating2Type;
import com.io7m.jtensors.storage.api.unparameterized.vectors.VectorStorageFloating3Type;
import com.io7m.jtensors.storage.api.unparameterized.vectors.VectorStorageFloating4Type;
import com.io7m.r2.meshes.R2MeshTangentsType;
import com.io7m.r2.meshes.R2MeshTangentsVertexType;
import com.io7m.r2.meshes.R2MeshTriangleType;
import com.io7m.r2.meshes.binary.r2mb.R2MBHeaderByteBuffered;
import com.io7m.r2.meshes.binary.r2mb.R2MBHeaderType;
import com.io7m.r2.meshes.binary.r2mb.R2MBTriangleByteBuffered;
import com.io7m.r2.meshes.binary.r2mb.R2MBTriangleType;
import com.io7m.r2.meshes.binary.r2mb.R2MBVertexByteBuffered;
import com.io7m.r2.meshes.binary.r2mb.R2MBVertexType;
import com.io7m.r2.spaces.R2SpaceObjectType;
import com.io7m.r2.spaces.R2SpaceTextureType;
import it.unimi.dsi.fastutil.BigList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A mesh writer that writes data to a
 * {@link WritableByteChannel}.
 */

public final class R2MBUnmappedWriter implements R2MBWriterType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(R2MBUnmappedWriter.class);
  }

  private final WritableByteChannel channel;
  private final ByteBuffer buffer_header;
  private final ByteBuffer buffer_vertex;
  private final ByteBuffer buffer_tri;
  private final R2MeshTangentsType mesh;

  private R2MBUnmappedWriter(
    final WritableByteChannel in_channel,
    final R2MeshTangentsType in_mesh)
  {
    this.channel = NullCheck.notNull(in_channel, "Channel");
    this.mesh = NullCheck.notNull(in_mesh, "Mesh");

    this.buffer_header =
      ByteBuffer.allocate(R2MBHeaderByteBuffered.sizeInOctets());
    this.buffer_header.order(ByteOrder.BIG_ENDIAN);
    this.buffer_vertex =
      ByteBuffer.allocate(R2MBVertexByteBuffered.sizeInOctets());
    this.buffer_vertex.order(ByteOrder.BIG_ENDIAN);
    this.buffer_tri =
      ByteBuffer.allocate(R2MBTriangleByteBuffered.sizeInOctets());
    this.buffer_tri.order(ByteOrder.BIG_ENDIAN);
  }

  /**
   * Construct a new writer for the given file.
   *
   * @param p The file
   * @param m The mesh
   *
   * @return A new writer
   *
   * @throws IOException On I/O errors
   */

  public static R2MBWriterType newWriterForPath(
    final Path p,
    final R2MeshTangentsType m)
    throws IOException
  {
    NullCheck.notNull(p, "Path");
    NullCheck.notNull(m, "Mesh");

    final FileChannel fc = FileChannel.open(
      p,
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.READ,
      StandardOpenOption.TRUNCATE_EXISTING);
    return new R2MBUnmappedWriter(fc, m);
  }

  /**
   * Construct a new writer for the given stream.
   *
   * @param s The output stream
   * @param m The mesh
   *
   * @return A new writer
   *
   * @throws IOException On I/O errors
   */

  public static R2MBWriterType newWriterForOutputStream(
    final OutputStream s,
    final R2MeshTangentsType m)
    throws IOException
  {
    NullCheck.notNull(s, "Stream");
    NullCheck.notNull(m, "Mesh");

    return new R2MBUnmappedWriter(Channels.newChannel(s), m);
  }

  private static long writeAll(
    final ByteBuffer bh,
    final WritableByteChannel channel)
    throws IOException
  {
    long all = 0L;
    while (bh.hasRemaining()) {
      final int w = channel.write(bh);
      if (w == -1) {
        break;
      }
      all += w;
    }
    return all;
  }

  @Override
  public long run()
    throws IOException
  {
    long bc = 0L;
    final long v_count = this.mesh.vertices().size64();
    final long t_count = this.mesh.triangles().size64();

    /*
      Write header.
     */

    {
      final JPRACursor1DType<R2MBHeaderType> c =
        JPRACursor1DByteBufferedChecked.newCursor(
          this.buffer_header, R2MBHeaderByteBuffered::newValueWithOffset);

      final R2MBHeaderType v = c.getElementView();
      v.setMagic0((byte) 'R');
      v.setMagic1((byte) '2');
      v.setMagic2((byte) 'B');
      v.setMagic3((byte) '\n');
      v.setVersion(R2MBVersion.R2MB_VERSION);
      v.setTriangleCount((int) (t_count & 0xFFFFFFFFL));
      v.setVertexCount((int) (v_count & 0xFFFFFFFFL));

      bc += writeAll(this.buffer_header, this.channel);
    }

    /*
      Write vertices.
     */

    if (v_count > 0L) {
      final JPRACursor1DType<R2MBVertexType> c =
        JPRACursor1DByteBufferedChecked.newCursor(
          this.buffer_vertex,
          R2MBVertexByteBuffered::newValueWithOffset);

      final R2MBVertexType v = c.getElementView();
      final VectorStorageFloating3Type v_pos = v.getPositionWritable();
      final VectorStorageFloating3Type v_nor = v.getNormalWritable();
      final VectorStorageFloating4Type v_tan = v.getTangentWritable();
      final VectorStorageFloating2Type v_uv = v.getUvWritable();

      final BigList<R2MeshTangentsVertexType> vertices =
        this.mesh.vertices();
      final BigList<PVector3D<R2SpaceObjectType>> m_pos =
        this.mesh.positions();
      final BigList<PVector3D<R2SpaceObjectType>> m_nor =
        this.mesh.normals();
      final BigList<PVector4D<R2SpaceObjectType>> m_tan =
        this.mesh.tangents();
      final BigList<PVector2D<R2SpaceTextureType>> m_uv =
        this.mesh.uvs();

      for (long index = 0L; index < v_count; ++index) {
        this.buffer_vertex.rewind();

        final Long bi = Long.valueOf(index);
        final R2MeshTangentsVertexType vv = vertices.get(index);

        {
          final PVector3D<R2SpaceObjectType> k = m_pos.get(vv.positionIndex());

          final double x = k.x();
          final double y = k.y();
          final double z = k.z();

          Invariants.checkInvariantD(
            x,
            Double.isFinite(x),
            n -> String.format("Position [%d].x must be finite", bi));
          Invariants.checkInvariantD(
            y,
            Double.isFinite(y),
            n -> String.format("Position [%d].y must be finite", bi));
          Invariants.checkInvariantD(
            z,
            Double.isFinite(z),
            n -> String.format("Position [%d].z must be finite", bi));

          Invariants.checkInvariantD(
            x,
            !Double.isNaN(x),
            n -> String.format("Position [%d].x must be a valid number", bi));
          Invariants.checkInvariantD(
            y,
            !Double.isNaN(y),
            n -> String.format("Position [%d].y must be a valid number", bi));
          Invariants.checkInvariantD(
            z,
            !Double.isNaN(z),
            n -> String.format("Position [%d].z must be a valid number", bi));

          v_pos.setXYZ(x, y, z);
        }

        {
          final PVector3D<R2SpaceObjectType> k = m_nor.get(vv.normalIndex());

          final double x = k.x();
          final double y = k.y();
          final double z = k.z();

          Invariants.checkInvariantD(
            x,
            Double.isFinite(x),
            n -> String.format("Normal [%d].x must be finite", bi));
          Invariants.checkInvariantD(
            y,
            Double.isFinite(y),
            n -> String.format("Normal [%d].y must be finite", bi));
          Invariants.checkInvariantD(
            z,
            Double.isFinite(z),
            n -> String.format("Normal [%d].z must be finite", bi));

          Invariants.checkInvariantD(
            x,
            !Double.isNaN(x),
            n -> String.format("Normal [%d].x must be a valid number", bi));
          Invariants.checkInvariantD(
            y,
            !Double.isNaN(y),
            n -> String.format("Normal [%d].y must be a valid number", bi));
          Invariants.checkInvariantD(
            z,
            !Double.isNaN(z),
            n -> String.format("Normal [%d].z must be a valid number", bi));

          v_nor.setXYZ(x, y, z);
        }

        {
          final PVector4D<R2SpaceObjectType> k = m_tan.get(vv.tangentIndex());

          final double x = k.x();
          final double y = k.y();
          final double z = k.z();
          final double w = k.w();

          Invariants.checkInvariantD(
            x,
            Double.isFinite(x),
            n -> String.format("Tangent [%d].x must be finite", bi));
          Invariants.checkInvariantD(
            y,
            Double.isFinite(y),
            n -> String.format("Tangent [%d].y must be finite", bi));
          Invariants.checkInvariantD(
            z,
            Double.isFinite(z),
            n -> String.format("Tangent [%d].z must be finite", bi));
          Invariants.checkInvariantD(
            w,
            Double.isFinite(z),
            n -> String.format("Tangent [%d].w must be finite", bi));

          Invariants.checkInvariantD(
            x,
            !Double.isNaN(x),
            n -> String.format("Tangent [%d].x must be a valid number", bi));
          Invariants.checkInvariantD(
            y,
            !Double.isNaN(y),
            n -> String.format("Tangent [%d].y must be a valid number", bi));
          Invariants.checkInvariantD(
            z,
            !Double.isNaN(z),
            n -> String.format("Tangent [%d].z must be a valid number", bi));
          Invariants.checkInvariantD(
            w,
            !Double.isNaN(w),
            n -> String.format("Tangent [%d].w must be a valid number", bi));

          v_tan.setXYZW(x, y, z, w);
        }

        {
          final PVector2D<R2SpaceTextureType> k = m_uv.get(vv.uvIndex());

          final double x = k.x();
          final double y = k.y();

          Invariants.checkInvariantD(
            x,
            Double.isFinite(x),
            n -> String.format("UV [%d].x must be finite", bi));
          Invariants.checkInvariantD(
            y,
            Double.isFinite(y),
            n -> String.format("UV [%d].y must be finite", bi));

          Invariants.checkInvariantD(
            x,
            !Double.isNaN(x),
            n -> String.format("UV [%d].x must be a valid number", bi));
          Invariants.checkInvariantD(
            y,
            !Double.isNaN(y),
            n -> String.format("UV [%d].y must be a valid number", bi));

          v_uv.setXY(x, y);
        }

        bc += writeAll(this.buffer_vertex, this.channel);
      }
    }

    /*
      Write triangles.
     */

    if (t_count > 0L) {
      final JPRACursor1DType<R2MBTriangleType> c =
        JPRACursor1DByteBufferedChecked.newCursor(
          this.buffer_tri,
          R2MBTriangleByteBuffered::newValueWithOffset);

      final R2MBTriangleType v = c.getElementView();

      final BigList<R2MeshTriangleType> m_tri =
        this.mesh.triangles();

      for (long index = 0L; index < t_count; ++index) {
        this.buffer_tri.rewind();

        final R2MeshTriangleType t = m_tri.get(index);
        v.setV0((int) (t.v0() & 0xFFFFFFFFL));
        v.setV1((int) (t.v1() & 0xFFFFFFFFL));
        v.setV2((int) (t.v2() & 0xFFFFFFFFL));

        bc += writeAll(this.buffer_tri, this.channel);
      }
    }

    return bc;
  }

  @Override
  public void close()
    throws IOException
  {
    this.channel.close();
  }
}