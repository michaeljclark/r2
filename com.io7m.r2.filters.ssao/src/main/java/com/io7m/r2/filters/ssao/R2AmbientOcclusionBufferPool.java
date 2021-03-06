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

package com.io7m.r2.filters.ssao;

import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.texture.unit_allocator.JCGLTextureUnitContextParentType;
import com.io7m.jnull.NullCheck;
import com.io7m.jpuddle.core.JPPoolSynchronous;
import com.io7m.jpuddle.core.JPPoolSynchronousType;
import com.io7m.jpuddle.core.JPPoolableListenerType;
import com.io7m.r2.filters.ssao.api.R2AmbientOcclusionBufferDescription;
import com.io7m.r2.filters.ssao.api.R2AmbientOcclusionBufferType;
import com.io7m.r2.filters.ssao.api.R2AmbientOcclusionBufferUsableType;
import com.io7m.r2.rendering.targets.R2RenderTargetPoolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * <p>A pool of ambient occlusion buffers, with a configurable <i>soft</i> and
 * <i>hard</i> size.</p>
 *
 * <p>Objects within the pool will occasionally be discarded so that the storage
 * size of the pool stays below the <i>soft</i> limit. The size of the pool will
 * never be allowed to grow beyond the <i>hard</i> limit, and attempting to do
 * this will result in exceptions being raised.</p>
 */

public final class R2AmbientOcclusionBufferPool implements
  R2RenderTargetPoolType<
    R2AmbientOcclusionBufferDescription,
    R2AmbientOcclusionBufferUsableType>
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(R2AmbientOcclusionBufferPool.class);
  }

  private final
  JPPoolSynchronousType<
    R2AmbientOcclusionBufferDescription,
    R2AmbientOcclusionBufferType,
    R2AmbientOcclusionBufferUsableType,
    JCGLTextureUnitContextParentType> actual;

  private R2AmbientOcclusionBufferPool(
    final JPPoolSynchronousType<
      R2AmbientOcclusionBufferDescription,
      R2AmbientOcclusionBufferType,
      R2AmbientOcclusionBufferUsableType,
      JCGLTextureUnitContextParentType> in_actual)
  {
    this.actual = NullCheck.notNull(in_actual, "Pool");
  }

  /**
   * Construct a new pool with the given size limits.
   *
   * @param g    An OpenGL interface
   * @param soft The soft limit for the pool in bytes
   * @param hard The hard limit for the pool in bytes
   *
   * @return A new pool
   */

  public static R2RenderTargetPoolType<
    R2AmbientOcclusionBufferDescription,
    R2AmbientOcclusionBufferUsableType> newPool(
    final JCGLInterfaceGL33Type g,
    final long soft,
    final long hard)
  {
    return new R2AmbientOcclusionBufferPool(
      JPPoolSynchronous.newPool(new Listener(g), soft, hard));
  }

  @Override
  public R2AmbientOcclusionBufferUsableType get(
    final JCGLTextureUnitContextParentType tc,
    final R2AmbientOcclusionBufferDescription desc)
  {
    NullCheck.notNull(tc, "Texture context");
    NullCheck.notNull(desc, "Description");
    return this.actual.get(tc, desc);
  }

  @Override
  public void returnValue(
    final JCGLTextureUnitContextParentType tc,
    final R2AmbientOcclusionBufferUsableType target)
  {
    NullCheck.notNull(tc, "Texture context");
    NullCheck.notNull(target, "Buffer");
    this.actual.returnValue(tc, target);
  }

  @Override
  public void delete(final JCGLTextureUnitContextParentType c)
  {
    this.actual.deleteUnsafely(c);
  }

  @Override
  public boolean isDeleted()
  {
    return this.actual.isDeleted();
  }

  private static final class Listener implements JPPoolableListenerType<
    R2AmbientOcclusionBufferDescription,
    R2AmbientOcclusionBufferType,
    JCGLTextureUnitContextParentType>
  {
    private final JCGLInterfaceGL33Type g;

    Listener(final JCGLInterfaceGL33Type in_g)
    {
      this.g = NullCheck.notNull(in_g, "G33");
    }

    @Override
    public long onEstimateSize(
      final JCGLTextureUnitContextParentType tc,
      final R2AmbientOcclusionBufferDescription key)
    {
      return Math.multiplyExact(key.area().sizeX(), key.area().sizeY());
    }

    @Override
    public R2AmbientOcclusionBufferType onCreate(
      final JCGLTextureUnitContextParentType tc,
      final R2AmbientOcclusionBufferDescription key)
    {
      return R2AmbientOcclusionBuffer.create(
        this.g.framebuffers(),
        this.g.textures(),
        tc,
        key);
    }

    @Override
    public long onGetSize(
      final JCGLTextureUnitContextParentType tc,
      final R2AmbientOcclusionBufferDescription key,
      final R2AmbientOcclusionBufferType value)
    {
      return value.byteRange().getInterval();
    }

    @Override
    public void onReuse(
      final JCGLTextureUnitContextParentType tc,
      final R2AmbientOcclusionBufferDescription key,
      final R2AmbientOcclusionBufferType value)
    {
      if (LOG.isTraceEnabled()) {
        LOG.trace("reuse {}", key);
      }
    }

    @Override
    public void onDelete(
      final JCGLTextureUnitContextParentType tc,
      final R2AmbientOcclusionBufferDescription key,
      final R2AmbientOcclusionBufferType value)
    {
      if (LOG.isTraceEnabled()) {
        LOG.trace("delete {}", value);
      }

      value.delete(this.g);
    }

    @Override
    public void onError(
      final JCGLTextureUnitContextParentType tc,
      final R2AmbientOcclusionBufferDescription key,
      final Optional<R2AmbientOcclusionBufferType> value,
      final Throwable e)
    {
      LOG.error(
        "Exception raised in cache listener: {}: ", key, e);
    }
  }
}
