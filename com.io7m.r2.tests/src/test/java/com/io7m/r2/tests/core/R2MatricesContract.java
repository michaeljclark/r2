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

package com.io7m.r2.tests.core;

import com.io7m.jaffirm.core.PreconditionViolationException;
import com.io7m.jcanephora.core.JCGLTexture2DType;
import com.io7m.jcanephora.core.JCGLTextureFilterMagnification;
import com.io7m.jcanephora.core.JCGLTextureFilterMinification;
import com.io7m.jcanephora.core.JCGLTextureFormat;
import com.io7m.jcanephora.core.JCGLTextureUnitType;
import com.io7m.jcanephora.core.JCGLTextureWrapS;
import com.io7m.jcanephora.core.JCGLTextureWrapT;
import com.io7m.jcanephora.core.JCGLUsageHint;
import com.io7m.jcanephora.core.api.JCGLInterfaceGL33Type;
import com.io7m.jcanephora.core.api.JCGLTexturesType;
import com.io7m.jfunctional.Unit;
import com.io7m.jtensors.core.parameterized.matrices.PMatrices3x3D;
import com.io7m.jtensors.core.parameterized.matrices.PMatrices4x4D;
import com.io7m.jtensors.core.parameterized.matrices.PMatrix3x3D;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r2.core.R2IDPool;
import com.io7m.r2.core.R2IDPoolType;
import com.io7m.r2.core.R2LightProjectiveWithoutShadow;
import com.io7m.r2.core.R2LightProjectiveWithoutShadowType;
import com.io7m.r2.core.R2MatricesObserverType;
import com.io7m.r2.core.R2MatricesType;
import com.io7m.r2.core.R2ProjectionFrustum;
import com.io7m.r2.core.R2ProjectionMesh;
import com.io7m.r2.core.R2ProjectionMeshType;
import com.io7m.r2.core.R2ProjectionOrthographic;
import com.io7m.r2.core.R2RendererExceptionInstanceAlreadyActive;
import com.io7m.r2.core.R2RendererExceptionObserverAlreadyActive;
import com.io7m.r2.core.R2RendererExceptionProjectiveAlreadyActive;
import com.io7m.r2.core.R2Texture2DStatic;
import com.io7m.r2.core.R2TransformOT;
import com.io7m.r2.core.R2TransformOTType;
import com.io7m.r2.core.R2TransformReadableType;
import com.io7m.r2.core.R2TransformSOT;
import com.io7m.r2.spaces.R2SpaceTextureType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

public abstract class R2MatricesContract
{
  @Rule public ExpectedException expected = ExpectedException.none();

  private static R2LightProjectiveWithoutShadowType newProjective(
    final JCGLInterfaceGL33Type c,
    final R2IDPoolType id_pool)
  {
    final JCGLTexture2DType pt =
      R2MatricesContract.newProjectionTexture(c);
    final R2ProjectionFrustum pp =
      R2ProjectionFrustum.create();
    final R2ProjectionMeshType pmesh =
      R2ProjectionMesh.create(
        c,
        pp,
        JCGLUsageHint.USAGE_STATIC_DRAW,
        JCGLUsageHint.USAGE_STATIC_DRAW);
    return R2LightProjectiveWithoutShadow.create(
      pmesh,
      R2Texture2DStatic.of(pt),
      id_pool);
  }

  private static JCGLTexture2DType newProjectionTexture(
    final JCGLInterfaceGL33Type c)
  {
    final JCGLTexturesType gt = c.textures();
    final List<JCGLTextureUnitType> gu = gt.textureGetUnits();
    return gt.texture2DAllocate(
      gu.get(0), 64L, 64L,
      JCGLTextureFormat.TEXTURE_FORMAT_RGB_8_3BPP,
      JCGLTextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
      JCGLTextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
      JCGLTextureFilterMinification.TEXTURE_FILTER_NEAREST,
      JCGLTextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  }

  protected abstract R2MatricesType newMatrices();

  @Test
  public final void testMatricesObserverCalled()
  {
    final R2MatricesType m = this.newMatrices();

    final Integer r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) -> Integer.valueOf(23));

