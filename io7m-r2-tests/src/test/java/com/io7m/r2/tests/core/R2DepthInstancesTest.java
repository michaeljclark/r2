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

package com.io7m.r2.tests.core;

import com.io7m.jcanephora.core.JCGLArrayObjectType;
import com.io7m.jcanephora.core.JCGLArrayObjectUsableType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r2.core.R2DepthInstances;
import com.io7m.r2.core.R2DepthInstancesConsumerType;
import com.io7m.r2.core.R2DepthInstancesType;
import com.io7m.r2.core.R2InstanceBatchedType;
import com.io7m.r2.core.R2InstanceSingleType;
import com.io7m.r2.core.R2MaterialOpaqueBatchedType;
import com.io7m.r2.core.R2MaterialOpaqueSingleType;
import com.io7m.r2.core.R2MaterialType;
import com.io7m.r2.core.R2RendererExceptionInstanceAlreadyVisible;
import com.io7m.r2.core.shaders.types.R2ShaderInstanceBatchedUsableType;
import com.io7m.r2.core.shaders.types.R2ShaderInstanceSingleUsableType;
import com.io7m.r2.core.shaders.types.R2ShaderUsableType;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class R2DepthInstancesTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(R2DepthInstancesTest.class);
  }

  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testEmpty()
  {
    final AtomicBoolean finished = new AtomicBoolean(false);
    final AtomicBoolean started = new AtomicBoolean(false);
    final R2DepthInstancesType o = R2DepthInstances.newDepthInstances();

    o.depthsExecute(new UnreachableConsumer()
    {
      @Override
      public void onFinish()
      {
        finished.set(true);
      }

      @Override
      public void onStart()
      {
        started.set(true);
      }
    });

    Assert.assertTrue(started.get());
    Assert.assertTrue(finished.get());
  }

  @Test
  public void testSingleAlreadyVisible()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();
    final R2DepthInstancesType o = R2DepthInstances.newDepthInstances();
    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final R2InstanceSingleType i =
      R2TestUtilities.getInstanceSingle(g, a0, 0L);
    final R2ShaderInstanceSingleUsableType<Object> s =
      R2TestUtilities.getShaderInstanceSingle(g, 0L);
    final R2MaterialOpaqueSingleType<Object> m0 =
      R2TestUtilities.getMaterialSingle(g, s, new Object(), 0L);
    final R2MaterialOpaqueSingleType<Object> m1 =
      R2TestUtilities.getMaterialSingle(g, s, new Object(), 1L);

    o.depthsAddSingleInstance(i, m0);
    this.expected.expect(R2RendererExceptionInstanceAlreadyVisible.class);
    o.depthsAddSingleInstance(i, m1);
  }

  @Test
  public void testSingleOrdering()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceSingleType i0a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 0L);
    final R2InstanceSingleType i1a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 1L);
    final R2InstanceSingleType i2a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 2L);

    final R2InstanceSingleType i3a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 3L);
    final R2InstanceSingleType i4a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 4L);
    final R2InstanceSingleType i5a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 5L);

    final R2InstanceSingleType i6a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 6L);
    final R2InstanceSingleType i7a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 7L);
    final R2InstanceSingleType i8a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 8L);

    final R2InstanceSingleType i9a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 9L);
    final R2InstanceSingleType i10a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 10L);
    final R2InstanceSingleType i11a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 11L);

    final R2InstanceSingleType i12a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 12L);
    final R2InstanceSingleType i13a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 13L);
    final R2InstanceSingleType i14a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 14L);

    final R2InstanceSingleType i15a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 15L);
    final R2InstanceSingleType i16a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 16L);
    final R2InstanceSingleType i17a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 17L);

    final R2InstanceSingleType i18a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 18L);
    final R2InstanceSingleType i19a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 19L);
    final R2InstanceSingleType i20a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 20L);

    final R2InstanceSingleType i21a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 21L);
    final R2InstanceSingleType i22a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 22L);
    final R2InstanceSingleType i23a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 23L);

    final R2ShaderInstanceSingleUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceSingle(g, 0L);
    final R2ShaderInstanceSingleUsableType<Object> s1 =
      R2TestUtilities.getShaderInstanceSingle(g, 1L);

    final R2MaterialOpaqueSingleType<Object> m0 =
      R2TestUtilities.getMaterialSingle(g, s0, new Object(), 0L);
    final R2MaterialOpaqueSingleType<Object> m1 =
      R2TestUtilities.getMaterialSingle(g, s0, new Object(), 1L);
    final R2MaterialOpaqueSingleType<Object> m2 =
      R2TestUtilities.getMaterialSingle(g, s1, new Object(), 2L);
    final R2MaterialOpaqueSingleType<Object> m3 =
      R2TestUtilities.getMaterialSingle(g, s1, new Object(), 3L);

    final R2DepthInstancesType o = R2DepthInstances.newDepthInstances();
    o.depthsAddSingleInstance(i0a0, m0);
    o.depthsAddSingleInstance(i1a0, m0);
    o.depthsAddSingleInstance(i2a0, m0);
    o.depthsAddSingleInstance(i3a1, m0);
    o.depthsAddSingleInstance(i4a1, m0);
    o.depthsAddSingleInstance(i5a1, m0);

    o.depthsAddSingleInstance(i6a0, m1);
    o.depthsAddSingleInstance(i7a0, m1);
    o.depthsAddSingleInstance(i8a0, m1);
    o.depthsAddSingleInstance(i9a1, m1);
    o.depthsAddSingleInstance(i10a1, m1);
    o.depthsAddSingleInstance(i11a1, m1);

    o.depthsAddSingleInstance(i12a0, m2);
    o.depthsAddSingleInstance(i13a0, m2);
    o.depthsAddSingleInstance(i14a0, m2);
    o.depthsAddSingleInstance(i15a1, m2);
    o.depthsAddSingleInstance(i16a1, m2);
    o.depthsAddSingleInstance(i17a1, m2);

    o.depthsAddSingleInstance(i18a0, m3);
    o.depthsAddSingleInstance(i19a0, m3);
    o.depthsAddSingleInstance(i20a0, m3);
    o.depthsAddSingleInstance(i21a1, m3);
    o.depthsAddSingleInstance(i22a1, m3);
    o.depthsAddSingleInstance(i23a1, m3);

    Assert.assertEquals(24L, o.depthsCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.depthsExecute(cc);

    Assert.assertEquals(4L, (long) a0.getGLName());
    Assert.assertEquals(7L, (long) a1.getGLName());

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 4", op.remove(0));
    Assert.assertEquals("onInstanceSingle 0", op.remove(0));
    Assert.assertEquals("onInstanceSingle 1", op.remove(0));
    Assert.assertEquals("onInstanceSingle 2", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 7", op.remove(0));
    Assert.assertEquals("onInstanceSingle 5", op.remove(0));
    Assert.assertEquals("onInstanceSingle 3", op.remove(0));
    Assert.assertEquals("onInstanceSingle 4", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialStart 0 1", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 4", op.remove(0));
    Assert.assertEquals("onInstanceSingle 6", op.remove(0));
    Assert.assertEquals("onInstanceSingle 7", op.remove(0));
    Assert.assertEquals("onInstanceSingle 8", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 7", op.remove(0));
    Assert.assertEquals("onInstanceSingle 9", op.remove(0));
    Assert.assertEquals("onInstanceSingle 11", op.remove(0));
    Assert.assertEquals("onInstanceSingle 10", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialFinish 0 1", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderFinish 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderStart 1", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialStart 1 3", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 4", op.remove(0));
    Assert.assertEquals("onInstanceSingle 20", op.remove(0));
    Assert.assertEquals("onInstanceSingle 19", op.remove(0));
    Assert.assertEquals("onInstanceSingle 18", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 7", op.remove(0));
    Assert.assertEquals("onInstanceSingle 21", op.remove(0));
    Assert.assertEquals("onInstanceSingle 22", op.remove(0));
    Assert.assertEquals("onInstanceSingle 23", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialFinish 1 3", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialStart 1 2", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 4", op.remove(0));
    Assert.assertEquals("onInstanceSingle 14", op.remove(0));
    Assert.assertEquals("onInstanceSingle 12", op.remove(0));
    Assert.assertEquals("onInstanceSingle 13", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 7", op.remove(0));
    Assert.assertEquals("onInstanceSingle 15", op.remove(0));
    Assert.assertEquals("onInstanceSingle 16", op.remove(0));
    Assert.assertEquals("onInstanceSingle 17", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialFinish 1 2", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderFinish 1", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  @Test
  public void testSingleReset()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceSingleType i0a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 0L);
    final R2ShaderInstanceSingleUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceSingle(g, 0L);
    final R2MaterialOpaqueSingleType<Object> m0 =
      R2TestUtilities.getMaterialSingle(g, s0, new Object(), 0L);

    final R2DepthInstancesType o = R2DepthInstances.newDepthInstances();
    o.depthsAddSingleInstance(i0a0, m0);
    Assert.assertEquals(1L, o.depthsCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.depthsExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 4", op.remove(0));
    Assert.assertEquals("onInstanceSingle 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());

    o.depthsReset();
    o.depthsExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }


  private static final class LoggingConsumer implements
    R2DepthInstancesConsumerType
  {
    private final List<String>              ops;
    private       R2ShaderUsableType<?>     shader_current;
    private       R2MaterialType<?>         material_current;
    private       JCGLArrayObjectUsableType array_current;

    LoggingConsumer()
    {
      this.ops = new ArrayList<>(256);
    }

    @Override
    public void onStart()
    {
      this.ops.add("onStart");
    }

    @Override
    public void onInstanceBatchedUpdate(
      final R2InstanceBatchedType i)
    {
      this.ops.add(String.format(
        "onInstanceBatchedUpdate %d",
        Long.valueOf(i.getInstanceID())));
    }

    @Override
    public <M> void onInstanceBatchedShaderStart(
      final R2ShaderInstanceBatchedUsableType<M> s)
    {
      this.shader_current = s;
      this.ops.add(String.format(
        "onInstanceBatchedShaderStart %d",
        Long.valueOf(s.getShaderID())));
    }

    @Override
    public <M> void onInstanceBatchedMaterialStart(
      final R2MaterialOpaqueBatchedType<M> material)
    {
      final R2ShaderInstanceBatchedUsableType<M> s = material.getShader();
      Assert.assertEquals(this.shader_current, s);
      this.material_current = material;
      this.ops.add(String.format(
        "onInstanceBatchedMaterialStart %d %d",
        Long.valueOf(s.getShaderID()),
        Long.valueOf(material.getMaterialID())));
    }

    @Override
    public <M> void onInstanceBatched(
      final R2MaterialOpaqueBatchedType<M> material,
      final R2InstanceBatchedType i)
    {
      final R2ShaderInstanceBatchedUsableType<M> s = material.getShader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      this.ops.add(String.format(
        "onInstanceBatched %d",
        Long.valueOf(i.getInstanceID())));
    }

    @Override
    public <M> void onInstanceBatchedMaterialFinish(
      final R2MaterialOpaqueBatchedType<M> material)
    {
      final R2ShaderInstanceBatchedUsableType<M> s = material.getShader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      this.material_current = null;
      this.ops.add(String.format(
        "onInstanceBatchedMaterialFinish %d %d",
        Long.valueOf(s.getShaderID()),
        Long.valueOf(material.getMaterialID())));
    }

    @Override
    public <M> void onInstanceBatchedShaderFinish(
      final R2ShaderInstanceBatchedUsableType<M> s)
    {
      Assert.assertEquals(s, this.shader_current);
      this.shader_current = null;
      this.ops.add(String.format(
        "onInstanceBatchedShaderFinish %d",
        Long.valueOf(s.getShaderID())));
    }

    @Override
    public <M> void onInstanceSingleShaderStart(
      final R2ShaderInstanceSingleUsableType<M> s)
    {
      this.shader_current = s;
      this.ops.add(String.format(
        "onInstanceSingleShaderStart %d",
        Long.valueOf(s.getShaderID())));
    }

    @Override
    public <M> void onInstanceSingleMaterialStart(
      final R2MaterialOpaqueSingleType<M> material)
    {
      final R2ShaderInstanceSingleUsableType<M> s = material.getShader();
      Assert.assertEquals(this.shader_current, s);
      this.material_current = material;
      this.ops.add(String.format(
        "onInstanceSingleMaterialStart %d %d",
        Long.valueOf(s.getShaderID()),
        Long.valueOf(material.getMaterialID())));
    }

    @Override
    public void onInstanceSingleArrayStart(
      final R2InstanceSingleType i)
    {
      this.array_current = i.getArrayObject();
      this.ops.add(String.format(
        "onInstanceSingleArrayStart %d",
        Integer.valueOf(this.array_current.getGLName())));
    }

    @Override
    public <M> void onInstanceSingle(
      final R2MaterialOpaqueSingleType<M> material,
      final R2InstanceSingleType i)
    {
      final R2ShaderInstanceSingleUsableType<M> s = material.getShader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      Assert.assertEquals(i.getArrayObject(), this.array_current);
      this.ops.add(String.format(
        "onInstanceSingle %d",
        Long.valueOf(i.getInstanceID())));
    }

    @Override
    public <M> void onInstanceSingleMaterialFinish(
      final R2MaterialOpaqueSingleType<M> material)
    {
      final R2ShaderInstanceSingleUsableType<M> s = material.getShader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      this.material_current = null;
      this.ops.add(String.format(
        "onInstanceSingleMaterialFinish %d %d",
        Long.valueOf(s.getShaderID()),
        Long.valueOf(material.getMaterialID())));
    }

    @Override
    public <M> void onInstanceSingleShaderFinish(
      final R2ShaderInstanceSingleUsableType<M> s)
    {
      Assert.assertEquals(s, this.shader_current);
      this.shader_current = null;
      this.ops.add(String.format(
        "onInstanceSingleShaderFinish %d",
        Long.valueOf(s.getShaderID())));
    }

    @Override
    public void onFinish()
    {
      this.ops.add("onFinish");
      this.shader_current = null;
      this.array_current = null;
      this.material_current = null;
    }
  }

  @Test
  public void testBatchedAlreadyVisible()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();
    final R2DepthInstancesType o = R2DepthInstances.newDepthInstances();
    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final R2InstanceBatchedType i =
      R2TestUtilities.getInstanceBatched(g, a0, 0L);
    final R2ShaderInstanceBatchedUsableType<Object> s =
      R2TestUtilities.getShaderInstanceBatched(g, 0L);
    final R2MaterialOpaqueBatchedType<Object> m0 =
      R2TestUtilities.getMaterialBatched(g, s, new Object(), 0L);
    final R2MaterialOpaqueBatchedType<Object> m1 =
      R2TestUtilities.getMaterialBatched(g, s, new Object(), 1L);

    o.depthsAddBatchedInstance(i, m0);
    this.expected.expect(R2RendererExceptionInstanceAlreadyVisible.class);
    o.depthsAddBatchedInstance(i, m1);
  }

  @Test
  public void testBatchedReset()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceBatchedType i0a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 0L);
    final R2ShaderInstanceBatchedUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceBatched(g, 0L);
    final R2MaterialOpaqueBatchedType<Object> m0 =
      R2TestUtilities.getMaterialBatched(g, s0, new Object(), 0L);

    final R2DepthInstancesType o = R2DepthInstances.newDepthInstances();
    o.depthsAddBatchedInstance(i0a0, m0);
    Assert.assertEquals(1L, o.depthsCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.depthsExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onInstanceBatchedUpdate 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatched 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());

    o.depthsReset();
    o.depthsExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  @Test
  public void testBatchedOrdering()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceBatchedType i0a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 0L);
    final R2InstanceBatchedType i1a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 1L);
    final R2InstanceBatchedType i2a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 2L);

    final R2InstanceBatchedType i3a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 3L);
    final R2InstanceBatchedType i4a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 4L);
    final R2InstanceBatchedType i5a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 5L);

    final R2InstanceBatchedType i6a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 6L);
    final R2InstanceBatchedType i7a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 7L);
    final R2InstanceBatchedType i8a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 8L);

    final R2InstanceBatchedType i9a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 9L);
    final R2InstanceBatchedType i10a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 10L);
    final R2InstanceBatchedType i11a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 11L);

    final R2InstanceBatchedType i12a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 12L);
    final R2InstanceBatchedType i13a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 13L);
    final R2InstanceBatchedType i14a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 14L);

    final R2InstanceBatchedType i15a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 15L);
    final R2InstanceBatchedType i16a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 16L);
    final R2InstanceBatchedType i17a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 17L);

    final R2InstanceBatchedType i18a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 18L);
    final R2InstanceBatchedType i19a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 19L);
    final R2InstanceBatchedType i20a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 20L);

    final R2InstanceBatchedType i21a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 21L);
    final R2InstanceBatchedType i22a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 22L);
    final R2InstanceBatchedType i23a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 23L);

    final R2ShaderInstanceBatchedUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceBatched(g, 0L);
    final R2ShaderInstanceBatchedUsableType<Object> s1 =
      R2TestUtilities.getShaderInstanceBatched(g, 1L);

    final R2MaterialOpaqueBatchedType<Object> m0 =
      R2TestUtilities.getMaterialBatched(g, s0, new Object(), 0L);
    final R2MaterialOpaqueBatchedType<Object> m1 =
      R2TestUtilities.getMaterialBatched(g, s0, new Object(), 1L);
    final R2MaterialOpaqueBatchedType<Object> m2 =
      R2TestUtilities.getMaterialBatched(g, s1, new Object(), 2L);
    final R2MaterialOpaqueBatchedType<Object> m3 =
      R2TestUtilities.getMaterialBatched(g, s1, new Object(), 3L);

    final R2DepthInstancesType o = R2DepthInstances.newDepthInstances();
    o.depthsAddBatchedInstance(i0a0, m0);
    o.depthsAddBatchedInstance(i1a0, m0);
    o.depthsAddBatchedInstance(i2a0, m0);
    o.depthsAddBatchedInstance(i3a1, m0);
    o.depthsAddBatchedInstance(i4a1, m0);
    o.depthsAddBatchedInstance(i5a1, m0);

    o.depthsAddBatchedInstance(i6a0, m1);
    o.depthsAddBatchedInstance(i7a0, m1);
    o.depthsAddBatchedInstance(i8a0, m1);
    o.depthsAddBatchedInstance(i9a1, m1);
    o.depthsAddBatchedInstance(i10a1, m1);
    o.depthsAddBatchedInstance(i11a1, m1);

    o.depthsAddBatchedInstance(i12a0, m2);
    o.depthsAddBatchedInstance(i13a0, m2);
    o.depthsAddBatchedInstance(i14a0, m2);
    o.depthsAddBatchedInstance(i15a1, m2);
    o.depthsAddBatchedInstance(i16a1, m2);
    o.depthsAddBatchedInstance(i17a1, m2);

    o.depthsAddBatchedInstance(i18a0, m3);
    o.depthsAddBatchedInstance(i19a0, m3);
    o.depthsAddBatchedInstance(i20a0, m3);
    o.depthsAddBatchedInstance(i21a1, m3);
    o.depthsAddBatchedInstance(i22a1, m3);
    o.depthsAddBatchedInstance(i23a1, m3);

    Assert.assertEquals(24L, o.depthsCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.depthsExecute(cc);

    Assert.assertEquals(4L, (long) a0.getGLName());
    Assert.assertEquals(7L, (long) a1.getGLName());

    op.stream().forEach(System.out::println);

    Assert.assertEquals("onStart", op.remove(0));

    final IntOpenHashSet updates = new IntOpenHashSet();
    for (int index = 0; index < 24; ++index) {
      final String cmd = op.remove(0);
      Assert.assertThat(cmd, new StringStartsWith("onInstanceBatchedUpdate "));
      final String[] segments = cmd.split("\\s+");
      final int k = Integer.parseInt(segments[1]);
      Assert.assertFalse(updates.contains(k));
      updates.add(k);
    }

    Assert.assertEquals("onInstanceBatchedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatched 0", op.remove(0));
    Assert.assertEquals("onInstanceBatched 5", op.remove(0));
    Assert.assertEquals("onInstanceBatched 1", op.remove(0));
    Assert.assertEquals("onInstanceBatched 3", op.remove(0));
    Assert.assertEquals("onInstanceBatched 4", op.remove(0));
    Assert.assertEquals("onInstanceBatched 2", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialStart 0 1", op.remove(0));
    Assert.assertEquals("onInstanceBatched 9", op.remove(0));
    Assert.assertEquals("onInstanceBatched 6", op.remove(0));
    Assert.assertEquals("onInstanceBatched 11", op.remove(0));
    Assert.assertEquals("onInstanceBatched 10", op.remove(0));
    Assert.assertEquals("onInstanceBatched 7", op.remove(0));
    Assert.assertEquals("onInstanceBatched 8", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialFinish 0 1", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderFinish 0", op.remove(0));

    Assert.assertEquals("onInstanceBatchedShaderStart 1", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialStart 1 3", op.remove(0));
    Assert.assertEquals("onInstanceBatched 21", op.remove(0));
    Assert.assertEquals("onInstanceBatched 20", op.remove(0));
    Assert.assertEquals("onInstanceBatched 22", op.remove(0));
    Assert.assertEquals("onInstanceBatched 19", op.remove(0));
    Assert.assertEquals("onInstanceBatched 18", op.remove(0));
    Assert.assertEquals("onInstanceBatched 23", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialFinish 1 3", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialStart 1 2", op.remove(0));
    Assert.assertEquals("onInstanceBatched 15", op.remove(0));
    Assert.assertEquals("onInstanceBatched 14", op.remove(0));
    Assert.assertEquals("onInstanceBatched 16", op.remove(0));
    Assert.assertEquals("onInstanceBatched 17", op.remove(0));
    Assert.assertEquals("onInstanceBatched 12", op.remove(0));
    Assert.assertEquals("onInstanceBatched 13", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialFinish 1 2", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderFinish 1", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  private static abstract class UnreachableConsumer
    implements R2DepthInstancesConsumerType
  {
    @Override
    public void onStart()
    {
      throw new UnreachableCodeException();
    }

    @Override
    public void onInstanceBatchedUpdate(
      final R2InstanceBatchedType i)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBatchedShaderStart(
      final R2ShaderInstanceBatchedUsableType<M> s)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBatchedMaterialStart(
      final R2MaterialOpaqueBatchedType<M> material)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBatched(
      final R2MaterialOpaqueBatchedType<M> material,
      final R2InstanceBatchedType i)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBatchedMaterialFinish(
      final R2MaterialOpaqueBatchedType<M> material)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBatchedShaderFinish(
      final R2ShaderInstanceBatchedUsableType<M> s)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceSingleShaderStart(
      final R2ShaderInstanceSingleUsableType<M> s)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceSingleMaterialStart(
      final R2MaterialOpaqueSingleType<M> material)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public void onInstanceSingleArrayStart(
      final R2InstanceSingleType i)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceSingle(
      final R2MaterialOpaqueSingleType<M> material,
      final R2InstanceSingleType i)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceSingleMaterialFinish(
      final R2MaterialOpaqueSingleType<M> material)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceSingleShaderFinish(
      final R2ShaderInstanceSingleUsableType<M> s)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public void onFinish()
    {
      throw new UnreachableCodeException();
    }
  }

}
