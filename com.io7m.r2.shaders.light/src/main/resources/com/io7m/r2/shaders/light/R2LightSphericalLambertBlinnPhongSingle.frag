/// \file R2LightSphericalLambertBlinnPhongSingle.frag
/// \brief An instantiation of the `R2LightSphericalLambertBlinnPhong` shader for single instances

#define R2_RECONSTRUCT_REQUIRE_NORMAL
#define R2_RECONSTRUCT_REQUIRE_SPECULAR

#include "R2LightSphericalLambertBlinnPhong.h"

#include <com.io7m.r2.shaders.light.api/R2LightShaderDriverSingle.h>
