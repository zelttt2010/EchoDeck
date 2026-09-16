#pragma once
#include "wav.h"
#include "bridge_client.h"
#include <windows.h>
#include <cstdint>
#include <mmdeviceapi.h>
#include <audioclient.h>
#include <atomic>
#include <mutex>
#include <vector>
#include <memory>
#include <thread>
#include <deque>
class AudioEngine {
public:
    ~AudioEngine();
    bool Start(std::wstring& error);
    void Stop();
    bool Play(const WavFile& sound, float volume, bool loop, std::wstring& error);
    void StopAll();
    void SetMaster(float value) { master_.store(value); }
    float MicLevel() const { return micLevel_.load(); }
    bool Running() const { return running_.load(); }
    bool BridgeReady() const { return bridgeReady_; }
private:
    struct Voice { WavFile sound; double position=0; float volume=1.f; bool loop=false; };
    void RenderLoop();
    void CaptureLoop();
    bool OpenOutput(std::wstring& error);
    bool OpenCapture(std::wstring& error);
    void ReleaseAudio();
    std::atomic<bool> running_{false}; std::atomic<float> master_{1.f}; std::atomic<float> micLevel_{0.f};
    std::thread renderThread_, captureThread_; HANDLE renderEvent_=nullptr;
    IMMDevice* outDevice_=nullptr; IAudioClient* outClient_=nullptr; IAudioRenderClient* render_=nullptr; WAVEFORMATEX* format_=nullptr; UINT32 bufferFrames_=0;
    IMMDevice* inDevice_=nullptr; IAudioClient* inClient_=nullptr; IAudioCaptureClient* capture_=nullptr; WAVEFORMATEX* captureFormat_=nullptr;
    std::mutex voicesMutex_; std::vector<Voice> voices_;
    std::mutex micMutex_; std::deque<float> micSamples_;
    BridgeClient bridge_; bool bridgeReady_=false; uint32_t bridgeChannels_=0;
};
