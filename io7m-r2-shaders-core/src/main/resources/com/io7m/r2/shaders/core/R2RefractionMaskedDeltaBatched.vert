/// \file R2RefractionMaskedDeltaSingle.vert
/// \brief A vertex shader for refracting single instances.

#include "R2LogDepth.h"
#include "R2Normals.h"
#include "R2SurfaceTypes.h"
#include "R2SurfaceVertexBatched.h"
#include "R2View.h"

uniform R2_view_t R2_view;

out R2_vertex_data_t R2_vertex_data;

void
main (void)
{
  mat4x4 m_modelview =
    (R2_view.transform_view * R2_vertex_transform_model);
  // Batched transforms are guaranteed to be orthogonal
  mat3x3 m_normal =
    mat3x3 (m_modelview);
  mat3x3 m_uv =
    mat3x3 (1.0); // Identity matrix

  vec4 position_hom =
    vec4 (R2_vertex_position, 1.0);
  vec4 position_eye =
    (m_modelview * position_hom);
  vec4 position_clip =
    ((R2_view.transform_projection * m_modelview) * position_hom);
  vec4 position_clip_log =
    vec4 (
      position_clip.xy,
      R2_logDepthEncodeFull (position_clip.w, R2_view.depth_coefficient),
      position_clip.w);

  float positive_eye_z = R2_logDepthPrepareEyeZ (position_eye.z);

  vec2 uv     = R2_vertex_uv;
  vec3 normal = R2_vertex_normal;

  R2_vertex_data = R2_vertex_data_t (
    position_eye,
    position_clip,
    positive_eye_z,
    uv,
    normal,
    vec3(1.0, 0.0, 0.0),
    vec3(0.0, 1.0, 0.0));

  gl_Position = position_clip_log;
}