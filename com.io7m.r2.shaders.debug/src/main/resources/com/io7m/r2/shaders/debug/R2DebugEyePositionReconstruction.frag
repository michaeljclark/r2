/// \file R2DebugEyePositionReconstruction.frag
/// \brief A fragment shader driver for reconstructing eye-space positions.

#include <com.io7m.r2.shaders.core/R2PositionReconstruction.h>
#include <com.io7m.r2.shaders.core/R2LogDepth.h>
#include <com.io7m.r2.shaders.core/R2Viewport.h>
#include <com.io7m.r2.shaders.core/R2ViewRays.h>
#include <com.io7m.r2.shaders.geometry/R2GBufferInput.h>

layout(location = 0) out vec4 R2_out_eye_position;

uniform R2_viewport_t      R2_viewport;
uniform R2_view_rays_t     R2_view_rays;
uniform R2_gbuffer_input_t R2_gbuffer;
uniform float              R2_depth_coefficient;

void
main (void)
{
  // Get the current screen coordinates in UV coordinate form
  vec2 screen_uv =
    R2_viewportFragmentPositionToUV (R2_viewport, gl_FragCoord.xy);

  // Reconstruct the eye-space Z from the depth texture
  float log_depth =
    texture (R2_gbuffer.depth, screen_uv).x;
  float eye_z_positive =
    R2_logDepthDecode (log_depth, R2_depth_coefficient);
  float eye_z =
    -eye_z_positive;

  // Reconstruct the full eye-space position
  R2_out_eye_position =
    R2_positionReconstructFromEyeZ (eye_z, screen_uv, R2_view_rays);
}

