#include "device_utils.h"
#include <windows.h>
#include <mmdeviceapi.h>
#include <functiondiscoverykeys_devpkey.h>
#include <propvarutil.h>
#include <sstream>
std::wstring EnumerateAudioDevices(bool input){std::wstringstream out;IMMDeviceEnumerator* e=nullptr;if(FAILED(CoCreateInstance(__uuidof(MMDeviceEnumerator),nullptr,CLSCTX_ALL,IID_PPV_ARGS(&e))))return L"Enumerator unavailable";IMMDeviceCollection* c=nullptr;EDataFlow flow=input?eCapture:eRender;if(SUCCEEDED(e->EnumAudioEndpoints(flow,DEVICE_STATE_ACTIVE,&c))){UINT n=0;c->GetCount(&n);for(UINT i=0;i<n;i++){IMMDevice* d=nullptr;IPropertyStore* p=nullptr;PROPVARIANT v;PropVariantInit(&v);if(SUCCEEDED(c->Item(i,&d))&&SUCCEEDED(d->OpenPropertyStore(STGM_READ,&p))&&SUCCEEDED(p->GetValue(PKEY_Device_FriendlyName,&v))&&v.vt==VT_LPWSTR)out<<L"- "<<v.pwszVal<<L"\r\n";PropVariantClear(&v);if(p)p->Release();if(d)d->Release();}c->Release();}e->Release();if(out.str().empty())return L"(none)\r\n";return out.str();}
