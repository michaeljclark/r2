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

import com.io7m.jtensors.VectorWritable3FType;

import java.nio.FloatBuffer;

/**
 * The type of readable SSAO kernels.
 */

public interface R2SSAOKernelReadableType
{
  /**
   * @param index The sample index
   * @param out   The output vector
   */

  void getSample(
    final int index,
    VectorWritable3FType out);

  /**
   * @return The number of times this kernel has been regenerated
   */

  long getVersion();

  /**
   * @return The number of samples in the kernel
   */

  int getSize();

  /**
   * @return The raw float buffer that backs the kernel
   */

  FloatBuffer getFloatBuffer();
}