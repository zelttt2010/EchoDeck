package com.echodeck;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Real microphone capture/measurement service. It never claims to inject a mic into other apps. */
public final class AudioService extends Service {
    public static final String ACTION_START = "com.echodeck.START";
    public static final String ACTION_STOP = "com.echodeck.STOP";
    public static final String ACTION_MONITOR = "com.echodeck.MONITOR";
    public static volatile float micLevel = 0f;
    public static volatile boolean micAvailable = false;
    public static volatile String lastRecording = "";
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;
    private AudioRecord record;
    private AudioTrack monitor;
    private volatile boolean monitorMic;
    private volatile boolean saveRecording;
    private FileOutputStream recorder;
    private int recordedBytes;
    private File recordingFile;
    private AudioManager audioManager;

    public static void start(Context c) {
        Intent i = new Intent(c, AudioService.class).setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i); else c.startService(i);
    }
    public static void stop(Context c) { c.startService(new Intent(c, AudioService.class).setAction(ACTION_STOP)); }

    @Override public void onCreate() {
        super.onCreate(); audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        String ch = "echodeck-audio";
        if (Build.VERSION.SDK_INT >= 26) ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(new NotificationChannel(ch, "EchoDeck Audio", NotificationManager.IMPORTANCE_LOW));
        Notification n = new Notification.Builder(this, ch).setContentTitle("EchoDeck Audio Engine")
                .setContentText("Microfone pronto").setSmallIcon(android.R.drawable.ic_btn_speak_now).setOngoing(true).build();
        startForeground(42, n);
    }
    @Override public int onStartCommand(Intent intent, int flags, int id) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) { stopEngine(); stopSelf(); }
        else if (intent != null && "com.echodeck.RECORD".equals(intent.getAction())) { startEngine(); beginRecording(); }
        else if (intent != null && "com.echodeck.STOP_RECORD".equals(intent.getAction())) { finishRecording(); }
        else { monitorMic = intent != null && ACTION_MONITOR.equals(intent.getAction()); startEngine(); }
        return START_NOT_STICKY;
    }
    private synchronized void startEngine() {
        if (running.get()) { if (monitorMic && monitor == null) { int size = 8192; monitor = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, size, AudioTrack.MODE_STREAM); monitor.play(); } return; }
        int min = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min <= 0) return;
        int size = Math.max(min * 2, 4096);
        try {
            record = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 48000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, size);
            if (record.getState() != AudioRecord.STATE_INITIALIZED) { record.release(); record = null; return; }
            if (monitorMic) monitor = new AudioTrack(AudioManager.STREAM_MUSIC, 48000,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, size,
                    AudioTrack.MODE_STREAM);
            record.startRecording(); if (monitor != null) monitor.play();
            running.set(true); micAvailable = true;
            worker = new Thread(() -> loop(size), "EchoDeck-AudioRecord"); worker.start();
        } catch (SecurityException | IllegalArgumentException e) { micAvailable = false; releaseAudio(); }
    }
    private synchronized void beginRecording() {
        if (saveRecording) return;
        try {
            File dir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recordings");
            if (!dir.exists() && !dir.mkdirs()) return;
            recordingFile = new File(dir, "echodeck-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + ".wav");
            recorder = new FileOutputStream(recordingFile); recorder.write(new byte[44]); recordedBytes = 0; saveRecording = true;
        } catch (IOException e) { saveRecording = false; }
    }
    private synchronized void finishRecording() {
        if (!saveRecording || recorder == null) return;
        try { recorder.flush(); recorder.close(); RandomAccessFile r = new RandomAccessFile(recordingFile, "rw"); writeHeader(r, recordedBytes, 48000, 1, 16); r.close(); lastRecording = recordingFile.getAbsolutePath(); }
        catch (IOException ignored) {} finally { saveRecording = false; recorder = null; }
    }
    private static void writeHeader(RandomAccessFile r, int bytes, int rate, int channels, int bits) throws IOException {
        r.seek(0); r.writeBytes("RIFF"); writeLE(r, 36 + bytes, 4); r.writeBytes("WAVEfmt "); writeLE(r, 16, 4); writeLE(r, 1, 2); writeLE(r, channels, 2); writeLE(r, rate, 4); writeLE(r, rate * channels * bits / 8, 4); writeLE(r, channels * bits / 8, 2); writeLE(r, bits, 2); r.writeBytes("data"); writeLE(r, bytes, 4);
    }
    private static void writeLE(RandomAccessFile r, int value, int n) throws IOException { for (int i=0;i<n;i++) r.write(value >>> (8*i)); }
    private void loop(int size) {
        short[] data = new short[size / 2];
        while (running.get()) {
            int n = record == null ? -1 : record.read(data, 0, data.length, AudioRecord.READ_BLOCKING);
            if (n <= 0) continue;
            double sum = 0;
            for (int i = 0; i < n; i++) { double v = data[i] / 32768.0; sum += v * v; }
            micLevel = (float)Math.min(1.0, Math.sqrt(sum / n) * 3.0);
            if (monitorMic && monitor != null) monitor.write(data, 0, n);
            if (saveRecording && recorder != null) try { for (int i=0;i<n;i++){ recorder.write(data[i] & 0xff); recorder.write((data[i] >>> 8) & 0xff); } recordedBytes += n * 2; } catch (IOException e) { finishRecording(); }
        }
    }
    private synchronized void stopEngine() { running.set(false); if (worker != null) try { worker.join(250); } catch (InterruptedException ignored) {} finishRecording(); releaseAudio(); micAvailable = false; micLevel = 0; }
    private void releaseAudio() { try { if (record != null) { record.stop(); record.release(); } } catch (Exception ignored) {} try { if (monitor != null) { monitor.stop(); monitor.release(); } } catch (Exception ignored) {} record = null; monitor = null; }
    @Override public void onDestroy() { stopEngine(); super.onDestroy(); }
    @Override public android.os.IBinder onBind(Intent intent) { return null; }
}
