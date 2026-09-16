#include "bridge_client.h"
#include <algorithm>
BridgeClient::~BridgeClient(){Close();}
bool BridgeClient::Open(uint32_t rate,uint32_t channels,std::wstring& error){
 Close(); if(channels==0||channels>ECHODECK_MAX_CHANNELS){error=L"Bridge suporta 1 ou 2 canais";return false;}
 mapping_=CreateFileMappingW(INVALID_HANDLE_VALUE,nullptr,PAGE_READWRITE,0,(DWORD)sizeof(EchoDeckRing),ECHODECK_RING_NAME);
 if(!mapping_){error=L"Não foi possível criar o ring buffer";return false;}
 ring_=(EchoDeckRing*)MapViewOfFile(mapping_,FILE_MAP_ALL_ACCESS,0,0,sizeof(EchoDeckRing));
 if(!ring_){error=L"Não foi possível mapear o ring buffer";Close();return false;}
 ZeroMemory(ring_,sizeof(*ring_));ring_->header.magic=ECHODECK_RING_MAGIC;ring_->header.version=ECHODECK_RING_VERSION;ring_->header.sampleRate=rate;ring_->header.channels=channels;ring_->header.capacityFrames=ECHODECK_RING_FRAMES;ring_->header.writeFrame=0;ring_->header.readFrame=0;
 dataEvent_=CreateEventW(nullptr,FALSE,FALSE,ECHODECK_DATA_EVENT);stopEvent_=CreateEventW(nullptr,TRUE,FALSE,ECHODECK_STOP_EVENT);if(!dataEvent_||!stopEvent_){error=L"Não foi possível criar eventos do bridge";Close();return false;}ResetEvent(stopEvent_);return true;
}
void BridgeClient::Close(){if(stopEvent_)SetEvent(stopEvent_);if(ring_)UnmapViewOfFile(ring_);if(mapping_)CloseHandle(mapping_);if(dataEvent_)CloseHandle(dataEvent_);if(stopEvent_)CloseHandle(stopEvent_);ring_=nullptr;mapping_=nullptr;dataEvent_=nullptr;stopEvent_=nullptr;}
bool BridgeClient::Write(const float* frames,uint32_t count){if(!ring_||!frames)return false;const uint32_t ch=ring_->header.channels;for(uint32_t f=0;f<count;f++){LONG64 w=InterlockedCompareExchange64(&ring_->header.writeFrame,0,0);LONG64 r=InterlockedCompareExchange64(&ring_->header.readFrame,0,0);if((uint64_t)(w-r)>=ECHODECK_RING_FRAMES){InterlockedIncrement64(&ring_->header.overruns);InterlockedExchange64(&ring_->header.readFrame,w-ECHODECK_RING_FRAMES+1);r=w-ECHODECK_RING_FRAMES+1;}size_t slot=(size_t)(w%ECHODECK_RING_FRAMES)*ECHODECK_MAX_CHANNELS;for(uint32_t c=0;c<ch;c++)ring_->samples[slot+c]=std::clamp(frames[f*ch+c],-1.f,1.f);MemoryBarrier();InterlockedExchange64(&ring_->header.writeFrame,w+1);}if(dataEvent_)SetEvent(dataEvent_);return true;}
uint64_t BridgeClient::Underruns() const{return ring_?(uint64_t)InterlockedCompareExchange64((LONG64*)&ring_->header.underruns,0,0):0;}
