# EchoDeck Windows

Aplicativo nativo Win32/C++ usando WASAPI. Não usa HTML, WebView, PWA ou navegador.

## Compilar a aplicação

Requisitos:

- Windows 10/11;
- Visual Studio 2022 com Desktop development with C++;
- Windows 10/11 SDK;
- CMake 3.25 ou superior.

No **Developer PowerShell for VS 2022**:

```text
cmake --preset windows-msvc-x64
cmake --build build/msvc-x64 --config Release
ctest --test-dir build/msvc-x64 -C Release --output-on-failure
```

Executáveis produzidos:

```text
build/msvc-x64/Release/EchoDeck.exe
build/msvc-x64/Release/EchoDeckCaptureProbe.exe
build/msvc-x64/Release/EchoDeckBridgeTest.exe
```

## Implementado no aplicativo

- UI nativa Win32;
- Biblioteca em memória durante a sessão;
- Importação de WAV PCM 16-bit;
- Reprodução simultânea;
- Volume master e volume individual;
- WASAPI shared-mode para saída;
- Captura WASAPI do microfone;
- Medidor de microfone;
- Mixagem PCM no Audio Engine;
- Ring buffer combinado para o componente externo;
- Hotkeys globais Ctrl+Alt+1 até Ctrl+Alt+9;
- Diagnóstico de endpoint e captura;
- Teste unitário do ring buffer.

A saída física não monitora o microfone automaticamente, evitando feedback. O fluxo combinado é publicado no bridge destinado ao componente de áudio virtual.

## WDK separado

O componente WDK não faz parte do build CMake da aplicação. Ele requer Visual Studio, Windows SDK, WDK, o framework SysVAD/WaveRT e assinatura de driver. Este ambiente não possui essas ferramentas e nenhum driver é gerado ou instalado aqui.
