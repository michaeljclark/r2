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

package com.io7m.r2.rendering.lights.api;

import com.io7m.r2.lights.R2LightSingleReadableType;
import com.io7m.r2.shaders.light.api.R2ShaderLightSingleUsableType;

/**
 * A consumer for lights within a specific group.
 */

public interface R2SceneLightsGroupConsumerType
{
  /**
   * Called when rendering of the light group begins.
   */

  void onStart();

  /**
   * Called when a new shader should be activated in order to start rendering
   * single lights.
   *
   * @param s   The shader
   * @param <M> The type of shader parameters
   */

  <M extends R2LightSingleReadableType>
  void onLightSingleShaderStart(
    R2ShaderLightSingleUsableType<M> s);

  /**
   * Called when a new array object should be bound, for single lights.
   *
   * @param i The current instance
   */

  void onLightSingleArrayStart(
    R2LightSingleReadableType i);

  /**
   * Called when a single light should be rendered.
   *
   * @param <M> The type of shader parameters
   * @param s   The current shader
   * @param i   The current instance
   */

  <M extends R2LightSingleReadableType> void onLightSingle(
    R2ShaderLightSingleUsableType<M> s,
    M i);

  /**
   * Called when the current shader should be deactivated.
   *
   * @param s   The shader
   * @param <M> The type of shader parameters
   */

  <M extends R2LightSingleReadableType>
  void onLightSingleShaderFinish(
    R2ShaderLightSingleUsableType<M> s);

  /**
   * Called when rendering of the group is finished.
   */

  void onFinish();
}
