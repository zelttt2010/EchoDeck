#include "wav.h"
#include <fstream>
#include <cstring>
static uint16_t u16(const char* p){return (uint16_t)((unsigned char)p[0]|((unsigned char)p[1]<<8));}
static uint32_t u32(const char* p){return (uint32_t)((unsigned char)p[0]|((unsigned char)p[1]<<8)|((unsigned char)p[2]<<16)|((unsigned char)p[3]<<24);}
bool LoadWav(const std::wstring& path,WavFile& out,std::wstring& error){
 std::ifstream f(path,std::ios::binary); if(!f){error=L"Não foi possível abrir o arquivo";return false;}
 char riff[12]; f.read(riff,12); if(f.gcount()!=12||std::memcmp(riff,"RIFF",4)||std::memcmp(riff+8,"WAVE",4)){error=L"O arquivo não é WAV";return false;}
 uint16_t fmt=0,ch=0,bits=0;uint32_t rate=0;std::vector<char> data;char id[4];
 while(f.read(id,4)){char szb[4];if(!f.read(szb,4))break;uint32_t sz=u32(szb);std::vector<char> buf(sz);f.read(buf.data(),sz);if(sz&1)f.seekg(1,std::ios::cur);
  if(!std::memcmp(id,"fmt ",4)&&sz>=16){fmt=u16(buf.data());ch=u16(buf.data()+2);rate=u32(buf.data()+4);bits=u16(buf.data()+14);}
  if(!std::memcmp(id,"data",4))data=std::move(buf);
 }
 if(fmt!=1||ch==0||rate==0||bits!=16||data.empty()){error=L"Somente WAV PCM 16-bit é aceito nesta versão";return false;}
 size_t count=data.size()/2;out.sampleRate=rate;out.channels=ch;out.samples.resize(count);for(size_t i=0;i<count;i++)out.samples[i]=(float)(int16_t)u16(data.data()+i*2)/32768.f;return true;
}
