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

package com.io7m.r2.tests.rendering.geometry.api;

import com.io7m.jcanephora.core.JCGLArrayObjectType;
import com.io7m.jcanephora.core.JCGLArrayObjectUsableType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r2.instances.R2ExceptionInstanceAlreadyVisible;
import com.io7m.r2.instances.R2InstanceBatchedType;
import com.io7m.r2.instances.R2InstanceBillboardedType;
import com.io7m.r2.instances.R2InstanceSingleType;
import com.io7m.r2.rendering.geometry.R2SceneOpaques;
import com.io7m.r2.rendering.geometry.api.R2MaterialOpaqueBatchedType;
import com.io7m.r2.rendering.geometry.api.R2MaterialOpaqueBillboardedType;
import com.io7m.r2.rendering.geometry.api.R2MaterialOpaqueSingleType;
import com.io7m.r2.rendering.geometry.api.R2SceneOpaquesConsumerType;
import com.io7m.r2.rendering.geometry.api.R2SceneOpaquesReadableType;
import com.io7m.r2.rendering.geometry.api.R2SceneOpaquesType;
import com.io7m.r2.shaders.api.R2MaterialType;
import com.io7m.r2.shaders.api.R2ShaderInstanceBatchedUsableType;
import com.io7m.r2.shaders.api.R2ShaderInstanceBillboardedUsableType;
import com.io7m.r2.shaders.api.R2ShaderInstanceSingleUsableType;
import com.io7m.r2.shaders.api.R2ShaderUsableType;
import com.io7m.r2.shaders.geometry.api.R2ShaderGeometryBatchedUsableType;
import com.io7m.r2.shaders.geometry.api.R2ShaderGeometryBillboardedUsableType;
import com.io7m.r2.shaders.geometry.api.R2ShaderGeometrySingleUsableType;
import com.io7m.r2.tests.core.R2TestUtilities;
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

