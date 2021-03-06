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

package com.io7m.r2.core.api.deletable;

import com.io7m.jcanephora.core.JCGLResourceUsableType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.r2.core.api.R2Exception;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * The type of objects that can be deleted, and that require access to OpenGL to
 * perform the deletion.
 */

public interface R2DeletableType extends JCGLResourceUsableType
{
  /**
   * Wrap the given consumer as a deleteable object.
   *
   * @param c A consumer
   *
   * @return A wrapped consumer
   */

  static R2DeletableType wrap(
    final Consumer<JCGLInterfaceGL33Type> c)
  {
    return new R2DeletableType()
    {
      private final AtomicBoolean deleted =
        new AtomicBoolean(false);

      @Override
      public boolean isDeleted()
      {
        return this.deleted.get();
      }

      @Override
      public void delete(final JCGLInterfaceGL33Type g)
        throws R2Exception
      {
        c.accept(g);
        this.deleted.set(true);
      }
    };
  }

  /**
   * Delete the current object.
   *
   * @param g An OpenGL interface
   *
   * @throws R2Exception On errors
   */

  void delete(JCGLInterfaceGL33Type g)
    throws R2Exception;
}
