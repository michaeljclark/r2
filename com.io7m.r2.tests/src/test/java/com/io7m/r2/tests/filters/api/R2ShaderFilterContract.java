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

package com.io7m.r2.tests.filters.api;

import com.io7m.jcanephora.core.api.JCGLContextType;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocator;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitAllocatorType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextType;
import com.io7m.jfsm.core.FSMTransitionException;
import com.io7m.r2.core.api.ids.R2IDPool;
import com.io7m.r2.core.api.ids.R2IDPoolType;
import com.io7m.r2.shaders.api.R2ShaderPreprocessingEnvironmentType;
import com.io7m.r2.shaders.filter.api.R2ShaderFilterType;
import com.io7m.r2.shaders.filter.api.R2ShaderParametersFilterMutable;
import com.io7m.r2.tests.R2JCGLContract;
import com.io7m.r2.tests.ShaderPreprocessing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class R2ShaderFilterContract<T, TM extends T> extends
  R2JCGLContract
{
  @Rule public ExpectedException expected = ExpectedException.none();

  protected abstract R2ShaderFilterType<T> newShaderWithVerifier(
    JCGLInterfaceGL33Type g,
    R2ShaderPreprocessingEnvironmentType sources,
    R2IDPoolType pool);

  protected abstract TM newParameters(
    JCGLInterfaceGL33Type g);

  @Test
  public final void testCorrect()
    throws Exception
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);

    final JCGLInterfaceGL33Type g =
      c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool =
      R2IDPool.newPool();

    final R2ShaderFilterType<T> f =
      this.newShaderWithVerifier(g, sources, pool);
    final T t =
      this.newParameters(g);

    final JCGLTexturesType g_tex = g.textures();

    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        32,
        g_tex.textureGetUnits());
    final JCGLTextureUnitContextParentType tr =
      ta.rootContext();
    final JCGLTextureUnitContextType tc =
      tr.unitContextNew();

    final R2ShaderParametersFilterMutable<T> values =
      R2ShaderParametersFilterMutable.create();
    values.setTextureUnitContext(tc);
    values.setValues(t);

    f.onActivate(g);
    f.onReceiveFilterValues(g, values);
    f.onValidate();
    f.onDeactivate(g);
  }

  @Test
  public final void testInactive()
    throws Exception
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);

    final JCGLInterfaceGL33Type g =
      c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool =
      R2IDPool.newPool();

    final R2ShaderFilterType<T> f =
      this.newShaderWithVerifier(g, sources, pool);
    final T t =
      this.newParameters(g);

    final JCGLTexturesType g_tex = g.textures();
    final JCGLTextureUnitAllocatorType ta =
      JCGLTextureUnitAllocator.newAllocatorWithStack(
        32, g_tex.textureGetUnits());
    final JCGLTextureUnitContextParentType tr = ta.rootContext();
    final JCGLTextureUnitContextType tc = tr.unitContextNew();

    final R2ShaderParametersFilterMutable<T> values =
      R2ShaderParametersFilterMutable.create();
    values.setTextureUnitContext(tc);
    values.setValues(t);

    this.expected.expect(FSMTransitionException.class);
    f.onReceiveFilterValues(g, values);
  }

  @Test
  public final void testNoReceiveFilter()
    throws Exception
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);

    final JCGLInterfaceGL33Type g =
      c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool =
      R2IDPool.newPool();

    final R2ShaderFilterType<T> f =
      this.newShaderWithVerifier(g, sources, pool);
    final T t =
      this.newParameters(g);

    f.onActivate(g);
    this.expected.expect(FSMTransitionException.class);
    f.onValidate();
  }

  @Test
  public final void testDeactivatedValidate()
    throws Exception
  {
    final JCGLContextType c = this.newGL33Context("main", 24, 8);

    final JCGLInterfaceGL33Type g =
      c.contextGetGL33();
    final R2ShaderPreprocessingEnvironmentType sources =
      ShaderPreprocessing.preprocessor();
    final R2IDPoolType pool =
      R2IDPool.newPool();

    final R2ShaderFilterType<T> f =
      this.newShaderWithVerifier(g, sources, pool);
    final T t =
      this.newParameters(g);

    f.onActivate(g);
    f.onDeactivate(g);

    this.expected.expect(FSMTransitionException.class);
    f.onValidate();
  }
}