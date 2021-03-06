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

package com.io7m.r2.textures;

import com.io7m.jcanephora.core.JCGLTextureCubeType;
import com.io7m.jcanephora.core.JCGLTextureCubeUsableType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.r2.annotations.R2ImmutableStyleType;
import com.io7m.r2.core.api.R2Exception;
import org.immutables.value.Value;

import java.util.function.BiFunction;

/**
 * A simple static cube texture.
 */

@R2ImmutableStyleType
@Value.Immutable
public interface R2TextureCubeStaticType extends R2TextureCubeType
{
  @Override
  @Value.Parameter
  JCGLTextureCubeType textureWritable();

  @Override
  default JCGLTextureCubeUsableType texture()
  {
    return this.textureWritable();
  }

  @Override
  default boolean isDeleted()
  {
    return this.textureWritable().isDeleted();
  }

  @Override
  default void delete(
    final JCGLInterfaceGL33Type g)
    throws R2Exception
  {
    if (!this.textureWritable().isDeleted()) {
      final JCGLTexturesType g_tx = g.textures();
      g_tx.textureCubeDelete(this.textureWritable());
    }
  }

  @Override
  default <A, B> B matchTexture(
    final A context,
    final BiFunction<A, R2Texture2DUsableType, B> on_2d,
    final BiFunction<A, R2TextureCubeUsableType, B> on_cube)
  {
    return on_cube.apply(context, this);
  }
}
