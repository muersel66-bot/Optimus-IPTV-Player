package tr.optimus.iptv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

/**
 * Tam ekran VLC oynatıcı. Dolby Digital (AC-3), Dolby Digital Plus (E-AC-3), DTS ve diğer sesleri
 * libVLC'nin kendi çözücüleriyle oynatır. Kanal değiştirme sayfaya (WebView) iletilir.
 */
public class PlayerActivity extends Activity {

    static PlayerActivity instance;

    private LibVLC libVLC;
    private MediaPlayer player;
    private VLCVideoLayout video;
    private LinearLayout bar;
    private TextView title;
    private TextView info;
    private String curUrl = "";
    private String curName = "";
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable hideBar = new Runnable() {
        @Override
        public void run() {
            bar.setVisibility(View.GONE);
        }
    };

    private final Runnable readCodecs = new Runnable() {
        @Override
        public void run() {
            showCodecs();
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        instance = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        immersive();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        video = new VLCVideoLayout(this);
        root.addView(video, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setBackgroundColor(Color.parseColor("#B3000000"));
        int pad = dp(12);
        bar.setPadding(pad, pad, pad, pad);

        title = new TextView(this);
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        info = new TextView(this);
        info.setTextColor(Color.parseColor("#9FC4FF"));
        info.setTextSize(13);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(button("◀ Önceki", new Runnable() {
            @Override
            public void run() {
                zap(-1);
            }
        }));
        buttons.addView(button("Sonraki ▶", new Runnable() {
            @Override
            public void run() {
                zap(1);
            }
        }));
        buttons.addView(button("Ses", new Runnable() {
            @Override
            public void run() {
                pickAudio();
            }
        }));
        buttons.addView(button("Kapat", new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }));

        bar.addView(title);
        bar.addView(info);
        bar.addView(buttons);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP;
        root.addView(bar, lp);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBar();
            }
        });
        setContentView(root);

        ArrayList<String> options = new ArrayList<String>();
        options.add("--network-caching=2000");
        options.add("--http-reconnect");
        libVLC = new LibVLC(this, options);
        player = new MediaPlayer(libVLC);
        player.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event e) {
                if (e.type == MediaPlayer.Event.Playing) {
                    info.setText("Oynatılıyor");
                    handler.removeCallbacks(readCodecs);
                    handler.postDelayed(readCodecs, 1500);
                    scheduleHide();
                } else if (e.type == MediaPlayer.Event.EncounteredError) {
                    info.setText("Yayın açılamadı. Adres kapalı veya erişilemiyor olabilir.");
                    showBar();
                } else if (e.type == MediaPlayer.Event.Buffering) {
                    float b = e.getBuffering();
                    if (b < 100f) {
                        info.setText("Arabelleğe alınıyor %" + (int) b);
                    }
                }
            }
        });

        String url = getIntent().getStringExtra("url");
        String name = getIntent().getStringExtra("name");
        if (url != null) {
            load(url, name);
        }
    }

    private Button button(String text, final Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
                scheduleHide();
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    /** Sayfadan yeni kanal geldiğinde çağrılır. */
    void load(String url, String name) {
        curUrl = url;
        curName = name == null ? "" : name;
        title.setText(curName);
        info.setText("Bağlanılıyor…");
        showBar();
        handler.removeCallbacks(readCodecs);
        if (!player.getVLCVout().areViewsAttached()) {
            player.attachViews(video, null, false, false);
        }
        Media m = new Media(libVLC, Uri.parse(url));
        m.setHWDecoderEnabled(true, false);
        m.addOption(":network-caching=2000");
        player.setMedia(m);
        m.release();
        player.play();
    }

    private void zap(int d) {
        info.setText("Kanal değişiyor…");
        MainActivity.js("window.__zap&&window.__zap(" + d + ")");
    }

    private void toggleBar() {
        if (bar.getVisibility() == View.VISIBLE) {
            bar.setVisibility(View.GONE);
        } else {
            showBar();
            scheduleHide();
        }
    }

    private void showBar() {
        handler.removeCallbacks(hideBar);
        bar.setVisibility(View.VISIBLE);
    }

    private void scheduleHide() {
        handler.removeCallbacks(hideBar);
        handler.postDelayed(hideBar, 5000);
    }

    private static String codecName(String raw) {
        String c = raw == null ? "" : raw.trim().toLowerCase();
        if (c.equals("a52") || c.equals("ac3") || c.equals("ac-3")) {
            return "Dolby Digital";
        }
        if (c.equals("eac3") || c.equals("ec-3") || c.equals("ec3")) {
            return "Dolby Digital Plus";
        }
        if (c.equals("trhd") || c.equals("mlp")) {
            return "Dolby TrueHD";
        }
        if (c.equals("dtsh") || c.equals("dtsm") || c.equals("dtse")) {
            return "DTS-HD";
        }
        if (c.equals("dts") || c.startsWith("dts")) {
            return "DTS";
        }
        if (c.equals("mp4a") || c.equals("aac")) {
            return "AAC";
        }
        if (c.equals("mpga") || c.equals("mp3")) {
            return "MP3";
        }
        return c.length() == 0 ? "?" : c.toUpperCase();
    }

    private static String channelsName(int ch) {
        if (ch == 1) {
            return "1.0";
        }
        if (ch == 2) {
            return "2.0";
        }
        if (ch == 6) {
            return "5.1";
        }
        if (ch == 8) {
            return "7.1";
        }
        return ch > 0 ? ch + "ch" : "";
    }

    /** Akıştaki ses kodeklerini gösterir (örneğin "Dolby Digital Plus 5.1"). */
    private void showCodecs() {
        try {
            IMedia m = player.getMedia();
            if (m == null) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            int n = m.getTrackCount();
            for (int i = 0; i < n; i++) {
                Media.Track t = m.getTrack(i);
                if (t != null && t.type == Media.Track.Type.Audio) {
                    int ch = 0;
                    if (t instanceof Media.AudioTrack) {
                        ch = ((Media.AudioTrack) t).channels;
                    }
                    if (sb.length() > 0) {
                        sb.append("  ·  ");
                    }
                    sb.append(codecName(t.codec));
                    String cn = channelsName(ch);
                    if (cn.length() > 0) {
                        sb.append(' ').append(cn);
                    }
                }
            }
            m.release();
            if (sb.length() > 0) {
                info.setText("Ses: " + sb);
            }
        } catch (Throwable ignored) {
            // bilgi satırı isteğe bağlıdır
        }
    }

    private void pickAudio() {
        final MediaPlayer.TrackDescription[] tracks = player.getAudioTracks();
        if (tracks == null || tracks.length == 0) {
            info.setText("Bu yayında ses seçeneği yok.");
            return;
        }
        String[] names = new String[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            names[i] = tracks[i].name;
        }
        new AlertDialog.Builder(this)
                .setTitle("Ses parçası")
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        player.setAudioTrack(tracks[which].id);
                        immersive();
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                zap(1);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_CHANNEL_UP:
                zap(-1);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (bar.getVisibility() != View.VISIBLE) {
                    showBar();
                    scheduleHide();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (curUrl.length() > 0 && !player.getVLCVout().areViewsAttached()) {
            load(curUrl, curName);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
        player.stop();
        player.detachViews();
    }

    @Override
    protected void onDestroy() {
        instance = null;
        player.release();
        libVLC.release();
        MainActivity.js("window.__vlcClosed&&window.__vlcClosed()");
        super.onDestroy();
    }
}
