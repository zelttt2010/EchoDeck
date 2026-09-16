# EchoDeck

Implementação nativa inicial para Android e Windows.

## Estado honesto desta entrega

O código-fonte foi separado por plataforma e não usa HTML, WebView, PWA ou aplicação web.

- **Android:** projeto nativo Java, com soundboard, importação de arquivos via Storage Access Framework, reprodução real, controles de volume, gravação/medição de microfone, serviço em foreground e detecção de dispositivos. A mixagem de microfone para outros aplicativos continua limitada pelas políticas do Android.
- **Windows:** projeto nativo Win32/C++ com WASAPI, reprodução de WAV PCM, captura de microfone, controles de volume, hotkeys globais, ring bridge PCM compartilhado e testes de captura. O endpoint combinado exige o miniport WDK/SysVAD `.sys/.inf/.cat` assinado; o código não cria um dispositivo falso.
- **Builds:** este ambiente não possui Android SDK/Gradle nem Windows SDK/WDK, portanto os binários `EchoDeck.apk` e `EchoDeck.exe` não podem ser gerados aqui de forma verificável. O projeto contém os arquivos de build para serem compilados em ambientes nativos.

## Android

Abra `EchoDeck/android` em Android Studio e gere o APK debug. Requer Android SDK API 35 e JDK 17. O app usa APIs públicas e informa `LOCAL MODE` quando não pode entregar uma entrada de microfone para outro aplicativo.

## Windows

Gere em Windows com Visual Studio 2022, Windows 10/11 SDK e CMake:

```text
cmake -S windows -B build -A x64
cmake --build build --config Release
```

O componente de áudio virtual não é um executável: ele precisa ser compilado com WDK, assinado pela cadeia da Microsoft e instalado por pacote PnP. Por segurança, esta entrega não inclui um driver sem assinatura nem simula um endpoint. A pasta `windows/driver` contém o contrato de integração e a lista de requisitos para a implementação WDK/SysVAD.
