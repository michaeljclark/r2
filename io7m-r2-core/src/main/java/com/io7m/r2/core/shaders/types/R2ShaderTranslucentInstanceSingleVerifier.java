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

package com.io7m.r2.core.shaders.types;

import com.io7m.jareas.core.AreaInclusiveUnsignedLType;
import com.io7m.jcanephora.core.JCGLProgramShaderUsableType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLShadersType;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.renderstate.JCGLBlendState;
import com.io7m.jcanephora.texture_unit_allocator.JCGLTextureUnitContextMutableType;
import com.io7m.jfsm.core.FSMEnumMutable;
import com.io7m.jfsm.core.FSMEnumMutableBuilderType;
import com.io7m.jnull.NullCheck;
import com.io7m.r2.core.R2Exception;
import com.io7m.r2.core.R2ExceptionShaderValidationFailed;
import com.io7m.r2.core.R2MatricesInstanceSingleValuesType;
import com.io7m.r2.core.R2MatricesObserverValuesType;

import java.util.Optional;

/**
 * A delegating verifier for translucent single-instance shaders; a type that
 * verifies that a renderer has called all of the required methods in the
 * correct order.
 *
 * @param <M> See {@link R2ShaderTranslucentInstanceSingleType}
 */

public final class R2ShaderTranslucentInstanceSingleVerifier<M> implements
  R2ShaderTranslucentInstanceSingleType<M>
{
  private final R2ShaderTranslucentInstanceSingleType<M> shader;
  private final FSMEnumMutable<State> state;

  private R2ShaderTranslucentInstanceSingleVerifier(
    final R2ShaderTranslucentInstanceSingleType<M> in_shader)
  {
    this.shader = NullCheck.notNull(in_shader);

    final FSMEnumMutableBuilderType<State> sb =
      FSMEnumMutable.builder(State.STATE_DEACTIVATED);

    sb.addTransition(
      State.STATE_DEACTIVATED, State.STATE_ACTIVATED);
    sb.addTransition(
      State.STATE_ACTIVATED, State.STATE_VIEW_RECEIVED);
    sb.addTransition(
      State.STATE_VIEW_RECEIVED, State.STATE_MATERIAL_RECEIVED);
    sb.addTransition(
      State.STATE_MATERIAL_RECEIVED, State.STATE_INSTANCE_RECEIVED);
    sb.addTransition(
      State.STATE_INSTANCE_RECEIVED, State.STATE_VALIDATED);

    sb.addTransition(
      State.STATE_VALIDATED, State.STATE_MATERIAL_RECEIVED);
    sb.addTransition(
      State.STATE_VALIDATED, State.STATE_INSTANCE_RECEIVED);

    for (final State target : State.values()) {
      if (target != State.STATE_DEACTIVATED) {
        sb.addTransition(target, State.STATE_DEACTIVATED);
      }
    }

    this.state = sb.build();
  }

  /**
   * Construct a new verifier for the given shader.
   *
   * @param s   The shader
   * @param <M> See {@link R2ShaderInstanceSingleType}
   *
   * @return A new verifier
   */

  public static <M> R2ShaderTranslucentInstanceSingleType<M> newVerifier(
    final R2ShaderTranslucentInstanceSingleType<M> s)
  {
    return new R2ShaderTranslucentInstanceSingleVerifier<>(s);
  }

  @Override
  public void delete(final JCGLInterfaceGL33Type g)
    throws R2Exception
  {
    this.shader.delete(g);
  }

  @Override
  public boolean isDeleted()
  {
    return this.shader.isDeleted();
  }

  @Override
  public long getShaderID()
  {
    return this.shader.getShaderID();
  }

  @Override
  public Class<M> getShaderParametersType()
  {
    return this.shader.getShaderParametersType();
  }

  @Override
  public JCGLProgramShaderUsableType getShaderProgram()
  {
    return this.shader.getShaderProgram();
  }

  @Override
  public void onActivate(final JCGLShadersType g_sh)
  {
    this.shader.onActivate(g_sh);
    this.state.transition(State.STATE_ACTIVATED);
  }

  @Override
  public void onValidate()
    throws R2ExceptionShaderValidationFailed
  {
    this.state.transition(State.STATE_VALIDATED);
    this.shader.onValidate();
  }

  @Override
  public void onDeactivate(
    final JCGLShadersType g_sh)
  {
    this.state.transition(State.STATE_DEACTIVATED);
    this.shader.onDeactivate(g_sh);
  }

  @Override
  public void onReceiveViewValues(
    final JCGLShadersType g_sh,
    final R2MatricesObserverValuesType m,
    final AreaInclusiveUnsignedLType viewport)
  {
    this.state.transition(State.STATE_VIEW_RECEIVED);
    this.shader.onReceiveViewValues(g_sh, m, viewport);
  }

  @Override
  public void onReceiveMaterialValues(
    final JCGLTexturesType g_tex,
    final JCGLShadersType g_sh,
    final JCGLTextureUnitContextMutableType tc,
    final M values)
  {
    this.state.transition(State.STATE_MATERIAL_RECEIVED);
    this.shader.onReceiveMaterialValues(g_tex, g_sh, tc, values);
  }

  @Override
  public void onReceiveInstanceTransformValues(
    final JCGLShadersType g_sh,
    final R2MatricesInstanceSingleValuesType m)
  {
    this.state.transition(State.STATE_INSTANCE_RECEIVED);
    this.shader.onReceiveInstanceTransformValues(g_sh, m);
  }

  @Override
  public Optional<JCGLBlendState> suggestedBlendState()
  {
    return this.shader.suggestedBlendState();
  }

  private enum State
  {
    STATE_DEACTIVATED,
    STATE_ACTIVATED,
    STATE_MATERIAL_RECEIVED,
    STATE_INSTANCE_RECEIVED,
    STATE_VIEW_RECEIVED,
    STATE_VALIDATED
  }
}