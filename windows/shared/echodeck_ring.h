#pragma once
#include <stdint.h>

#define ECHODECK_RING_MAGIC 0x45445231u /* EDR1 */
#define ECHODECK_RING_VERSION 1u
#define ECHODECK_RING_NAME L"Global\\EchoDeckAudioBridge-v1"
#define ECHODECK_DATA_EVENT L"Global\\EchoDeckAudioData-v1"
#define ECHODECK_STOP_EVENT L"Global\\EchoDeckAudioStop-v1"
#define ECHODECK_MAX_CHANNELS 2u
#define ECHODECK_RING_FRAMES 16384u

#ifdef _KERNEL_MODE
#include <ntddk.h>
typedef volatile LONG64 ed_atomic64;
#elif defined(_WIN32)
#include <windows.h>
typedef volatile LONG64 ed_atomic64;
#else
typedef volatile long long ed_atomic64;
#endif

struct EchoDeckRingHeader {
    uint32_t magic;
    uint32_t version;
    uint32_t sampleRate;
    uint32_t channels;
    uint32_t capacityFrames;
    ed_atomic64 writeFrame;
    ed_atomic64 readFrame;
    ed_atomic64 underruns;
    ed_atomic64 overruns;
};
struct EchoDeckRing {
    EchoDeckRingHeader header;
    float samples[ECHODECK_RING_FRAMES * ECHODECK_MAX_CHANNELS];
};
#ifdef __cplusplus
static_assert(sizeof(EchoDeckRingHeader) < 128, "ring header unexpectedly large");
#endif
