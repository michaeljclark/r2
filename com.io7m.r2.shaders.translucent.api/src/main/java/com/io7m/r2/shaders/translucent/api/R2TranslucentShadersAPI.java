/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

package com.io7m.r2.shaders.translucent.api;

import com.io7m.sombrero.core.SoShaderModule;
import com.io7m.sombrero.core.SoShaderModuleProviderAbstract;
import com.io7m.sombrero.core.SoShaderModuleProviderType;
import com.io7m.sombrero.core.SoShaderStoreResource;
import org.osgi.service.component.annotations.Component;

/**
 * Shader module provider.
 */

@Component(service = SoShaderModuleProviderType.class)
public final class R2TranslucentShadersAPI extends SoShaderModuleProviderAbstract
{
  /**
   * Construct the module provider.
   */

  public R2TranslucentShadersAPI()
  {
    super(
      SoShaderModule.of(
        "com.io7m.r2.shaders.translucent.api",
        SoShaderStoreResource.create(
          "/com/io7m/r2/shaders/translucent/api",
          R2TranslucentShadersAPI.class::getResource))
    );
  }
}