public final class R2SceneOpaquesTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(R2SceneOpaquesTest.class);
  }

  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testEmpty()
  {
    final AtomicBoolean finished = new AtomicBoolean(false);
    final AtomicBoolean started = new AtomicBoolean(false);
    final R2SceneOpaquesReadableType o = R2SceneOpaques.create();

    o.opaquesExecute(new UnreachableConsumer()
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
    final R2SceneOpaquesType o = R2SceneOpaques.create();
    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final R2InstanceSingleType i =
      R2TestUtilities.getInstanceSingle(g, a0, 0L);
    final R2ShaderGeometrySingleUsableType<Object> s =
      R2TestUtilities.getShaderInstanceSingle(g, 0L);
    final R2MaterialOpaqueSingleType<Object> m0 =
      R2TestUtilities.getMaterialSingle(g, s, new Object(), 0L);
    final R2MaterialOpaqueSingleType<Object> m1 =
      R2TestUtilities.getMaterialSingle(g, s, new Object(), 1L);

    o.opaquesAddSingleInstance(i, m0);
    this.expected.expect(R2ExceptionInstanceAlreadyVisible.class);
    o.opaquesAddSingleInstance(i, m1);
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

    final R2ShaderGeometrySingleUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceSingle(g, 0L);
    final R2ShaderGeometrySingleUsableType<Object> s1 =
      R2TestUtilities.getShaderInstanceSingle(g, 1L);

    final R2MaterialOpaqueSingleType<Object> m0 =
      R2TestUtilities.getMaterialSingle(g, s0, new Object(), 0L);
    final R2MaterialOpaqueSingleType<Object> m1 =
      R2TestUtilities.getMaterialSingle(g, s0, new Object(), 1L);
    final R2MaterialOpaqueSingleType<Object> m2 =
      R2TestUtilities.getMaterialSingle(g, s1, new Object(), 2L);
    final R2MaterialOpaqueSingleType<Object> m3 =
      R2TestUtilities.getMaterialSingle(g, s1, new Object(), 3L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddSingleInstance(i0a0, m0);
    o.opaquesAddSingleInstance(i1a0, m0);
    o.opaquesAddSingleInstance(i2a0, m0);
    o.opaquesAddSingleInstance(i3a1, m0);
    o.opaquesAddSingleInstance(i4a1, m0);
    o.opaquesAddSingleInstance(i5a1, m0);

    o.opaquesAddSingleInstance(i6a0, m1);
    o.opaquesAddSingleInstance(i7a0, m1);
    o.opaquesAddSingleInstance(i8a0, m1);
    o.opaquesAddSingleInstance(i9a1, m1);
    o.opaquesAddSingleInstance(i10a1, m1);
    o.opaquesAddSingleInstance(i11a1, m1);

    o.opaquesAddSingleInstance(i12a0, m2);
    o.opaquesAddSingleInstance(i13a0, m2);
    o.opaquesAddSingleInstance(i14a0, m2);
    o.opaquesAddSingleInstance(i15a1, m2);
    o.opaquesAddSingleInstance(i16a1, m2);
    o.opaquesAddSingleInstance(i17a1, m2);

    o.opaquesAddSingleInstance(i18a0, m3);
    o.opaquesAddSingleInstance(i19a0, m3);
    o.opaquesAddSingleInstance(i20a0, m3);
    o.opaquesAddSingleInstance(i21a1, m3);
    o.opaquesAddSingleInstance(i22a1, m3);
    o.opaquesAddSingleInstance(i23a1, m3);

    Assert.assertEquals(24L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals(4L, (long) a0.glName());
    Assert.assertEquals(7L, (long) a1.glName());

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onStartGroup 1", op.remove(0));
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
    Assert.assertEquals("onFinishGroup 1", op.remove(0));
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
    final R2ShaderGeometrySingleUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceSingle(g, 0L);
    final R2MaterialOpaqueSingleType<Object> m0 =
      R2TestUtilities.getMaterialSingle(g, s0, new Object(), 0L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddSingleInstance(i0a0, m0);
    Assert.assertEquals(1L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onStartGroup 1", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 4", op.remove(0));
    Assert.assertEquals("onInstanceSingle 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 1", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());

    o.opaquesReset();
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  @Test
  public void testSingleGroups()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceSingleType i0a0 =
      R2TestUtilities.getInstanceSingle(g, a0, 0L);
    final R2InstanceSingleType i0a1 =
      R2TestUtilities.getInstanceSingle(g, a1, 1L);

    final R2ShaderGeometrySingleUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceSingle(g, 0L);
    final R2MaterialOpaqueSingleType<Object> m0 =
      R2TestUtilities.getMaterialSingle(g, s0, new Object(), 0L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddSingleInstance(i0a0, m0);
    o.opaquesAddSingleInstanceInGroup(i0a1, m0, 3);
    Assert.assertEquals(2L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onStartGroup 1", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 4", op.remove(0));
    Assert.assertEquals("onInstanceSingle 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 1", op.remove(0));

    Assert.assertEquals("onStartGroup 3", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleArrayStart 7", op.remove(0));
    Assert.assertEquals("onInstanceSingle 1", op.remove(0));
    Assert.assertEquals("onInstanceSingleMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceSingleShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 3", op.remove(0));

    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());

    o.opaquesReset();
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  @Test
  public void testBatchedAlreadyVisible()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();
    final R2SceneOpaquesType o = R2SceneOpaques.create();
    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final R2InstanceBatchedType i =
      R2TestUtilities.getInstanceBatched(g, a0, 0L);
    final R2ShaderGeometryBatchedUsableType<Object> s =
      R2TestUtilities.getShaderInstanceBatched(g, 0L);
    final R2MaterialOpaqueBatchedType<Object> m0 =
      R2TestUtilities.getMaterialBatched(g, s, new Object(), 0L);
    final R2MaterialOpaqueBatchedType<Object> m1 =
      R2TestUtilities.getMaterialBatched(g, s, new Object(), 1L);

    o.opaquesAddBatchedInstance(i, m0);
    this.expected.expect(R2ExceptionInstanceAlreadyVisible.class);
    o.opaquesAddBatchedInstance(i, m1);
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
    final R2ShaderGeometryBatchedUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceBatched(g, 0L);
    final R2MaterialOpaqueBatchedType<Object> m0 =
      R2TestUtilities.getMaterialBatched(g, s0, new Object(), 0L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddBatchedInstance(i0a0, m0);
    Assert.assertEquals(1L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onInstanceBatchedUpdate 0", op.remove(0));
    Assert.assertEquals("onStartGroup 1", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatched 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 1", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());

    o.opaquesReset();
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  @Test
  public void testBatchedGroups()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceBatchedType i0a0 =
      R2TestUtilities.getInstanceBatched(g, a0, 0L);
    final R2InstanceBatchedType i0a1 =
      R2TestUtilities.getInstanceBatched(g, a1, 1L);

    final R2ShaderGeometryBatchedUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceBatched(g, 0L);
    final R2MaterialOpaqueBatchedType<Object> m0 =
      R2TestUtilities.getMaterialBatched(g, s0, new Object(), 0L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddBatchedInstance(i0a0, m0);
    o.opaquesAddBatchedInstanceInGroup(i0a1, m0, 3);
    Assert.assertEquals(2L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onInstanceBatchedUpdate 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedUpdate 1", op.remove(0));

    Assert.assertEquals("onStartGroup 1", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatched 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 1", op.remove(0));

    Assert.assertEquals("onStartGroup 3", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatched 1", op.remove(0));
    Assert.assertEquals("onInstanceBatchedMaterialFinish 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBatchedShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 3", op.remove(0));

    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());

    o.opaquesReset();
    o.opaquesExecute(cc);

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

    final R2ShaderGeometryBatchedUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceBatched(g, 0L);
    final R2ShaderGeometryBatchedUsableType<Object> s1 =
      R2TestUtilities.getShaderInstanceBatched(g, 1L);

    final R2MaterialOpaqueBatchedType<Object> m0 =
      R2TestUtilities.getMaterialBatched(g, s0, new Object(), 0L);
    final R2MaterialOpaqueBatchedType<Object> m1 =
      R2TestUtilities.getMaterialBatched(g, s0, new Object(), 1L);
    final R2MaterialOpaqueBatchedType<Object> m2 =
      R2TestUtilities.getMaterialBatched(g, s1, new Object(), 2L);
    final R2MaterialOpaqueBatchedType<Object> m3 =
      R2TestUtilities.getMaterialBatched(g, s1, new Object(), 3L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddBatchedInstance(i0a0, m0);
    o.opaquesAddBatchedInstance(i1a0, m0);
    o.opaquesAddBatchedInstance(i2a0, m0);
    o.opaquesAddBatchedInstance(i3a1, m0);
    o.opaquesAddBatchedInstance(i4a1, m0);
    o.opaquesAddBatchedInstance(i5a1, m0);

    o.opaquesAddBatchedInstance(i6a0, m1);
    o.opaquesAddBatchedInstance(i7a0, m1);
    o.opaquesAddBatchedInstance(i8a0, m1);
    o.opaquesAddBatchedInstance(i9a1, m1);
    o.opaquesAddBatchedInstance(i10a1, m1);
    o.opaquesAddBatchedInstance(i11a1, m1);

    o.opaquesAddBatchedInstance(i12a0, m2);
    o.opaquesAddBatchedInstance(i13a0, m2);
    o.opaquesAddBatchedInstance(i14a0, m2);
    o.opaquesAddBatchedInstance(i15a1, m2);
    o.opaquesAddBatchedInstance(i16a1, m2);
    o.opaquesAddBatchedInstance(i17a1, m2);

    o.opaquesAddBatchedInstance(i18a0, m3);
    o.opaquesAddBatchedInstance(i19a0, m3);
    o.opaquesAddBatchedInstance(i20a0, m3);
    o.opaquesAddBatchedInstance(i21a1, m3);
    o.opaquesAddBatchedInstance(i22a1, m3);
    o.opaquesAddBatchedInstance(i23a1, m3);

    Assert.assertEquals(24L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals(4L, (long) a0.glName());
    Assert.assertEquals(7L, (long) a1.glName());

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

    Assert.assertEquals("onStartGroup 1", op.remove(0));
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
    Assert.assertEquals("onFinishGroup 1", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  @Test
  public void testBillboardedAlreadyVisible()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();
    final R2SceneOpaquesType o = R2SceneOpaques.create();
    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final R2InstanceBillboardedType i =
      R2TestUtilities.getInstanceBillboarded(g, a0, 0L);
    final R2ShaderGeometryBillboardedUsableType<Object> s =
      R2TestUtilities.getShaderInstanceBillboarded(g, 0L);
    final R2MaterialOpaqueBillboardedType<Object> m0 =
      R2TestUtilities.getMaterialBillboarded(g, s, new Object(), 0L);
    final R2MaterialOpaqueBillboardedType<Object> m1 =
      R2TestUtilities.getMaterialBillboarded(g, s, new Object(), 1L);

    o.opaquesAddBillboardedInstance(i, m0);
    this.expected.expect(R2ExceptionInstanceAlreadyVisible.class);
    o.opaquesAddBillboardedInstance(i, m1);
  }

  @Test
  public void testBillboardedReset()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceBillboardedType i0a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 0L);
    final R2ShaderGeometryBillboardedUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceBillboarded(g, 0L);
    final R2MaterialOpaqueBillboardedType<Object> m0 =
      R2TestUtilities.getMaterialBillboarded(g, s0, new Object(), 0L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddBillboardedInstance(i0a0, m0);
    Assert.assertEquals(1L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedUpdate 0", op.remove(0));
    Assert.assertEquals("onStartGroup 1", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 0", op.remove(0));
    Assert.assertEquals(
      "onInstanceBillboardedMaterialFinish 0 0",
      op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 1", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());

    o.opaquesReset();
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  @Test
  public void testBillboardedGroups()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceBillboardedType i0a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 0L);
    final R2InstanceBillboardedType i0a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 1L);

    final R2ShaderGeometryBillboardedUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceBillboarded(g, 0L);
    final R2MaterialOpaqueBillboardedType<Object> m0 =
      R2TestUtilities.getMaterialBillboarded(g, s0, new Object(), 0L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddBillboardedInstance(i0a0, m0);
    o.opaquesAddBillboardedInstanceInGroup(i0a1, m0, 3);
    Assert.assertEquals(2L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedUpdate 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedUpdate 1", op.remove(0));

    Assert.assertEquals("onStartGroup 1", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 0", op.remove(0));
    Assert.assertEquals(
      "onInstanceBillboardedMaterialFinish 0 0",
      op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 1", op.remove(0));

    Assert.assertEquals("onStartGroup 3", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 1", op.remove(0));
    Assert.assertEquals(
      "onInstanceBillboardedMaterialFinish 0 0",
      op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderFinish 0", op.remove(0));
    Assert.assertEquals("onFinishGroup 3", op.remove(0));

    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());

    o.opaquesReset();
    o.opaquesExecute(cc);

    Assert.assertEquals("onStart", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  @Test
  public void testBillboardedOrdering()
    throws Exception
  {
    final JCGLInterfaceGL33Type g = R2TestUtilities.getFakeGL();

    final JCGLArrayObjectType a0 = R2TestUtilities.getArrayObject(g);
    final JCGLArrayObjectType a1 = R2TestUtilities.getArrayObject(g);

    final R2InstanceBillboardedType i0a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 0L);
    final R2InstanceBillboardedType i1a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 1L);
    final R2InstanceBillboardedType i2a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 2L);

    final R2InstanceBillboardedType i3a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 3L);
    final R2InstanceBillboardedType i4a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 4L);
    final R2InstanceBillboardedType i5a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 5L);

    final R2InstanceBillboardedType i6a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 6L);
    final R2InstanceBillboardedType i7a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 7L);
    final R2InstanceBillboardedType i8a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 8L);

    final R2InstanceBillboardedType i9a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 9L);
    final R2InstanceBillboardedType i10a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 10L);
    final R2InstanceBillboardedType i11a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 11L);

    final R2InstanceBillboardedType i12a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 12L);
    final R2InstanceBillboardedType i13a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 13L);
    final R2InstanceBillboardedType i14a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 14L);

    final R2InstanceBillboardedType i15a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 15L);
    final R2InstanceBillboardedType i16a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 16L);
    final R2InstanceBillboardedType i17a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 17L);

    final R2InstanceBillboardedType i18a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 18L);
    final R2InstanceBillboardedType i19a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 19L);
    final R2InstanceBillboardedType i20a0 =
      R2TestUtilities.getInstanceBillboarded(g, a0, 20L);

    final R2InstanceBillboardedType i21a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 21L);
    final R2InstanceBillboardedType i22a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 22L);
    final R2InstanceBillboardedType i23a1 =
      R2TestUtilities.getInstanceBillboarded(g, a1, 23L);

    final R2ShaderGeometryBillboardedUsableType<Object> s0 =
      R2TestUtilities.getShaderInstanceBillboarded(g, 0L);
    final R2ShaderGeometryBillboardedUsableType<Object> s1 =
      R2TestUtilities.getShaderInstanceBillboarded(g, 1L);

    final R2MaterialOpaqueBillboardedType<Object> m0 =
      R2TestUtilities.getMaterialBillboarded(g, s0, new Object(), 0L);
    final R2MaterialOpaqueBillboardedType<Object> m1 =
      R2TestUtilities.getMaterialBillboarded(g, s0, new Object(), 1L);
    final R2MaterialOpaqueBillboardedType<Object> m2 =
      R2TestUtilities.getMaterialBillboarded(g, s1, new Object(), 2L);
    final R2MaterialOpaqueBillboardedType<Object> m3 =
      R2TestUtilities.getMaterialBillboarded(g, s1, new Object(), 3L);

    final R2SceneOpaquesType o = R2SceneOpaques.create();
    o.opaquesAddBillboardedInstance(i0a0, m0);
    o.opaquesAddBillboardedInstance(i1a0, m0);
    o.opaquesAddBillboardedInstance(i2a0, m0);
    o.opaquesAddBillboardedInstance(i3a1, m0);
    o.opaquesAddBillboardedInstance(i4a1, m0);
    o.opaquesAddBillboardedInstance(i5a1, m0);

    o.opaquesAddBillboardedInstance(i6a0, m1);
    o.opaquesAddBillboardedInstance(i7a0, m1);
    o.opaquesAddBillboardedInstance(i8a0, m1);
    o.opaquesAddBillboardedInstance(i9a1, m1);
    o.opaquesAddBillboardedInstance(i10a1, m1);
    o.opaquesAddBillboardedInstance(i11a1, m1);

    o.opaquesAddBillboardedInstance(i12a0, m2);
    o.opaquesAddBillboardedInstance(i13a0, m2);
    o.opaquesAddBillboardedInstance(i14a0, m2);
    o.opaquesAddBillboardedInstance(i15a1, m2);
    o.opaquesAddBillboardedInstance(i16a1, m2);
    o.opaquesAddBillboardedInstance(i17a1, m2);

    o.opaquesAddBillboardedInstance(i18a0, m3);
    o.opaquesAddBillboardedInstance(i19a0, m3);
    o.opaquesAddBillboardedInstance(i20a0, m3);
    o.opaquesAddBillboardedInstance(i21a1, m3);
    o.opaquesAddBillboardedInstance(i22a1, m3);
    o.opaquesAddBillboardedInstance(i23a1, m3);

    Assert.assertEquals(24L, o.opaquesCount());

    final LoggingConsumer cc = new LoggingConsumer();
    final List<String> op = cc.ops;
    o.opaquesExecute(cc);

    Assert.assertEquals(4L, (long) a0.glName());
    Assert.assertEquals(7L, (long) a1.glName());

    op.stream().forEach(System.out::println);

    Assert.assertEquals("onStart", op.remove(0));

    final IntOpenHashSet updates = new IntOpenHashSet();
    for (int index = 0; index < 24; ++index) {
      final String cmd = op.remove(0);
      Assert.assertThat(
        cmd,
        new StringStartsWith("onInstanceBillboardedUpdate "));
      final String[] segments = cmd.split("\\s+");
      final int k = Integer.parseInt(segments[1]);
      Assert.assertFalse(updates.contains(k));
      updates.add(k);
    }

    Assert.assertEquals("onStartGroup 1", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderStart 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedMaterialStart 0 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 0", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 5", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 1", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 3", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 4", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 2", op.remove(0));
    Assert.assertEquals(
      "onInstanceBillboardedMaterialFinish 0 0",
      op.remove(0));
    Assert.assertEquals("onInstanceBillboardedMaterialStart 0 1", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 9", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 6", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 11", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 10", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 7", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 8", op.remove(0));
    Assert.assertEquals(
      "onInstanceBillboardedMaterialFinish 0 1",
      op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderFinish 0", op.remove(0));

    Assert.assertEquals("onInstanceBillboardedShaderStart 1", op.remove(0));
    Assert.assertEquals("onInstanceBillboardedMaterialStart 1 3", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 21", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 20", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 22", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 19", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 18", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 23", op.remove(0));
    Assert.assertEquals(
      "onInstanceBillboardedMaterialFinish 1 3",
      op.remove(0));
    Assert.assertEquals("onInstanceBillboardedMaterialStart 1 2", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 15", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 14", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 16", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 17", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 12", op.remove(0));
    Assert.assertEquals("onInstanceBillboarded 13", op.remove(0));
    Assert.assertEquals(
      "onInstanceBillboardedMaterialFinish 1 2",
      op.remove(0));
    Assert.assertEquals("onInstanceBillboardedShaderFinish 1", op.remove(0));
    Assert.assertEquals("onFinishGroup 1", op.remove(0));
    Assert.assertEquals("onFinish", op.remove(0));
    Assert.assertTrue(op.isEmpty());
  }

  private static final class LoggingConsumer implements
    R2SceneOpaquesConsumerType
  {
    private final List<String> ops;
    private R2ShaderUsableType<?> shader_current;
    private R2MaterialType<?> material_current;
    private JCGLArrayObjectUsableType array_current;
    private int group;

    LoggingConsumer()
    {
      this.ops = new ArrayList<>(256);
      this.group = -1;
    }

    @Override
    public void onStart()
    {
      this.ops.add("onStart");
    }

    @Override
    public void onStartGroup(final int g)
    {
      this.group = g;
      this.ops.add(String.format(
        "onStartGroup %d", Integer.valueOf(g)));
    }

    @Override
    public void onFinishGroup(final int g)
    {
      Assert.assertEquals((long) g, (long) this.group);
      this.group = -1;
      this.ops.add(String.format(
        "onFinishGroup %d", Integer.valueOf(g)));
    }

    @Override
    public void onInstanceBatchedUpdate(
      final R2InstanceBatchedType i)
    {
      this.ops.add(String.format(
        "onInstanceBatchedUpdate %d",
        Long.valueOf(i.instanceID())));
    }

    @Override
    public <M> void onInstanceBatchedShaderStart(
      final R2ShaderInstanceBatchedUsableType<M> s)
    {
      this.shader_current = s;
      this.ops.add(String.format(
        "onInstanceBatchedShaderStart %d",
        Long.valueOf(s.shaderID())));
    }

    @Override
    public <M> void onInstanceBatchedMaterialStart(
      final R2MaterialOpaqueBatchedType<M> material)
    {
      final R2ShaderInstanceBatchedUsableType<M> s = material.shader();
      Assert.assertEquals(this.shader_current, s);
      this.material_current = material;
      this.ops.add(String.format(
        "onInstanceBatchedMaterialStart %d %d",
        Long.valueOf(s.shaderID()),
        Long.valueOf(material.materialID())));
    }

    @Override
    public <M> void onInstanceBatched(
      final R2MaterialOpaqueBatchedType<M> material,
      final R2InstanceBatchedType i)
    {
      final R2ShaderInstanceBatchedUsableType<M> s = material.shader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      this.ops.add(String.format(
        "onInstanceBatched %d",
        Long.valueOf(i.instanceID())));
    }

    @Override
    public <M> void onInstanceBatchedMaterialFinish(
      final R2MaterialOpaqueBatchedType<M> material)
    {
      final R2ShaderInstanceBatchedUsableType<M> s = material.shader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      this.material_current = null;
      this.ops.add(String.format(
        "onInstanceBatchedMaterialFinish %d %d",
        Long.valueOf(s.shaderID()),
        Long.valueOf(material.materialID())));
    }

    @Override
    public <M> void onInstanceBatchedShaderFinish(
      final R2ShaderInstanceBatchedUsableType<M> s)
    {
      Assert.assertEquals(s, this.shader_current);
      this.shader_current = null;
      this.ops.add(String.format(
        "onInstanceBatchedShaderFinish %d",
        Long.valueOf(s.shaderID())));
    }

    @Override
    public void onInstanceBillboardedUpdate(
      final R2InstanceBillboardedType i)
    {
      this.ops.add(String.format(
        "onInstanceBillboardedUpdate %d",
        Long.valueOf(i.instanceID())));
    }

    @Override
    public <M> void onInstanceBillboardedShaderStart(
      final R2ShaderInstanceBillboardedUsableType<M> s)
    {
      this.shader_current = s;
      this.ops.add(String.format(
        "onInstanceBillboardedShaderStart %d",
        Long.valueOf(s.shaderID())));
    }

    @Override
    public <M> void onInstanceBillboardedMaterialStart(
      final R2MaterialOpaqueBillboardedType<M> material)
    {
      final R2ShaderInstanceBillboardedUsableType<M> s = material.shader();
      Assert.assertEquals(this.shader_current, s);
      this.material_current = material;
      this.ops.add(String.format(
        "onInstanceBillboardedMaterialStart %d %d",
        Long.valueOf(s.shaderID()),
        Long.valueOf(material.materialID())));
    }

    @Override
    public <M> void onInstanceBillboarded(
      final R2MaterialOpaqueBillboardedType<M> material,
      final R2InstanceBillboardedType i)
    {
      final R2ShaderInstanceBillboardedUsableType<M> s = material.shader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      this.ops.add(String.format(
        "onInstanceBillboarded %d",
        Long.valueOf(i.instanceID())));
    }

    @Override
    public <M> void onInstanceBillboardedMaterialFinish(
      final R2MaterialOpaqueBillboardedType<M> material)
    {
      final R2ShaderInstanceBillboardedUsableType<M> s = material.shader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      this.material_current = null;
      this.ops.add(String.format(
        "onInstanceBillboardedMaterialFinish %d %d",
        Long.valueOf(s.shaderID()),
        Long.valueOf(material.materialID())));
    }

    @Override
    public <M> void onInstanceBillboardedShaderFinish(
      final R2ShaderInstanceBillboardedUsableType<M> s)
    {
      Assert.assertEquals(s, this.shader_current);
      this.shader_current = null;
      this.ops.add(String.format(
        "onInstanceBillboardedShaderFinish %d",
        Long.valueOf(s.shaderID())));
    }

    @Override
    public <M> void onInstanceSingleShaderStart(
      final R2ShaderInstanceSingleUsableType<M> s)
    {
      this.shader_current = s;
      this.ops.add(String.format(
        "onInstanceSingleShaderStart %d",
        Long.valueOf(s.shaderID())));
    }

    @Override
    public <M> void onInstanceSingleMaterialStart(
      final R2MaterialOpaqueSingleType<M> material)
    {
      final R2ShaderInstanceSingleUsableType<M> s = material.shader();
      Assert.assertEquals(this.shader_current, s);
      this.material_current = material;
      this.ops.add(String.format(
        "onInstanceSingleMaterialStart %d %d",
        Long.valueOf(s.shaderID()),
        Long.valueOf(material.materialID())));
    }

    @Override
    public void onInstanceSingleArrayStart(
      final R2InstanceSingleType i)
    {
      this.array_current = i.arrayObject();
      this.ops.add(String.format(
        "onInstanceSingleArrayStart %d",
        Integer.valueOf(this.array_current.glName())));
    }

    @Override
    public <M> void onInstanceSingle(
      final R2MaterialOpaqueSingleType<M> material,
      final R2InstanceSingleType i)
    {
      final R2ShaderInstanceSingleUsableType<M> s = material.shader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      Assert.assertEquals(i.arrayObject(), this.array_current);
      this.ops.add(String.format(
        "onInstanceSingle %d",
        Long.valueOf(i.instanceID())));
    }

    @Override
    public <M> void onInstanceSingleMaterialFinish(
      final R2MaterialOpaqueSingleType<M> material)
    {
      final R2ShaderInstanceSingleUsableType<M> s = material.shader();
      Assert.assertEquals(s, this.shader_current);
      Assert.assertEquals(material, this.material_current);
      this.material_current = null;
      this.ops.add(String.format(
        "onInstanceSingleMaterialFinish %d %d",
        Long.valueOf(s.shaderID()),
        Long.valueOf(material.materialID())));
    }

    @Override
    public <M> void onInstanceSingleShaderFinish(
      final R2ShaderInstanceSingleUsableType<M> s)
    {
      Assert.assertEquals(s, this.shader_current);
      this.shader_current = null;
      this.ops.add(String.format(
        "onInstanceSingleShaderFinish %d",
        Long.valueOf(s.shaderID())));
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

  private static abstract class UnreachableConsumer
    implements R2SceneOpaquesConsumerType
  {
    @Override
    public void onStart()
    {
      throw new UnreachableCodeException();
    }

    @Override
    public void onStartGroup(final int group)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public void onInstanceBillboardedUpdate(
      final R2InstanceBillboardedType i)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBillboardedShaderStart(
      final R2ShaderInstanceBillboardedUsableType<M> s)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBillboardedMaterialStart(
      final R2MaterialOpaqueBillboardedType<M> material)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBillboarded(
      final R2MaterialOpaqueBillboardedType<M> material,
      final R2InstanceBillboardedType i)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBillboardedMaterialFinish(
      final R2MaterialOpaqueBillboardedType<M> material)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public <M> void onInstanceBillboardedShaderFinish(
      final R2ShaderInstanceBillboardedUsableType<M> s)
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
    public void onFinishGroup(final int group)
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
