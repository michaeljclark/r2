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

package com.io7m.r2.filters.ssao.api;

import com.io7m.r2.annotations.R2ImmutableStyleType;
import com.io7m.r2.matrices.R2MatricesObserverValuesType;
import com.io7m.r2.rendering.geometry.api.R2GeometryBufferUsableType;
import com.io7m.r2.textures.R2Texture2DUsableType;
import org.immutables.value.Value;

/**
 * The type of parameters for filters that calculate ambient occlusion in
 * screen-space.
 */

@Value.Immutable
@R2ImmutableStyleType
public interface R2FilterSSAOParametersType
{
  /**
   * @return The exponent that will be used to scale the final occlusion term
   */

  @Value.Parameter
  @Value.Default
  default double exponent()
  {
    return 1.0;
  }

  /**
   * @return The noise texture that will be used to peturb sampling when
   * calculating the occlusion term
   */

  @Value.Parameter
  R2Texture2DUsableType noiseTexture();

  /**
   * @return The sampling kernel
   */

  @Value.Parameter
  R2SSAOKernelType kernel();

  /**
   * @return The maximum distance, in eye-space units, from which samples will
   * be taken around each sampling point
   */

  @Value.Parameter
  @Value.Default
  default double sampleRadius()
  {
    return 0.25;
  }

  /**
   * @return The geometry buffer that will be sampled
   */

  @Value.Parameter
  R2GeometryBufferUsableType geometryBuffer();

  /**
   * @return The ambient occlusion buffer that will contain the results of the
   * filter operation
   */

  @Value.Parameter
  R2AmbientOcclusionBufferUsableType outputBuffer();

  /**
   * @return The observer values that were used to produce the original scene
   */

  @Value.Parameter
  R2MatricesObserverValuesType sceneObserverValues();
}
