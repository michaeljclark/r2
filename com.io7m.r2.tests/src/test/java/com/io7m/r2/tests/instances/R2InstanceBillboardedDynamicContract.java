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

package com.io7m.r2.tests.instances;

import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jtensors.core.parameterized.vectors.PVector3D;
import com.io7m.r2.core.api.ids.R2IDPool;
import com.io7m.r2.core.api.ids.R2IDPoolType;
import com.io7m.r2.instances.R2ExceptionInstanceBatchIsFull;
import com.io7m.r2.instances.R2InstanceBillboardedDynamic;
import com.io7m.r2.instances.R2InstanceBillboardedDynamicType;
import com.io7m.r2.tests.R2JCGLContract;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class R2InstanceBillboardedDynamicContract
  extends R2JCGLContract
{
  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testTooMany()
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g33 = c.contextGetGL33();
    final R2IDPoolType id_pool = R2IDPool.newPool();

    final R2InstanceBillboardedDynamicType i =
      R2InstanceBillboardedDynamic.create(
        id_pool,
        g33.arrayBuffers(),
        g33.arrayObjects(),
        8);

    for (int index = 0; index < 8; ++index) {
      i.addInstance(PVector3D.of(0.0, 0.0, 0.0), 1.0, 0.0);
    }

    this.expected.expect(R2ExceptionInstanceBatchIsFull.class);
    i.addInstance(PVector3D.of(0.0, 0.0, 0.0), 1.0, 0.0);
  }

  @Test
  public void testUpdateRequired()
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);
    final JCGLInterfaceGL33Type g33 = c.contextGetGL33();
    final R2IDPoolType id_pool = R2IDPool.newPool();

    final R2InstanceBillboardedDynamicType i =
      R2InstanceBillboardedDynamic.create(
        id_pool,
        g33.arrayBuffers(),
        g33.arrayObjects(),
        8);

    Assert.assertTrue(i.updateRequired());
    i.update(g33);
    Assert.assertFalse(i.updateRequired());

    i.addInstance(PVector3D.of(23.0, 23.0, 23.0), 1.0, 0.0);

    Assert.assertTrue(i.updateRequired());
    i.update(g33);
    Assert.assertFalse(i.updateRequired());

    i.clear();
    Assert.assertTrue(i.updateRequired());
    i.update(g33);
    Assert.assertFalse(i.updateRequired());
  }
}
