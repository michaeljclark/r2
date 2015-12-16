/*
 * Copyright © 2015 <code@io7m.com> http://io7m.com
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

package com.io7m.r2.meshes;

import java.util.Optional;

/**
 * The type of mesh-parsing listeners.
 */

public interface R2MeshParserListenerType
{
  /**
   * The number of vertices was received.
   *
   * @param count The number of vertices
   */

  void onEventVertexCount(long count);

  /**
   * The number of triangles was received.
   *
   * @param count The number of triangles
   */

  void onEventTriangleCount(long count);

  /**
   * An error was encountered during parsing.
   *
   * @param e       An exception raised, if any
   * @param message The error message
   */

  void onError(
    Optional<Throwable> e,
    String message);

  /**
   * Vertex {@code index} is starting.
   *
   * @param index The vertex index
   */

  void onEventVertexStarted(int index);

  /**
   * A vertex position was received.
   *
   * @param index The vertex number
   * @param x     The {@code X} coordinate
   * @param y     The {@code Y} coordinate
   * @param z     The {@code Z} coordinate
   */

  void onEventVertexPosition(
    int index,
    double x,
    double y,
    double z);

  /**
   * A vertex normal was received.
   *
   * @param index The vertex number
   * @param x     The {@code X} coordinate
   * @param y     The {@code Y} coordinate
   * @param z     The {@code Z} coordinate
   */

  void onEventVertexNormal(
    int index,
    double x,
    double y,
    double z);

  /**
   * A vertex tangent was received.
   *
   * @param index The vertex number
   * @param x     The {@code X} coordinate
   * @param y     The {@code Y} coordinate
   * @param z     The {@code Z} coordinate
   * @param w     The {@code W} coordinate
   */

  void onEventVertexTangent(
    int index,
    double x,
    double y,
    double z,
    double w);

  /**
   * A vertex UV coordinate was received.
   *
   * @param index The vertex number
   * @param x     The {@code X} coordinate
   * @param y     The {@code Y} coordinate
   */

  void onEventVertexUV(
    int index,
    double x,
    double y);

  /**
   * Vertex {@code index} is finished.
   *
   * @param index The vertex index
   */

  void onEventVertexFinished(int index);

  /**
   * All vertices have been parsed.
   */

  void onEventVerticesFinished();

  /**
   * A triangle was received.
   *
   * @param index The triangle number
   * @param v0    The index of the first vertex
   * @param v1    The index of the second vertex
   * @param v2    The index of the third vertex
   */

  void onEventTriangle(
    int index,
    int v0,
    int v1,
    int v2);

  /**
   * All triangles have been parsed.
   */

  void onEventTrianglesFinished();
}
