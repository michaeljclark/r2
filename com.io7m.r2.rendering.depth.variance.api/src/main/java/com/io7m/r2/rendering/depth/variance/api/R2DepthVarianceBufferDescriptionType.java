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

package com.io7m.r2.rendering.depth.variance.api;

import com.io7m.jcanephora.core.JCGLTextureFilterMagnification;
import com.io7m.jcanephora.core.JCGLTextureFilterMinification;
import com.io7m.jregions.core.unparameterized.sizes.AreaSizeL;
import com.io7m.r2.annotations.R2ImmutableStyleType;
import com.io7m.r2.rendering.targets.R2RenderTargetDescriptionType;
import com.io7m.r2.rendering.depth.api.R2DepthPrecision;
import org.immutables.value.Value;

/**
 * The type of depth variance buffer descriptions.
 */

@Value.Immutable
@R2ImmutableStyleType
public interface R2DepthVarianceBufferDescriptionType
  extends R2RenderTargetDescriptionType
{
  @Override
  @Value.Parameter
  AreaSizeL area();

  /**
   * @return The magnification filter used for the buffer
   */

  @Value.Parameter
  @Value.Default
  default JCGLTextureFilterMagnification magnificationFilter()
  {
    return JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR;
  }

  /**
   * @return The minification filter used for the buffer
   */

  @Value.Parameter
  @Value.Default
  default JCGLTextureFilterMinification minificationFilter()
  {
    return JCGLTextureFilterMinification.TEXTURE_FILTER_LINEAR_MIPMAP_LINEAR;
  }

  /**
   * @return The precision of the depth attachment
   */

  @Value.Parameter
  @Value.Default
  default R2DepthPrecision depthPrecision()
  {
    return R2DepthPrecision.R2_DEPTH_PRECISION_24;
  }

  /**
   * @return The precision of the depth-variance attachment
   */

  @Value.Parameter
  @Value.Default
  default R2DepthVariancePrecision depthVariancePrecision()
  {
    return R2DepthVariancePrecision.R2_DEPTH_VARIANCE_PRECISION_16;
  }
}
