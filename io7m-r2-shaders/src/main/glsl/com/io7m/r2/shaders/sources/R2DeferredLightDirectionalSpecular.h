#ifndef R2_DEFERRED_LIGHT_DIRECTIONAL_SPECULAR_H
#define R2_DEFERRED_LIGHT_DIRECTIONAL_SPECULAR_H

/// \file R2DeferredLightDirectionalSpecular.h
/// \brief A trivial directional light with Phong specular highlights

#include "R2DeferredLightShaderMain.h"
#include "R2LightDirectional.h"

uniform R2_light_directional_t R2_light_directional;

R2_deferred_light_output_t
R2_deferredLightMain(
  const R2_deferred_reconstructed_surface_t surface)
{
  R2_light_directional_vectors_t vectors =
    R2_lightDirectionalVectors (
      R2_light_directional, surface.position.xyz, surface.normal);

  vec3 diffuse =
    R2_lightDirectionalDiffuseTerm (R2_light_directional, vectors);

  vec3 specular =
    R2_lightDirectionalSpecularTerm (
      R2_light_directional,
      vectors,
      surface.specular,
      surface.specular_exponent);

  return R2_deferred_light_output_t (diffuse, specular);
}

#endif // R2_DEFERRED_LIGHT_DIRECTIONAL_SPECULAR_H
