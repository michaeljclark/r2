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

package com.io7m.r2.shaders.api;

import com.io7m.jcanephora.core.JCGLProgramShaderUsableType;
import com.io7m.jcanephora.core.JCGLProgramUniformType;
import com.io7m.jcanephora.core.JCGLType;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Convenient functions for dealing with shader parameters.
 */

public final class R2ShaderParameters
{
  private R2ShaderParameters()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Retrieve a uniform parameter for the given program, raising an exception if
   * the parameter is not present for any reason.
   *
   * @param p    The program
   * @param name The parameter name
   * @param type The expected type of the parameter
   *
   * @return The parameter, if it exists
   *
   * @throws R2ExceptionShaderParameterNotPresent Iff the shader parameter was
   *                                              either undeclared or was
   *                                              optimized out
   */

  public static JCGLProgramUniformType uniform(
    final JCGLProgramShaderUsableType p,
    final String name,
    final JCGLType type)
    throws R2ExceptionShaderParameterNotPresent
  {
    final Map<String, JCGLProgramUniformType> u = p.uniforms();
    if (u.containsKey(name)) {
      final JCGLProgramUniformType uu = u.get(name);
      if (uu.type() == type) {
        return uu;
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Shader parameter is of an unexpected type.\n");
      sb.append("Program name: ");
      sb.append(p.name());
      sb.append("\n");
      sb.append("Parameter name: ");
      sb.append(name);
      sb.append("\n");
      sb.append("Expected type: ");
      sb.append(type);
      sb.append("\n");
      sb.append("Received type: ");
      sb.append(uu.type());
      sb.append("\n");
      dumpParameters(u, sb);
      throw new R2ExceptionShaderParameterWrongType(sb.toString());
    }

    final StringBuilder sb = new StringBuilder(128);
    sb.append("Shader parameter either undeclared or optimized out.\n");
    sb.append("Program name: ");
    sb.append(p.name());
    sb.append("\n");
    sb.append("Parameter name: ");
    sb.append(name);
    sb.append("\n");
    dumpParameters(u, sb);
    throw new R2ExceptionShaderParameterNotPresent(sb.toString());
  }

  private static void dumpParameters(
    final Map<String, JCGLProgramUniformType> us,
    final StringBuilder sb)
  {
    sb.append("Parameters:\n");

    final List<JCGLProgramUniformType> ps = new ArrayList<>(us.size());
    ps.addAll(us.values());
    ps.sort((u0, u1) -> Integer.compareUnsigned(u0.glName(), u1.glName()));

    for (int index = 0; index < ps.size(); ++index) {
      final JCGLProgramUniformType u = ps.get(index);
      sb.append("[");
      sb.append(u.glName());
      sb.append("] ");
      sb.append(u.name());
      sb.append(" ");
      sb.append(u.type());
      sb.append(" (size ");
      sb.append(u.size());
      sb.append(")");
      sb.append("\n");
    }
  }

  /**
   * Check that the given program has the expected number of uniform
   * parameters.
   *
   * @param p     The program
   * @param count The expected number of parameters
   *
   * @throws R2ExceptionShaderParameterCountMismatch On unexpected parameter
   *                                                 counts
   */

  public static void checkUniformParameterCount(
    final JCGLProgramShaderUsableType p,
    final int count)
    throws R2ExceptionShaderParameterCountMismatch
  {
    final Map<String, JCGLProgramUniformType> u = p.uniforms();
    if (u.size() != count) {
      final StringBuilder sb = new StringBuilder(128);
      sb.append("Shader parameter count is incorrect.\n");
      sb.append("Program name:             ");
      sb.append(p.name());
      sb.append("\n");
      sb.append("Expected parameter count: ");
      sb.append(count);
      sb.append("\n");
      sb.append("Actual parameter count:   ");
      sb.append(u.size());
      sb.append("\n");
      dumpParameters(u, sb);
      throw new R2ExceptionShaderParameterCountMismatch(sb.toString());
    }
  }
}
