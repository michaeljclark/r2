#ifndef R2_LIGHT_SPHERICAL_H
#define R2_LIGHT_SPHERICAL_H

/// \file R2LightSpherical.h
/// \brief Functions and types related to spherical lighting

#include "R2LightPositional.h"

/// Calculate the Lambert diffuse term for a spherical light.
///
/// @param light The light parameters
/// @param v     The calculated light vectors
///
/// @return The diffuse term

vec3
R2_lightSphericalDiffuseLambertTerm (
  const R2_light_positional_t light,
  const R2_light_positional_vectors_t v)
{
  float factor = max (0.0, dot (v.surface_to_light, v.normal));
  return (light.color * light.intensity) * factor;
}

/// Calculate the specular Phong term for a spherical light
///
/// @param light             The light parameters
/// @param v                 The calculated light vectors
/// @param specular_color    The surface specular color
/// @param specular_exponent The surface specular exponent
///
/// @return The specular term

vec3
R2_lightSphericalSpecularPhongTerm (
  const R2_light_positional_t light,
  const R2_light_positional_vectors_t v,
  const vec3 specular_color,
  const float specular_exponent)
{
  vec3 reflection =
    reflect (v.observer_to_surface, v.normal);
  float base_factor =
    max (0.0, dot (reflection, v.surface_to_light));
  float factor =
    pow (base_factor, specular_exponent);
  vec3 color =
    (light.color * light.intensity) * factor;
  return color * specular_color;
}

/// Calculate the specular Blinn-Phong term for a spherical light
///
/// @param light             The light parameters
/// @param v                 The calculated light vectors
/// @param specular_color    The surface specular color
/// @param specular_exponent The surface specular exponent
///
/// @return The specular term

vec3
R2_lightSphericalSpecularBlinnPhongTerm (
  const R2_light_positional_t light,
  const R2_light_positional_vectors_t v,
  const vec3 specular_color,
  const float specular_exponent)
{
  vec3 half_v =
    normalize ((-v.observer_to_surface) + v.surface_to_light);
  float base_factor =
    max (0.0, dot (v.normal, half_v));
  float factor =
    pow (base_factor, specular_exponent);
  vec3 color =
    (light.color * light.intensity) * factor;
  return color * specular_color;
}

#endif // R2_LIGHT_SPHERICAL_H
