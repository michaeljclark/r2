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

package com.io7m.r2.shaders.refraction;

import com.io7m.jtensors.core.parameterized.vectors.PVector3D;
import com.io7m.r2.annotations.R2ImmutableStyleType;
import com.io7m.r2.spaces.R2SpaceRGBType;
import com.io7m.r2.textures.R2Texture2DUsableType;
import org.immutables.value.Value;

/**
 * Parameters for the refractive masked delta shader.
 */

@R2ImmutableStyleType
@Value.Immutable
public interface R2RefractionMaskedDeltaParametersType
{
  /**
   * The scale value applied to vectors for the refraction effect. The effect
   * works purely in screen-space and passing in a too-large value here will
   * typically result in artifacts.
   *
   * @return A value by which to scale delta vectors for refraction
   */

  @Value.Parameter
  @Value.Default
  default double scale()
  {
    return 0.05;
  }

  /**
   * @return A color value by which to multiply the final refracted image
   */

  @Value.Parameter
  @Value.Default
  default PVector3D<R2SpaceRGBType> color()
  {
    return PVector3D.of(1.0, 1.0, 1.0);
  }

  /**
   * @return The scene texture that will be used as the source of refraction
   */

  @Value.Parameter
  R2Texture2DUsableType sceneTexture();

  /**
   * @return The mask texture that will be used to control sampling
   */

  @Value.Parameter
  R2Texture2DUsableType maskTexture();

  /**
   * @return The delta texture that will be used as the source of refraction
   * vectors
   */

  @Value.Parameter
  R2Texture2DUsableType deltaTexture();
}
