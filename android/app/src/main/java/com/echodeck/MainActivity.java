package com.echodeck;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.*;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class MainActivity extends Activity {
    private static final int PICK_AUDIO = 7, REQ_MIC = 8;
    private final ArrayList<Sound> sounds = new ArrayList<>();
    private final HashMap<Long, MediaPlayer> players = new HashMap<>();
    private LinearLayout list;
    private TextView status, meter;
    private SeekBar master, micVolume;
    private float masterValue = 1f, micValue = 1f;
    private MediaSession mediaSession;
    private final int purple = Color.rgb(124,92,255);
    private final String PREFS = "echodeck-sounds";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(13,17,23));
        mediaSession = new MediaSession(this, "EchoDeck");
        mediaSession.setCallback(new MediaSession.Callback(){ @Override public void onPlay(){ if(!sounds.isEmpty()) play(sounds.get(0)); } @Override public void onPause(){ for(Sound s:sounds) stop(s); } });
        mediaSession.setActive(true);
        AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE); if(Build.VERSION.SDK_INT>=23) am.registerAudioDeviceCallback(new AudioDeviceCallback(){ @Override public void onAudioDevicesAdded(AudioDeviceInfo[] d){ statusSafe("Audio device conectado"); } @Override public void onAudioDevicesRemoved(AudioDeviceInfo[] d){ statusSafe("Audio device removido"); } },new Handler());
        loadSounds(); buildUi(); refresh();
        new Handler().post(new Runnable(){ public void run(){ updateMeter(); new Handler().postDelayed(this,100); }});
    }
    private TextView text(String s, int size) { TextView t=new TextView(this); t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(size); t.setPadding(0,8,0,8); return t; }
    private Button button(String s) { Button b=new Button(this); b.setText(s); return b; }
    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,20,24,16); root.setBackgroundColor(Color.rgb(13,17,23));
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=text("♫  EchoDeck",22); header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button add=button("＋ Adicionar"); add.setOnClickListener(v->pickAudio()); header.addView(add);
        root.addView(header);
        status=text("LOCAL MODE  •  Audio Engine parado",14); status.setTextColor(Color.rgb(255,195,90)); root.addView(status);
        LinearLayout controls=new LinearLayout(this); controls.setOrientation(LinearLayout.VERTICAL); controls.setPadding(0,10,0,10);
        meter=text("MIC  ░░░░░░░░░░  0%",13); controls.addView(meter);
        TextView mv=text("MASTER VOLUME",11); mv.setTextColor(Color.LTGRAY); controls.addView(mv);
        master=new SeekBar(this); master.setMax(100); master.setProgress(100); controls.addView(master);
        TextView micv=text("MIC VOLUME",11); micv.setTextColor(Color.LTGRAY); controls.addView(micv);
        micVolume=new SeekBar(this); micVolume.setMax(100); micVolume.setProgress(100); controls.addView(micVolume);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        Button engine=button("Iniciar engine"); engine.setOnClickListener(v->requestMicAndStart(false)); actions.addView(engine,new LinearLayout.LayoutParams(0,-2,1));
        Button test=button("Testar combinado"); test.setOnClickListener(v->requestMicAndStart(true)); actions.addView(test,new LinearLayout.LayoutParams(0,-2,1));
        Button rec=button("Gravar 10s"); rec.setOnClickListener(v->recordTenSeconds()); actions.addView(rec,new LinearLayout.LayoutParams(0,-2,1));
        controls.addView(actions); root.addView(controls);
        TextView devices=text("Audio Devices",17); devices.setPadding(0,18,0,5); root.addView(devices);
        TextView deviceInfo=text(deviceSummary(),12); deviceInfo.setTextColor(Color.LTGRAY); root.addView(deviceInfo);
        ScrollView scroll=new ScrollView(this); list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); scroll.addView(list); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button diag=button("Audio Diagnostics"); diag.setOnClickListener(v->diagnostics()); root.addView(diag);
        setContentView(root);
        master.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean f){masterValue=p/100f; for(MediaPlayer m:players.values())m.setVolume(masterValue*micValue,masterValue*micValue);} public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        micVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean f){micValue=p/100f;} public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
    }
    private String deviceSummary(){ StringBuilder s=new StringBuilder("Input: "); AudioManager a=(AudioManager)getSystemService(AUDIO_SERVICE); if(Build.VERSION.SDK_INT>=23){AudioDeviceInfo[] d=a.getDevices(AudioManager.GET_DEVICES_INPUTS); if(d.length==0)s.append("nenhum");else{for(int i=0;i<d.length;i++){if(i>0)s.append(", ");s.append(deviceName(d[i]));}} s.append("\nOutput: "); d=a.getDevices(AudioManager.GET_DEVICES_OUTPUTS); if(d.length==0)s.append("nenhum");else{for(int i=0;i<d.length;i++){if(i>0)s.append(", ");s.append(deviceName(d[i]));}}} return s.toString(); }
    private String deviceName(AudioDeviceInfo d){String n=d.getProductName()==null?"device":d.getProductName().toString();return n+" ("+d.getType()+")";}
    private void requestMicAndStart(boolean combined){ if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){ requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_MIC); return; } Intent i=new Intent(this,AudioService.class).setAction(combined?AudioService.ACTION_MONITOR:AudioService.ACTION_START); startServiceCompat(i); status.setText(combined?"COMPATIBILITY MODE  •  Monitor de microfone ativo":"LIMITED  •  Microfone monitorado localmente"); status.setTextColor(Color.rgb(255,195,90)); }
    private void startServiceCompat(Intent i){ if(Build.VERSION.SDK_INT>=26)startForegroundService(i); else startService(i); }
    private void recordTenSeconds(){ if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_MIC);return;} Intent i=new Intent(this,AudioService.class).setAction("com.echodeck.RECORD");startServiceCompat(i);status.setText("READY  •  Gravando microfone por 10 segundos");new Handler().postDelayed(()->{startServiceCompat(new Intent(this,AudioService.class).setAction("com.echodeck.STOP_RECORD"));toast("Gravação salva no armazenamento privado do EchoDeck");},10000); }
    private void pickAudio(){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("audio/*").putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true); startActivityForResult(i,PICK_AUDIO); }
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data); if(req!=PICK_AUDIO||res!=RESULT_OK||data==null)return; int n=0; if(data.getClipData()!=null){for(int i=0;i<data.getClipData().getItemCount();i++){addUri(data.getClipData().getItemAt(i).getUri());n++;}}else if(data.getData()!=null){addUri(data.getData());n++;} if(n>0)refresh();}
    private void addUri(Uri u){try{getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION); }catch(Exception ignored){} String name=u.getLastPathSegment(); if(name==null)name="Som "+(sounds.size()+1); int slash=name.lastIndexOf('/'); if(slash>=0)name=name.substring(slash+1); int dot=name.lastIndexOf('.'); if(dot>0)name=name.substring(0,dot); sounds.add(new Sound(System.nanoTime(),name,u,new int[]{purple,Color.rgb(22,185,166),Color.rgb(233,162,59),Color.rgb(232,93,117)}[sounds.size()%4])); saveSounds(); }
    private void refresh(){ if(list==null)return; list.removeAllViews(); if(sounds.isEmpty()){TextView e=text("Sua biblioteca está vazia\nAdicione MP3, WAV, OGG ou outro formato compatível.",15); e.setGravity(Gravity.CENTER); list.addView(e); return;} for(Sound s:sounds)list.addView(soundRow(s)); }
    private View soundRow(Sound s){ LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(14,12,14,12); row.setBackgroundColor(Color.rgb(25,32,43)); TextView n=text("♫  "+s.name,17); n.setTextColor(s.color); row.addView(n); TextView volumeLabel=text("VOLUME "+(int)(s.volume*100)+"%",11); volumeLabel.setTextColor(Color.LTGRAY); row.addView(volumeLabel); SeekBar volume=new SeekBar(this); volume.setMax(100); volume.setProgress((int)(s.volume*100)); volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean fromUser){s.volume=p/100f;volumeLabel.setText("VOLUME "+p+"%");MediaPlayer active=players.get(s.id);if(active!=null)active.setVolume(masterValue*s.volume,masterValue*s.volume); } public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){saveSounds();}}); row.addView(volume); LinearLayout a=new LinearLayout(this); Button play=button("▶ Reproduzir"); play.setOnClickListener(v->play(s)); a.addView(play,new LinearLayout.LayoutParams(0,-2,1)); Button loop=button(s.loop?"↻ Loop":"Uma vez"); loop.setOnClickListener(v->{s.loop=!s.loop;loop.setText(s.loop?"↻ Loop":"Uma vez");saveSounds();}); a.addView(loop); Button del=button("Excluir"); del.setOnClickListener(v->{stop(s);sounds.remove(s);saveSounds();refresh();}); a.addView(del); row.addView(a); return row; }
    private void play(Sound s){ try{stop(s); MediaPlayer p=new MediaPlayer(); p.setDataSource(this,s.uri); p.setAudioStreamType(AudioManager.STREAM_MUSIC); p.setLooping(s.loop); p.setOnPreparedListener(x->{x.setVolume(masterValue*s.volume,masterValue*s.volume);x.start();}); p.setOnCompletionListener(x->{if(!s.loop){stop(s);}}); p.setOnErrorListener((x,w,e)->{stop(s);return true;}); p.prepareAsync(); players.put(s.id,p);}catch(Exception e){toast("Formato não pôde ser reproduzido");} }
    private void stop(Sound s){MediaPlayer p=players.remove(s.id);if(p!=null){try{p.stop();}catch(Exception ignored){}p.release();}}
    private void updateMeter(){if(meter==null)return; int n=Math.min(10,Math.max(0,(int)(AudioService.micLevel*10))); StringBuilder b=new StringBuilder("MIC  ");for(int i=0;i<10;i++)b.append(i<n?'█':'░');meter.setText(b+"  "+(int)(AudioService.micLevel*100)+"%");}
    private void diagnostics(){ String s="EchoDeck Audio Diagnostics\n\nMicrofone: "+(AudioService.micAvailable?"READY":"não iniciado ou sem permissão")+"\nEntrada/saída: "+deviceSummary()+"\nAudio Engine: "+(AudioService.micAvailable?"working":"stopped")+"\nModo: Android público; sem microfone virtual global\n\nSe outro aplicativo bloquear o microfone, o EchoDeck permanece em LOCAL/COMPATIBILITY MODE."; new AlertDialog.Builder(this).setTitle("Audio Diagnostics").setMessage(s).setPositiveButton("OK",null).show(); }
    private void statusSafe(String s){if(status!=null && !AudioService.micAvailable) status.setText("ACTION REQUIRED  •  "+s);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void loadSounds(){String all=getSharedPreferences(PREFS,0).getString("items","");if(all.isEmpty())return;for(String line:all.split("\\n",-1)){String[] p=line.split("\\|",-1);if(p.length>=3)try{Sound s=new Sound(Long.parseLong(p[0]),p[1],Uri.parse(p[2]),purple);if(p.length>=4)s.volume=Math.max(0,Math.min(1,Float.parseFloat(p[3])));if(p.length>=5)s.loop="1".equals(p[4]);sounds.add(s);}catch(Exception ignored){}}}
    private void saveSounds(){StringBuilder b=new StringBuilder();for(Sound s:sounds)b.append(s.id).append('|').append(s.name.replace("|"," ")).append('|').append(s.uri).append('|').append(s.volume).append('|').append(s.loop?"1":"0").append('\n');getSharedPreferences(PREFS,0).edit().putString("items",b.toString()).apply();}
    @Override protected void onDestroy(){for(Sound s:new ArrayList<>(sounds))stop(s);if(mediaSession!=null){mediaSession.setActive(false);mediaSession.release();}super.onDestroy();}
}
