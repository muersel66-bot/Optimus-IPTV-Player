package tr.optimus.iptv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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
import android.widget.SeekBar;
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
    private LinearLayout buttonsRow;
    private LinearLayout bottom;
    private SeekBar seek;
    private TextView tPos;
    private TextView tLen;
    private Button playBtn;
    private Button aspectBtn;
    private int aspectIdx = 0;
    private boolean userSeeking = false;
    private final StringBuilder numBuf = new StringBuilder();
    private final Runnable numGo = new Runnable() {
        @Override
        public void run() {
            goNumber();
        }
    };
    private static final MediaPlayer.ScaleType[] ASPECTS = new MediaPlayer.ScaleType[] {
            MediaPlayer.ScaleType.SURFACE_BEST_FIT,
            MediaPlayer.ScaleType.SURFACE_FILL,
            MediaPlayer.ScaleType.SURFACE_16_9,
            MediaPlayer.ScaleType.SURFACE_4_3,
            MediaPlayer.ScaleType.SURFACE_ORIGINAL
    };
    private static final String[] ASPECT_NAMES = new String[] {
            "Sığdır", "Ekrana doldur", "16:9", "4:3", "Orijinal"
    };
    private String curUrl = "";
    private String curName = "";
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable hideBar = new Runnable() {
        @Override
        public void run() {
            bar.setVisibility(View.GONE);
            bottom.setVisibility(View.GONE);
            handler.removeCallbacks(tick);
        }
    };

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            handler.postDelayed(this, 500);
        }
    };

    /** Görüntü gelmezse (siyah ekran) bir kez yazılım çözücüyle yeniden dener. */
    private boolean swDecode = false;
    private boolean swTried = false;
    private final Runnable blackCheck = new Runnable() {
        @Override
        public void run() {
            try {
                if (player != null && player.isPlaying() && player.getVideoTrack() < 0 && !swTried && curUrl != null) {
                    swTried = true;
                    swDecode = true;
                    info.setText("Görüntü gelmedi, yazılım çözücü deneniyor…");
                    showBar();
                    load(curUrl, curName);
                }
            } catch (Throwable ignored) {
            }
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
        buttons.addView(button("◀◀ Kanal", new Runnable() {
            @Override
            public void run() {
                zap(-1);
            }
        }));
        playBtn = button("⏸ Duraklat", new Runnable() {
            @Override
            public void run() {
                togglePlay();
            }
        });
        buttons.addView(playBtn);
        buttons.addView(button("Kanal ▶▶", new Runnable() {
            @Override
            public void run() {
                zap(1);
            }
        }));
        buttons.addView(button("⏪ 10", new Runnable() {
            @Override
            public void run() {
                seekBy(-10000);
            }
        }));
        buttons.addView(button("10 ⏩", new Runnable() {
            @Override
            public void run() {
                seekBy(10000);
            }
        }));

        LinearLayout buttons2 = new LinearLayout(this);
        buttons2.setOrientation(LinearLayout.HORIZONTAL);
        aspectBtn = button("Oran: Sığdır", new Runnable() {
            @Override
            public void run() {
                nextAspect();
            }
        });
        buttons2.addView(aspectBtn);
        buttons2.addView(button("Ses", new Runnable() {
            @Override
            public void run() {
                pickAudio();
            }
        }));
        buttons2.addView(button("Altyazı", new Runnable() {
            @Override
            public void run() {
                pickSub();
            }
        }));
        buttons2.addView(button("Kapat", new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }));

        bar.addView(title);
        bar.addView(info);
        buttonsRow = buttons;
        bar.addView(buttons);
        bar.addView(buttons2);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP;
        root.addView(bar, lp);

        bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setBackgroundColor(Color.parseColor("#B3000000"));
        bottom.setPadding(pad, pad, pad, pad);
        tPos = new TextView(this);
        tPos.setTextColor(Color.WHITE);
        tPos.setTextSize(14);
        tPos.setText("0:00");
        tLen = new TextView(this);
        tLen.setTextColor(Color.WHITE);
        tLen.setTextSize(14);
        tLen.setText("0:00");
        seek = new SeekBar(this);
        seek.setMax(1000);
        seek.setKeyProgressIncrement(10);
        seek.setFocusable(true);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && player != null && player.getLength() > 0) {
                    player.setPosition(progress / 1000f);
                    scheduleHide();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                userSeeking = false;
            }
        });
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        slp.leftMargin = pad;
        slp.rightMargin = pad;
        bottom.addView(tPos);
        bottom.addView(seek, slp);
        bottom.addView(tLen);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.gravity = Gravity.BOTTOM;
        bottom.setVisibility(View.GONE);
        root.addView(bottom, blp);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBar();
            }
        });
        setContentView(root);

        ArrayList<String> options = new ArrayList<String>();
        options.add("--network-caching=3000");
        options.add("--avcodec-threads=0");
        options.add("--http-reconnect");
        libVLC = new LibVLC(this, options);
        player = new MediaPlayer(libVLC);
        player.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event e) {
                if (e.type == MediaPlayer.Event.Paused) {
                    playBtn.setText("▶ Oynat");
                    info.setText("Duraklatıldı");
                    showBar();
                } else if (e.type == MediaPlayer.Event.EndReached) {
                    info.setText("Bitti");
                    showBar();
                } else if (e.type == MediaPlayer.Event.Playing) {
                    playBtn.setText("⏸ Duraklat");
                    info.setText("Oynatılıyor");
                    handler.removeCallbacks(readCodecs);
                    handler.postDelayed(readCodecs, 1500);
                    handler.removeCallbacks(blackCheck);
                    handler.postDelayed(blackCheck, 6000);
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
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setFocusable(true);
        b.setBackground(focusBg());
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
                scheduleHide();
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        b.setLayoutParams(lp);
        b.setMinHeight(dp(48));
        return b;
    }

    /** Kumandada seçili düğme belirgin (camgöbeği çerçeve) görünür. */
    private StateListDrawable focusBg() {
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(Color.parseColor("#33FFFFFF"));
        normal.setCornerRadius(dp(8));
        GradientDrawable focused = new GradientDrawable();
        focused.setColor(Color.parseColor("#660B6BCB"));
        focused.setCornerRadius(dp(8));
        focused.setStroke(dp(3), Color.parseColor("#19E3FF"));
        StateListDrawable sl = new StateListDrawable();
        sl.addState(new int[] {android.R.attr.state_focused}, focused);
        sl.addState(new int[] {android.R.attr.state_pressed}, focused);
        sl.addState(new int[] {}, normal);
        return sl;
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
        if (curUrl == null || !curUrl.equals(url)) {
            swDecode = false;
            swTried = false;
        }
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
        m.setHWDecoderEnabled(!swDecode, false);
        if (swDecode) {
            m.addOption(":avcodec-hw=none");
        }
        m.addOption(":network-caching=3000");
        player.setMedia(m);
        m.release();
        player.play();
    }

    private void goNumber() {
        if (numBuf.length() == 0) {
            return;
        }
        String n = numBuf.toString();
        numBuf.setLength(0);
        info.setText("Kanal " + n + " açılıyor…");
        MainActivity.js("window.__goNum&&window.__goNum(" + n + ")");
    }

    private void zap(int d) {
        info.setText("Kanal değişiyor…");
        MainActivity.js("window.__zap&&window.__zap(" + d + ")");
    }

    private void toggleBar() {
        if (bar.getVisibility() == View.VISIBLE) {
            bar.setVisibility(View.GONE);
            bottom.setVisibility(View.GONE);
            handler.removeCallbacks(tick);
        } else {
            showBar();
            scheduleHide();
        }
    }

    private void showBar() {
        handler.removeCallbacks(hideBar);
        bar.setVisibility(View.VISIBLE);
        handler.removeCallbacks(tick);
        handler.post(tick);
        if (buttonsRow != null && getCurrentFocus() == null && buttonsRow.getChildCount() > 0) {
            buttonsRow.getChildAt(0).requestFocus();
        }
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
            String vinfo = "";
            int n = m.getTrackCount();
            for (int i = 0; i < n; i++) {
                Media.Track t = m.getTrack(i);
                if (t != null && t.type == Media.Track.Type.Video && t instanceof Media.VideoTrack) {
                    Media.VideoTrack vt = (Media.VideoTrack) t;
                    int h = vt.height;
                    String q = h >= 2000 ? "4K Ultra HD" : h >= 1400 ? "2K" : h >= 1000 ? "Full HD" : h >= 700 ? "HD" : "";
                    String vc = vt.codec == null ? "" : vt.codec.toLowerCase();
                    String vn = vc.startsWith("hev") || vc.startsWith("hvc") || vc.equals("h265") ? "HEVC" : vc.equals("h264") || vc.startsWith("avc") ? "H.264" : vc.startsWith("av01") ? "AV1" : vc.startsWith("vp9") || vc.equals("vp09") ? "VP9" : vc.toUpperCase();
                    if (vt.width > 0 && h > 0) {
                        vinfo = vt.width + "×" + h + (q.length() > 0 ? " " + q : "") + (vn.length() > 0 ? " " + vn : "");
                    }
                }
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
            if (sb.length() > 0 || vinfo.length() > 0) {
                info.setText((vinfo.length() > 0 ? "Video: " + vinfo : "") + (vinfo.length() > 0 && sb.length() > 0 ? "   " : "") + (sb.length() > 0 ? "Ses: " + sb : ""));
            }
        } catch (Throwable ignored) {
            // bilgi satırı isteğe bağlıdır
        }
    }

    private static String fmt(long ms) {
        long sec = Math.max(0, ms / 1000);
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long x = sec % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, x) : String.format("%d:%02d", m, x);
    }

    private void updateProgress() {
        if (player == null || bar == null) {
            return;
        }
        long len = player.getLength();
        boolean vod = len > 0;
        bottom.setVisibility(vod && bar.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        if (vod && !userSeeking) {
            long t = player.getTime();
            tPos.setText(fmt(t));
            tLen.setText(fmt(len));
            seek.setProgress((int) Math.min(1000, (t * 1000L) / len));
        }
    }

    private void togglePlay() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private void seekBy(long delta) {
        long len = player.getLength();
        if (len <= 0) {
            info.setText("Canlı yayında sarma yok.");
            return;
        }
        player.setTime(Math.max(0, Math.min(player.getTime() + delta, len - 1000)));
        updateProgress();
    }

    private void nextAspect() {
        aspectIdx = (aspectIdx + 1) % ASPECTS.length;
        try {
            player.setVideoScale(ASPECTS[aspectIdx]);
        } catch (Throwable ignored) {
            // eski sürümlerde yok sayılır
        }
        aspectBtn.setText("Oran: " + ASPECT_NAMES[aspectIdx]);
    }

    private void pickSub() {
        final MediaPlayer.TrackDescription[] tracks = player.getSpuTracks();
        if (tracks == null || tracks.length == 0) {
            info.setText("Bu yayında altyazı yok.");
            return;
        }
        String[] names = new String[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            names[i] = tracks[i].name;
        }
        new AlertDialog.Builder(this)
                .setTitle("Altyazı")
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        player.setSpuTrack(tracks[which].id);
                        immersive();
                    }
                })
                .show();
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
        if (bar.getVisibility() == View.VISIBLE) {
            scheduleHide();
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (bar.getVisibility() == View.VISIBLE) {
                    break;
                }
                zap(1);
                return true;
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                zap(1);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (bar.getVisibility() == View.VISIBLE) {
                    break;
                }
                zap(-1);
                return true;
            case KeyEvent.KEYCODE_CHANNEL_UP:
                zap(-1);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if ((bar.getVisibility() != View.VISIBLE
                        || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
                        || keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) && player.getLength() > 0) {
                    boolean back = keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND;
                    long target = player.getTime() + (back ? -10000 : 10000);
                    player.setTime(Math.max(0, Math.min(target, player.getLength() - 1000)));
                    info.setText((back ? "◀ -10 sn" : "+10 sn ▶"));
                    showBar();
                    scheduleHide();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_PROG_RED:
                showBar();
                nextAspect();
                return true;
            case KeyEvent.KEYCODE_PROG_GREEN:
                pickAudio();
                return true;
            case KeyEvent.KEYCODE_PROG_YELLOW:
                pickSub();
                return true;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case KeyEvent.KEYCODE_INFO:
            case KeyEvent.KEYCODE_MENU:
                showBar();
                showCodecs();
                scheduleHide();
                return true;
            case KeyEvent.KEYCODE_LAST_CHANNEL:
                MainActivity.js("window.__rc&&window.__rc('last')");
                return true;
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
                if (numBuf.length() < 4) {
                    numBuf.append((char) ('0' + (keyCode - KeyEvent.KEYCODE_0)));
                }
                info.setText("Kanal numarası: " + numBuf);
                showBar();
                handler.removeCallbacks(numGo);
                handler.postDelayed(numGo, 1600);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (numBuf.length() > 0) {
                    handler.removeCallbacks(numGo);
                    goNumber();
                    return true;
                }
                if (bar.getVisibility() != View.VISIBLE) {
                    showBar();
                    scheduleHide();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                player.play();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                player.pause();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
                togglePlay();
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
