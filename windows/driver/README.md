# EchoDeck Virtual Audio Component — WDK package contract

Esta pasta não instala nem simula um endpoint. O endpoint combinado do Windows precisa ser um driver WDK real, assinado e distribuído por um pacote PnP.

## Requisitos de implementação

- Basear o dispositivo em WaveRT/AVStream e nos padrões oficiais de áudio do Windows;
- Expor um endpoint de captura `EchoDeck Combined Microphone`;
- Aceitar PCM compartilhado produzido pelo Audio Engine por ring buffer protegido e eventos;
- Implementar PnP, power management, surpresa de remoção e reinicialização;
- Não depender de Test Signing em produção;
- Validar com Windows 10/11 x64, HVCI/Memory Integrity e WHQL/attestation conforme a distribuição escolhida;
- Expor versão e estado para o Audio Assistant;
- Remover o pacote e o endpoint no uninstall.

O aplicativo Win32 nesta entrega não cria um endpoint falso. Sem um pacote `.inf/.sys/.cat` assinado por uma cadeia de confiança da Microsoft, o EchoDeck fica em `LIMITED`: o WASAPI de saída e o medidor do microfone funcionam, mas a entrada combinada não é anunciada para outros aplicativos.

## Contrato entre engine e driver

A implementação do driver deve abrir o stream de captura virtual e consumir frames do buffer alimentado pelo serviço do EchoDeck. O serviço deve publicar:

- taxa de amostragem;
- canais;
- posição de leitura/escrita;
- contagem de underruns/overruns;
- evento de dados disponível;
- estado de parada e troca de dispositivo.

O contrato deve ser versionado antes de qualquer release do pacote de driver.
