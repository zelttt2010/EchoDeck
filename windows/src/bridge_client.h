#pragma once
#include "../shared/echodeck_ring.h"
#include <windows.h>
#include <cstdint>
#include <string>
class BridgeClient {
public:
    ~BridgeClient();
    bool Open(uint32_t sampleRate, uint32_t channels, std::wstring& error);
    void Close();
    bool Write(const float* frames, uint32_t count);
    uint64_t Underruns() const;
private:
    HANDLE mapping_=nullptr, dataEvent_=nullptr, stopEvent_=nullptr;
    EchoDeckRing* ring_=nullptr;
};