    Assert.assertEquals(Integer.valueOf(23), r);
  }

  @Test
  public final void testMatricesObserverInactive0()
  {
    final R2MatricesType m = this.newMatrices();

    final R2MatricesObserverType r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) -> mm);

    this.expected.expect(PreconditionViolationException.class);
    r.matrixProjection();
  }

  @Test
  public final void testMatricesObserverInactive1()
  {
    final R2MatricesType m = this.newMatrices();

    final R2MatricesObserverType r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) -> mm);

    this.expected.expect(PreconditionViolationException.class);
    r.matrixView();
  }

  @Test
  public final void testMatricesObserverInactive2()
  {
    final R2MatricesType m = this.newMatrices();

    final R2MatricesObserverType r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) -> mm);

    this.expected.expect(PreconditionViolationException.class);
    r.matrixViewInverse();
  }

  @Test
  public final void testMatricesObserverInactive3()
  {
    final R2MatricesType m = this.newMatrices();

    final R2MatricesObserverType r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) -> mm);

    this.expected.expect(PreconditionViolationException.class);
    r.viewRays();
  }

  @Test
  public final void testMatricesObserverCalledTwice()
  {
    final R2MatricesType m = this.newMatrices();

    final R2ProjectionOrthographic pp =
      R2ProjectionOrthographic.create();

    this.expected.expect(R2RendererExceptionObserverAlreadyActive.class);
    this.expected.expectMessage("Observer already active");

    m.withObserver(
      PMatrices4x4D.identity(),
      pp,
      Unit.unit(),
      (mm, u0) -> m.withObserver(
        PMatrices4x4D.identity(), pp, u0, (mk, u1) -> {
          throw new UnreachableCodeException();
        }));
  }

  @Test
  public final void testMatricesObserverTransformCalled()
  {
    final R2MatricesType m = this.newMatrices();
    final PMatrix3x3D<R2SpaceTextureType, R2SpaceTextureType> uv =
      PMatrices3x3D.identity();
    final R2TransformReadableType t =
      R2TransformSOT.create();

    final Integer r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) -> mm.withTransform(t, uv, u0, (mi, u1) -> Integer.valueOf(23))
    );

    Assert.assertEquals(Integer.valueOf(23), r);
  }

  @Test
  public final void testMatricesObserverTransformCalledTwice()
  {
    final R2MatricesType m = this.newMatrices();
    final PMatrix3x3D<R2SpaceTextureType, R2SpaceTextureType> uv =
      PMatrices3x3D.identity();
    final R2TransformReadableType t =
      R2TransformSOT.create();

    this.expected.expect(R2RendererExceptionInstanceAlreadyActive.class);
    this.expected.expectMessage("Instance already active");

    m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) -> mm.withTransform(t, uv, u0, (mi, u1) ->
        mm.withTransform(t, uv, u1, (mk, u2) -> {
          throw new UnreachableCodeException();
        }))
    );
  }

  @Test
  public final void testMatricesObserverProjectiveCalled()
    throws Exception
  {
    final JCGLInterfaceGL33Type c = R2TestUtilities.getFakeGL();
    final R2IDPoolType id_pool = R2IDPool.newPool();

    final R2MatricesType m =
      this.newMatrices();
    final PMatrix3x3D<R2SpaceTextureType, R2SpaceTextureType> uv =
      PMatrices3x3D.identity();
    final R2TransformOTType t =
      R2TransformOT.create();

    final R2LightProjectiveWithoutShadowType lp =
      R2MatricesContract.newProjective(c, id_pool);

    final Integer r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) ->
        mm.withProjectiveLight(lp, Integer.valueOf(23), (mp, u1) -> u1));

    Assert.assertEquals(Integer.valueOf(23), r);
  }

  @Test
  public final void testMatricesObserverProjectiveCalledTwice()
    throws Exception
  {
    final JCGLInterfaceGL33Type c = R2TestUtilities.getFakeGL();
    final R2IDPoolType id_pool = R2IDPool.newPool();


    final R2MatricesType m =
      this.newMatrices();
    final PMatrix3x3D<R2SpaceTextureType, R2SpaceTextureType> uv =
      PMatrices3x3D.identity();
    final R2TransformOTType t =
      R2TransformOT.create();

    final R2LightProjectiveWithoutShadowType lp =
      R2MatricesContract.newProjective(c, id_pool);

    this.expected.expect(R2RendererExceptionProjectiveAlreadyActive.class);
    this.expected.expectMessage("Projective already active");

    m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) ->
        mm.withProjectiveLight(
          lp, Integer.valueOf(23), (mp, u1) ->
            mm.withProjectiveLight(
              lp, Integer.valueOf(24), (mpx, u1x) -> u1))
    );
  }

  @Test
  public final void testMatricesObserverProjectiveInstanceActive()
    throws Exception
  {
    final JCGLInterfaceGL33Type c = R2TestUtilities.getFakeGL();
    final R2IDPoolType id_pool = R2IDPool.newPool();


    final R2MatricesType m =
      this.newMatrices();
    final PMatrix3x3D<R2SpaceTextureType, R2SpaceTextureType> uv =
      PMatrices3x3D.identity();
    final R2TransformOTType t =
      R2TransformOT.create();

    final R2LightProjectiveWithoutShadowType lp =
      R2MatricesContract.newProjective(c, id_pool);

    this.expected.expect(R2RendererExceptionProjectiveAlreadyActive.class);
    this.expected.expectMessage("Projective already active");

    m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) ->
        mm.withProjectiveLight(
          lp, Integer.valueOf(23), (mp, u1) ->
            mm.withTransform(t, uv, Integer.valueOf(64), (mpx, ux) -> ux)));
  }

  @Test
  public final void testMatricesObserverProjectiveVolumeActive()
    throws Exception
  {
    final JCGLInterfaceGL33Type c = R2TestUtilities.getFakeGL();
    final R2IDPoolType id_pool = R2IDPool.newPool();


    final R2MatricesType m =
      this.newMatrices();
    final PMatrix3x3D<R2SpaceTextureType, R2SpaceTextureType> uv =
      PMatrices3x3D.identity();
    final R2TransformOTType t =
      R2TransformOT.create();

    final R2LightProjectiveWithoutShadowType lp =
      R2MatricesContract.newProjective(c, id_pool);

    this.expected.expect(R2RendererExceptionProjectiveAlreadyActive.class);
    this.expected.expectMessage("Projective already active");

    m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) ->
        mm.withProjectiveLight(
          lp, Integer.valueOf(23), (mp, u1) ->
            mm.withVolumeLight(lp, Integer.valueOf(24), (mpx, ux) -> ux)));
  }

  @Test
  public final void testMatricesObserverInstanceProjectiveActive()
    throws Exception
  {
    final JCGLInterfaceGL33Type c = R2TestUtilities.getFakeGL();
    final R2IDPoolType id_pool = R2IDPool.newPool();


    final R2MatricesType m =
      this.newMatrices();
    final PMatrix3x3D<R2SpaceTextureType, R2SpaceTextureType> uv =
      PMatrices3x3D.identity();
    final R2TransformOTType t =
      R2TransformOT.create();

    final R2LightProjectiveWithoutShadowType lp =
      R2MatricesContract.newProjective(c, id_pool);

    this.expected.expect(R2RendererExceptionInstanceAlreadyActive.class);
    this.expected.expectMessage("Instance already active");

    final Integer r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) ->
        mm.withTransform(t, uv, u0, (mi, u1) ->
          mm.withProjectiveLight(lp, Integer.valueOf(23), (mpx, mvx) -> mvx)));
  }

  @Test
  public final void testMatricesObserverInstanceVolumeActive()
    throws Exception
  {
    final JCGLInterfaceGL33Type c = R2TestUtilities.getFakeGL();
    final R2IDPoolType id_pool = R2IDPool.newPool();

    final R2MatricesType m =
      this.newMatrices();
    final PMatrix3x3D<R2SpaceTextureType, R2SpaceTextureType> uv =
      PMatrices3x3D.identity();
    final R2TransformOTType t =
      R2TransformOT.create();

    final R2LightProjectiveWithoutShadowType lp =
      R2MatricesContract.newProjective(c, id_pool);

    this.expected.expect(R2RendererExceptionInstanceAlreadyActive.class);
    this.expected.expectMessage("Instance already active");

    final Integer r = m.withObserver(
      PMatrices4x4D.identity(),
      R2ProjectionOrthographic.create(),
      Unit.unit(),
      (mm, u0) ->
        mm.withTransform(t, uv, u0, (mi, u1) ->
          mm.withVolumeLight(lp, Integer.valueOf(23), (mpx, mvx) -> mvx)));
  }
}