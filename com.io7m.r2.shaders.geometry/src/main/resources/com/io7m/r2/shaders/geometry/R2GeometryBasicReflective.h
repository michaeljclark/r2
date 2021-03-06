#ifndef R2_GEOMETRY_BASIC_REFLECTIVE_H
#define R2_GEOMETRY_BASIC_REFLECTIVE_H

/// \file R2GeometryBasicReflective.h
/// \brief Basic pseudo-reflective (environment mapped) deferred surface implementation

#include <com.io7m.r2.shaders.core/R2EnvironmentReflection.h>
#include <com.io7m.r2.shaders.core/R2MatricesInstance.h>

#include <com.io7m.r2.shaders.geometry.api/R2GeometryShaderMain.h>

#include "R2GeometryBasicTypes.h"

/// Textures for reflections

struct R2_surface_reflective_textures_t {
  /// A right-handed cube map representing the reflected environment
  samplerCube environment;
};

/// Parameters for reflections

struct R2_surface_reflective_parameters_t {
  /// Eye-to-world matrix for transforming reflection vectors.
  mat4x4 transform_view_inverse;
  /// Mix factor in the range `[0, 1]`, where 1 indicates a fully reflective surface
  float  environment_mix;
};

uniform R2_basic_surface_textures_t        R2_basic_surface_textures;
uniform R2_basic_surface_parameters_t      R2_basic_surface_parameters;

uniform R2_surface_reflective_textures_t   R2_surface_reflective_textures;
uniform R2_surface_reflective_parameters_t R2_surface_reflective_parameters;

R2_geometry_output_t
R2_geometryMain (
  const R2_vertex_data_t data,
  const R2_geometry_derived_t derived,
  const R2_geometry_textures_t textures,
  const R2_view_t view,
  const R2_matrices_instance_t matrices_instance)
{
  vec4 albedo_sample =
    texture (R2_basic_surface_textures.albedo, data.uv);
  vec4 albedo =
    mix (R2_basic_surface_parameters.albedo_color,
         albedo_sample,
         R2_basic_surface_parameters.albedo_mix * albedo_sample.w);

  vec4 environment_sample =
    R2_environmentReflection (
      R2_surface_reflective_textures.environment,
      data.position_eye.xyz,
      derived.normal_bumped,
      R2_surface_reflective_parameters.transform_view_inverse
    );

  vec4 surface =
    mix (albedo, environment_sample, R2_surface_reflective_parameters.environment_mix);

  float emission_sample =
    texture (R2_basic_surface_textures.emission, data.uv).x;
  float emission =
    R2_basic_surface_parameters.emission_amount * emission_sample;

  vec3 specular_sample =
    texture (R2_basic_surface_textures.specular, data.uv).xyz;
  vec3 specular =
    specular_sample * R2_basic_surface_parameters.specular_color;

  bool discarded =
    surface.w < R2_basic_surface_parameters.alpha_discard_threshold;

  return R2_geometry_output_t (
    surface.xyz,
    emission,
    derived.normal_bumped,
    specular,
    R2_basic_surface_parameters.specular_exponent,
    discarded
  );
}

#endif // R2_GEOMETRY_BASIC_REFLECTIVE_H

