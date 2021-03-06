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

package com.io7m.r2.instances;

import com.io7m.jcanephora.core.JCGLArrayObjectType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;

/**
 * <p>The type of billboarded instances.</p>
 *
 * <p>A billboarded instance consists of a set of one or more vertex buffers
 * containing a list of world-space positions. At render-time, the positions are
 * transformed into quadrilaterals (pairs of triangles) that are oriented to
 * face the viewer at all times.</p>
 *
 * <p>The primary use case for billboarded instances is rendering particle
 * systems.</p>
 */

public interface R2InstanceBillboardedType extends R2InstanceType
{
  /**
   * @return The instance array object
   */

  JCGLArrayObjectType arrayObject();

  /**
   * Update any data required for rendering on the GPU.
   *
   * @param g An OpenGL interface
   */

  void update(
    JCGLInterfaceGL33Type g);

  /**
   * @return {@code true} if the instance data has changed since the last call
   * to {{@link #update(JCGLInterfaceGL33Type)}}
   */

  boolean updateRequired();

  /**
   * @return The number of instances currently present in the set
   */

  int enabledCount();
}
