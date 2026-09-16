# Driver workstream

O driver de captura combinado não pode ser reduzido a um `.inf` com um nome. O endpoint precisa de um miniport/topology/WaveRT baseado no framework SysVAD oficial e deve ser compilado com WDK.

A integração que já está no projeto é o lado que pode ser validado independentemente do WDK:

1. `shared/echodeck_ring.h` define o contrato binário entre o Audio Engine e o endpoint;
2. `src/bridge_client.*` publica frames PCM combinados em uma seção nomeada e sinaliza evento;
3. `driver/echodeck_ring_consumer.*` consome os frames, trata underrun/overrun e preenche silêncio quando necessário;
4. `tests/bridge_test.cpp` valida o ring em user mode;
5. `tests/capture_probe.cpp` abre o endpoint real pelo nome e confirma frames/sinal;
6. `tests/Test-EchoDeckAudio.ps1` valida PnP e executa o probe.

O arquivo `EchoDeckVirtualAudio.inx` registra o ID pretendido (`Root\\EchoDeckVirtualAudio`) e o serviço, mas é somente um template de build: ele não é instalável sem o miniport real e o `.cat` correspondente. O passo WDK restante é ligar `EchoDeckRingRead` ao processamento do capture pin do miniport SysVAD, implementar o ciclo PnP/power do próprio adapter e gerar o pacote INF/CAT/SYS assinado. Não há um `.sys` fictício no repositório.

Referência oficial usada para o modelo do driver: [SysVAD Virtual Audio Device Driver Sample](https://learn.microsoft.com/en-us/samples/microsoft/windows-driver-samples/sysvad-virtual-audio-device-driver-sample/).
