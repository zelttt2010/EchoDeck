#include "echodeck_ring_consumer.h"
#ifdef _KERNEL_MODE
#include <ntddk.h>
#define EDC_READ64(p) InterlockedCompareExchange64((volatile LONG64*)(p), 0, 0)
#define EDC_INC64(p) InterlockedIncrement64((volatile LONG64*)(p))
#else
#include <windows.h>
#define EDC_READ64(p) InterlockedCompareExchange64((volatile LONG64*)(p), 0, 0)
#define EDC_INC64(p) InterlockedIncrement64((volatile LONG64*)(p))
#endif
int EchoDeckRingRead(EchoDeckRing* ring, float* dst, unsigned frameCount) {
    if (!ring || !dst || ring->header.magic != ECHODECK_RING_MAGIC || ring->header.version != ECHODECK_RING_VERSION) return 0;
    unsigned channels = ring->header.channels;
    if (!channels || channels > ECHODECK_MAX_CHANNELS) return 0;
    unsigned produced = 0;
    while (produced < frameCount) {
        LONG64 r = EDC_READ64(&ring->header.readFrame);
        LONG64 w = EDC_READ64(&ring->header.writeFrame);
        if (r >= w) { EDC_INC64(&ring->header.underruns); break; }
        unsigned slot = (unsigned)((unsigned long long)r % ECHODECK_RING_FRAMES) * ECHODECK_MAX_CHANNELS;
        for (unsigned c=0;c<channels;c++) dst[produced*channels+c] = ring->samples[slot+c];
        for (unsigned c=channels;c<ECHODECK_MAX_CHANNELS;c++) dst[produced*channels+c] = 0.0f;
        MemoryBarrier(); InterlockedExchange64(&ring->header.readFrame, r+1); produced++;
    }
    for (unsigned f=produced; f<frameCount; f++) for (unsigned c=0;c<channels;c++) dst[f*channels+c]=0.0f;
    return (int)produced;
}
