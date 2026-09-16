#include "../src/bridge_client.h"
#include "../driver/echodeck_ring_consumer.h"
#include <windows.h>
#include <iostream>
int main(){BridgeClient writer;std::wstring error;if(!writer.Open(48000,2,error)){std::wcerr<<error;return 2;}HANDLE map=OpenFileMappingW(FILE_MAP_ALL_ACCESS,FALSE,ECHODECK_RING_NAME);if(!map)return 3;auto* ring=(EchoDeckRing*)MapViewOfFile(map,FILE_MAP_ALL_ACCESS,0,0,sizeof(EchoDeckRing));if(!ring)return 4;float in[8]={.25f,-.25f,.5f,-.5f,.75f,-.75f,1.f,-1.f};writer.Write(in,4);float out[8]{};int got=EchoDeckRingRead(ring,out,4);bool ok=got==4;for(int i=0;i<8;i++)ok=ok&&out[i]==in[i];UnmapViewOfFile(ring);CloseHandle(map);writer.Close();std::cout<<(ok?"bridge-ok\n":"bridge-failed\n");return ok?0:5;}
