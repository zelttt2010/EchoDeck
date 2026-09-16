#pragma once
#include <cstdint>
#include <string>
#include <vector>
struct WavFile { uint32_t sampleRate=0; uint16_t channels=0; std::vector<float> samples; };
bool LoadWav(const std::wstring& path, WavFile& out, std::wstring& error);
