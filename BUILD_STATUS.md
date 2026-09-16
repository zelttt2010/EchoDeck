# EchoDeck build status

## Toolchains checked in this environment

This workspace runs on Linux x86_64 and does not contain Android Studio/JDK/Gradle, Visual Studio/MSVC, Windows SDK or WDK. No APK, EXE or SYS binary is generated here.

## Android source status

The native Java Android project is prepared for Android Studio:

- native Activity UI;
- persistent sound library using Storage Access Framework URIs;
- multiple `MediaPlayer` instances;
- individual and master volume;
- `AudioRecord` microphone meter;
- `AudioTrack` microphone monitor test;
- WAV recording;
- foreground microphone service;
- MediaSession;
- input/output device detection;
- diagnostics and LOCAL/COMPATIBILITY modes.

It does not claim a universal Android virtual microphone. The project requires Android SDK 35, JDK 17 and Gradle/Android Studio to build.

## Windows source status

The native Win32/C++ project is prepared for Visual Studio+CMake:

- persistent library under `%APPDATA%\\EchoDeck\\library.txt`;
- WAV PCM 16-bit import and playback;
- simultaneous voices;
- individual and master volume;
- WASAPI output and microphone capture;
- live microphone meter;
- PCM mixer and ring bridge;
- global Ctrl+Alt+1..9 hotkeys;
- device listing and diagnostics;
- bridge and capture probe tests.

The WDK driver is explicitly separate. No virtual endpoint is claimed or installed. The existing driver folder contains only the previously created integration contract and build notes; it is not a compiled driver.

## Build entry points

Android: open `android/` in Android Studio and generate `app-debug.apk`.

Windows application:

```text
cmake --preset windows-msvc-x64
cmake --build build/msvc-x64 --config Release
ctest --test-dir build/msvc-x64 -C Release --output-on-failure
```

The WDK component must be built and signed separately on Windows with a real SysVAD/WaveRT implementation. It is intentionally not part of the application CMake build.
