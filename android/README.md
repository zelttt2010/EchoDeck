# EchoDeck Android

Aplicativo Android nativo em Java. Não usa HTML, WebView, PWA ou navegador.

## Abrir no Android Studio

1. Abra a pasta `EchoDeck/android`.
2. Use JDK 17.
3. Instale Android SDK Platform 35 e Build Tools compatíveis.
4. Faça o Sync do Gradle.
5. Execute em um aparelho Android 10/API 29 ou superior.

Gerar o APK debug no Android Studio: **Build > Generate App Bundles or APKs > Generate APKs**.

Via terminal, em um ambiente com Gradle disponível:

```text
gradle :app:assembleDebug
```

O APK será produzido em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Escopo funcional

- Biblioteca persistente usando Storage Access Framework e URIs persistentes;
- Reprodução simultânea usando instâncias independentes de `MediaPlayer`;
- Volume individual e master;
- `AudioRecord` para medidor e monitoramento;
- `AudioTrack` para o teste de monitoramento;
- Gravação WAV em armazenamento privado do aplicativo;
- Foreground Service de microfone;
- MediaSession;
- Detecção de entradas e saídas com `AudioManager`;
- Diagnósticos e estados LOCAL/COMPATIBILITY.

O Android não oferece, para aplicativos comuns, um microfone virtual universal para outros aplicativos. O EchoDeck não afirma essa capacidade e pode ser silenciado quando outro aplicativo tiver prioridade de captura.
