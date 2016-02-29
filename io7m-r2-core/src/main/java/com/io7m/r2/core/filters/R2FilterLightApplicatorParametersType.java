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

package com.io7m.r2.core.filters;

import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.r2.core.R2CopyDepth;
import com.io7m.r2.core.R2GeometryBufferUsableType;
import com.io7m.r2.core.R2ImmutableStyleType;
import com.io7m.r2.core.R2LightBufferUsableType;
import org.immutables.value.Value;

/**
 * The type of light applicator filter parameters.
 */

@Value.Immutable
@Value.Modifiable
@R2ImmutableStyleType
public interface R2FilterLightApplicatorParametersType
{
  /**
   * @return A value specifying whether or not the depth buffer of the scene's
   * geometry buffer should be copied to the output
   */

  @Value.Parameter
  @Value.Default
  default R2CopyDepth getCopyDepth()
  {
    return R2CopyDepth.R2_COPY_DEPTH_ENABLED;
  }

  /**
   * @return The geometry buffer that will be used to produce a lit image
   */

  @Value.Parameter
  R2GeometryBufferUsableType getGeometryBuffer();

  /**
   * @return The light buffer that will be used to produce a lit image
   */

  @Value.Parameter
  R2LightBufferUsableType getLightBuffer();

  /**
   * @return The size of the current viewport
   */

  @Value.Parameter
  AreaInclusiveUnsignedLType getOutputViewport();
}
