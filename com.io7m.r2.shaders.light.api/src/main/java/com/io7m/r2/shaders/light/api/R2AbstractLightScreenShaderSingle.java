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

package com.io7m.r2.shaders.light.api;

import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jfsm.core.FSMEnumMutable;
import com.io7m.jfsm.core.FSMEnumMutableBuilderType;
import com.io7m.jnull.NullCheck;
import com.io7m.r2.core.api.ids.R2IDPoolType;
import com.io7m.r2.lights.R2LightSingleReadableType;
import com.io7m.r2.rendering.geometry.api.R2GeometryBufferUsableType;
import com.io7m.r2.shaders.api.R2AbstractShader;
import com.io7m.r2.shaders.api.R2ExceptionShaderPreprocessingFailed;
import com.io7m.r2.shaders.api.R2ShaderPreprocessingEnvironmentReadableType;
import com.io7m.r2.shaders.api.R2ShaderStateChecking;

import java.util.Optional;

/**
 * An abstract shader implementation that checks state transitions for
 * correctness.
 *
 * @param <M> The type of shader parameters
 */

public abstract class R2AbstractLightScreenShaderSingle<M extends R2LightSingleReadableType>
  extends R2AbstractShader<R2AbstractLightScreenShaderSingle.State, M>
  implements R2ShaderLightScreenSingleType<M>
{
  private final FSMEnumMutable<State> state;
  private final R2ShaderStateChecking check;

  protected R2AbstractLightScreenShaderSingle(
    final JCGLShadersType in_shaders,
    final R2ShaderPreprocessingEnvironmentReadableType in_shader_env,
    final R2IDPoolType in_pool,
    final String in_name,
    final String in_vertex,
    final Optional<String> in_geometry,
    final String in_fragment,
    final R2ShaderStateChecking in_check)
    throws R2ExceptionShaderPreprocessingFailed
  {
    super(
      in_shaders,
      in_shader_env,
      in_pool,
      in_name,
      in_vertex,
      in_geometry,
      in_fragment);

    this.check = NullCheck.notNull(in_check, "Check");

    final FSMEnumMutableBuilderType<State> sb =
      FSMEnumMutable.builder(State.STATE_DEACTIVATED);

    sb.addTransition(
      State.STATE_DEACTIVATED, State.STATE_ACTIVATED);
    sb.addTransition(
      State.STATE_ACTIVATED, State.STATE_GEOMETRY_BUFFER_RECEIVED);
    sb.addTransition(
      State.STATE_GEOMETRY_BUFFER_RECEIVED, State.STATE_VALUES_RECEIVED);

    for (final State target : State.values()) {
      if (target != State.STATE_DEACTIVATED) {
        sb.addTransition(target, State.STATE_DEACTIVATED);
      }
    }

    this.state = sb.build();
  }

  protected abstract void onActualReceiveBoundGeometryBufferTextures(
    JCGLInterfaceGL33Type g,
    R2GeometryBufferUsableType gbuffer,
    JCGLTextureUnitType unit_albedo,
    JCGLTextureUnitType unit_specular,
    JCGLTextureUnitType unit_depth,
    JCGLTextureUnitType unit_normals);

  protected abstract void onActualReceiveValues(
    JCGLInterfaceGL33Type g,
    R2ShaderParametersLightType<M> light_parameters);

  @Override
  public final void onReceiveBoundGeometryBufferTextures(
    final JCGLInterfaceGL33Type g,
    final R2GeometryBufferUsableType gbuffer,
    final JCGLTextureUnitType unit_albedo,
    final JCGLTextureUnitType unit_specular,
    final JCGLTextureUnitType unit_depth,
    final JCGLTextureUnitType unit_normals)
  {
    NullCheck.notNull(g, "G33");
    NullCheck.notNull(gbuffer, "G-Buffer");
    NullCheck.notNull(unit_albedo, "Albedo");
    NullCheck.notNull(unit_depth, "Depth");
    NullCheck.notNull(unit_normals, "Normals");
    NullCheck.notNull(unit_specular, "Specular");
    this.state.transition(State.STATE_GEOMETRY_BUFFER_RECEIVED);
    this.onActualReceiveBoundGeometryBufferTextures(
      g, gbuffer, unit_albedo, unit_specular, unit_depth, unit_normals);
  }

  @Override
  public final void onReceiveValues(
    final JCGLInterfaceGL33Type g,
    final R2ShaderParametersLightType<M> light_parameters)
  {
    this.state.transition(State.STATE_VALUES_RECEIVED);
    this.onActualReceiveValues(g, light_parameters);
  }

  @Override
  protected final FSMEnumMutable<State> onCheckGetFSM()
  {
    return this.state;
  }

  @Override
  protected final void onCheckActivated()
  {
    this.state.transition(State.STATE_ACTIVATED);
  }

  @Override
  protected final void onCheckDeactivated()
  {
    this.state.transition(State.STATE_DEACTIVATED);
  }

  @Override
  protected final void onCheckValidated()
  {
    this.state.transition(State.STATE_VALIDATED);
  }

  protected enum State
  {
    STATE_DEACTIVATED,
    STATE_ACTIVATED,
    STATE_GEOMETRY_BUFFER_RECEIVED,
    STATE_VALUES_RECEIVED,
    STATE_VALIDATED
  }
}
