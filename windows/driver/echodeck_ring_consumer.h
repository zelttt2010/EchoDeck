#pragma once
#include "../shared/echodeck_ring.h"
#ifdef __cplusplus
extern "C" {
#endif
int EchoDeckRingRead(EchoDeckRing* ring, float* dst, unsigned frameCount);
#ifdef __cplusplus
}
#endif
