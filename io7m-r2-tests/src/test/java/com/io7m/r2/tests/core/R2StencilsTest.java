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

import com.io7m.r2.core.R2ExceptionInvalidGroup;
import com.io7m.r2.core.R2Stencils;
import org.junit.Assert;
import org.junit.Test;

public final class R2StencilsTest
{
  @Test(expected = R2ExceptionInvalidGroup.class)
  public void testInvalidGroup0()
  {
    R2Stencils.checkValidGroup(0);
  }

  @Test(expected = R2ExceptionInvalidGroup.class)
  public void testInvalidGroup1()
  {
    R2Stencils.checkValidGroup(16);
  }

  @Test
  public void testValidGroup()
  {
    for (int index = 1; index <= 15; ++index) {
      Assert.assertEquals(
        (long) index, (long) R2Stencils.checkValidGroup(index));
    }
  }
}
