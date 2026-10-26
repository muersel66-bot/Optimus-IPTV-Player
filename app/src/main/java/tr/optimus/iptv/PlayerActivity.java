package tr.optimus.iptv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
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

    private static final String UA = "Mozilla/5.0 (Linux; Android) OPTIMUS-IPTV";
    private static final int MAX_RETRIES = 3;
    private static final MediaPlayer.ScaleType[] SCALES = {
            MediaPlayer.ScaleType.SURFACE_BEST_FIT,
            MediaPlayer.ScaleType.SURFACE_FILL,
            MediaPlayer.ScaleType.SURFACE_16_9,
            MediaPlayer.ScaleType.SURFACE_4_3,
            MediaPlayer.ScaleType.SURFACE_ORIGINAL};
    private static final String[] SCALE_NAMES = {"Sığdır", "Doldur", "16:9", "4:3", "Orijinal"};

    private LibVLC libVLC;
    private MediaPlayer player;
    private VLCVideoLayout video;
    private LinearLayout top;
    private LinearLayout controls;
    private TextView title;
    private TextView info;
    private Button playBtn;
    private Button scaleBtn;
    private String curUrl = "";
    private String curName = "";
    private int retries;
    private int scale;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable hideBar = new Runnable() {
        @Override
        public void run() {
            setBarsVisible(false);
        }
    };

    private final Runnable readCodecs = new Runnable() {
        @Override
        public void run() {
            showCodecs();
        }
    };

    private final Runnable reconnect = new Runnable() {
        @Override
        public void run() {
            if (curUrl.length() > 0) {
                startMedia();
            }
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        instance = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams wlp = getWindow().getAttributes();
            wlp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(wlp);
        }
        immersive();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        video = new VLCVideoLayout(this);
        root.addView(video, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Üst çubuk: kanal adı, durum, kapat
        top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackground(gradient(GradientDrawable.Orientation.TOP_BOTTOM));
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        title = new TextView(this);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        info = new TextView(this);
        info.setTextColor(Color.parseColor("#9FC4FF"));
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        texts.addView(title);
        texts.addView(info);
        top.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button close = button("✕", "Kapat", new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
        top.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.gravity = Gravity.TOP;
        root.addView(top, tlp);

        // Alt çubuk: kanal değiştirme ve oynatma kontrolleri
        controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);
        controls.setBackground(gradient(GradientDrawable.Orientation.BOTTOM_TOP));
        controls.addView(weighted(button("◀", "Önceki kanal", new Runnable() {
            @Override
            public void run() {
                zap(-1);
            }
        })));
        playBtn = button("❚❚", "Duraklat", new Runnable() {
            @Override
            public void run() {
                togglePlay();
            }
        });
        controls.addView(weighted(playBtn));
        controls.addView(weighted(button("▶", "Sonraki kanal", new Runnable() {
            @Override
            public void run() {
                zap(1);
            }
        })));
        controls.addView(weighted(button("Ses", "Ses parçası", new Runnable() {
            @Override
            public void run() {
                pickAudio();
            }
        })));
        controls.addView(weighted(button("Altyazı", "Altyazı", new Runnable() {
            @Override
            public void run() {
                pickSubtitle();
            }
        })));
        scaleBtn = button(SCALE_NAMES[0], "Görüntü oranı", new Runnable() {
            @Override
            public void run() {
                cycleScale();
            }
        });
        controls.addView(weighted(scaleBtn));
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.gravity = Gravity.BOTTOM;
        root.addView(controls, clp);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBar();
            }
        });
        // Kamera deliği ve sistem çubukları: kontroller bu alanların dışında kalır
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int l;
                int t;
                int r;
                int b;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    android.graphics.Insets i = insets.getInsetsIgnoringVisibility(
                            WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                    l = i.left;
                    t = i.top;
                    r = i.right;
                    b = i.bottom;
                } else {
                    l = insets.getStableInsetLeft();
                    t = insets.getStableInsetTop();
                    r = insets.getStableInsetRight();
                    b = insets.getStableInsetBottom();
                }
                int pad = dp(12);
                top.setPadding(pad + l, pad + t, pad / 2 + r, dp(24));
                controls.setPadding(pad + l, dp(24), pad + r, pad + b);
                return insets;
            }
        });
        setContentView(root);
        scale = getPreferences(MODE_PRIVATE).getInt("scale", 0);
        if (scale < 0 || scale >= SCALES.length) {
            scale = 0;
        }
        scaleBtn.setText(SCALE_NAMES[scale]);

        ArrayList<String> options = new ArrayList<String>();
        options.add("--network-caching=2000");
        options.add("--http-reconnect");
        options.add("--http-user-agent=" + UA);
        libVLC = new LibVLC(this, options);
        player = new MediaPlayer(libVLC);
        player.setVideoScale(SCALES[scale]);
        player.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event e) {
                if (e.type == MediaPlayer.Event.Playing) {
                    retries = 0;
                    info.setText("Oynatılıyor");
                    playBtn.setText("❚❚");
                    playBtn.setContentDescription("Duraklat");
                    handler.removeCallbacks(readCodecs);
                    handler.postDelayed(readCodecs, 1500);
                    scheduleHide();
                } else if (e.type == MediaPlayer.Event.Paused) {
                    playBtn.setText("▶︎");
                    playBtn.setContentDescription("Oynat");
                    showBar();
                } else if (e.type == MediaPlayer.Event.EncounteredError) {
                    retryOrFail("Yayın açılamadı. Adres kapalı veya erişilemiyor olabilir.");
                } else if (e.type == MediaPlayer.Event.EndReached) {
                    // Canlı yayın kesildiyse yeniden bağlan; dosya bittiyse çubuğu göster
                    if (player.getLength() <= 0) {
                        retryOrFail("Yayın kesildi.");
                    } else {
                        info.setText("Bitti");
                        playBtn.setText("▶︎");
                        showBar();
                    }
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

    private GradientDrawable gradient(GradientDrawable.Orientation o) {
        return new GradientDrawable(o, new int[]{Color.parseColor("#D9000000"), Color.TRANSPARENT});
    }

    private Button button(String text, String label, final Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        b.setContentDescription(label);
        b.setMinHeight(dp(48));
        b.setMinimumHeight(dp(48));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#33FFFFFF"));
        bg.setCornerRadius(dp(10));
        b.setBackground(bg);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
                scheduleHide();
            }
        });
        return b;
    }

    private View weighted(View v) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        v.setLayoutParams(lp);
        return v;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private void immersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                c.hide(WindowInsets.Type.systemBars());
            }
            return;
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            immersive();
        }
    }

    /** Sayfadan yeni kanal geldiğinde çağrılır. */
    void load(String url, String name) {
        curUrl = url;
        curName = name == null ? "" : name;
        retries = 0;
        title.setText(curName);
        info.setText("Bağlanılıyor…");
        showBar();
        startMedia();
    }

    private void startMedia() {
        handler.removeCallbacks(readCodecs);
        handler.removeCallbacks(reconnect);
        if (!player.getVLCVout().areViewsAttached()) {
            player.attachViews(video, null, true, false);
        }
        Media m = new Media(libVLC, Uri.parse(curUrl));
        m.setHWDecoderEnabled(true, false);
        m.addOption(":network-caching=2000");
        m.addOption(":http-user-agent=" + UA);
        player.setMedia(m);
        m.release();
        player.play();
    }

    private void retryOrFail(String msg) {
        if (retries < MAX_RETRIES && curUrl.length() > 0) {
            retries++;
            info.setText(msg + " Yeniden bağlanılıyor (" + retries + "/" + MAX_RETRIES + ")…");
            handler.removeCallbacks(reconnect);
            handler.postDelayed(reconnect, 1500L * retries);
        } else {
            info.setText(msg);
            playBtn.setText("▶︎");
        }
        showBar();
    }

    private void togglePlay() {
        if (player.isPlaying()) {
            player.pause();
        } else if (player.getPlayerState() == IMedia.State.Ended
                || player.getPlayerState() == IMedia.State.Error
                || player.getPlayerState() == IMedia.State.Stopped) {
            retries = 0;
            info.setText("Bağlanılıyor…");
            startMedia();
        } else {
            player.play();
        }
    }

    private void cycleScale() {
        scale = (scale + 1) % SCALES.length;
        player.setVideoScale(SCALES[scale]);
        scaleBtn.setText(SCALE_NAMES[scale]);
        getPreferences(MODE_PRIVATE).edit().putInt("scale", scale).apply();
    }

    private void zap(int d) {
        info.setText("Kanal değişiyor…");
        MainActivity.js("window.__zap&&window.__zap(" + d + ")");
    }

    private void toggleBar() {
        if (top.getVisibility() == View.VISIBLE) {
            setBarsVisible(false);
        } else {
            showBar();
            scheduleHide();
        }
    }

    private void setBarsVisible(boolean on) {
        int v = on ? View.VISIBLE : View.GONE;
        top.setVisibility(v);
        controls.setVisibility(v);
    }

    private void showBar() {
        handler.removeCallbacks(hideBar);
        if (!isInPip()) {
            setBarsVisible(true);
        }
    }

    private void scheduleHide() {
        handler.removeCallbacks(hideBar);
        if (player != null && player.isPlaying()) {
            handler.postDelayed(hideBar, 5000);
        }
    }

    private boolean isInPip() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode();
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

    /** Akıştaki görüntü ve ses kodeklerini gösterir (örneğin "1920×1080 · Dolby Digital Plus 5.1"). */
    private void showCodecs() {
        try {
            IMedia m = player.getMedia();
            if (m == null) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            String res = "";
            int n = m.getTrackCount();
            for (int i = 0; i < n; i++) {
                IMedia.Track t = m.getTrack(i);
                if (t == null) {
                    continue;
                }
                if (t.type == IMedia.Track.Type.Video && t instanceof IMedia.VideoTrack && res.length() == 0) {
                    IMedia.VideoTrack vt = (IMedia.VideoTrack) t;
                    if (vt.width > 0 && vt.height > 0) {
                        res = vt.width + "×" + vt.height;
                    }
                } else if (t.type == IMedia.Track.Type.Audio) {
                    int ch = 0;
                    if (t instanceof IMedia.AudioTrack) {
                        ch = ((IMedia.AudioTrack) t).channels;
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
            String text = res;
            if (sb.length() > 0) {
                text = (text.length() > 0 ? text + "  ·  " : "") + "Ses: " + sb;
            }
            if (text.length() > 0) {
                info.setText(text);
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
        pickTrack("Ses parçası", tracks, player.getAudioTrack(), new TrackSetter() {
            @Override
            public void set(int id) {
                player.setAudioTrack(id);
            }
        });
    }

    private void pickSubtitle() {
        final MediaPlayer.TrackDescription[] tracks = player.getSpuTracks();
        if (tracks == null || tracks.length == 0) {
            info.setText("Bu yayında altyazı yok.");
            return;
        }
        pickTrack("Altyazı", tracks, player.getSpuTrack(), new TrackSetter() {
            @Override
            public void set(int id) {
                player.setSpuTrack(id);
            }
        });
    }

    private interface TrackSetter {
        void set(int id);
    }

    private void pickTrack(String heading, final MediaPlayer.TrackDescription[] tracks, int currentId, final TrackSetter setter) {
        String[] names = new String[tracks.length];
        int checked = -1;
        for (int i = 0; i < tracks.length; i++) {
            names[i] = tracks[i].id == -1 ? "Kapalı" : tracks[i].name;
            if (tracks[i].id == currentId) {
                checked = i;
            }
        }
        handler.removeCallbacks(hideBar);
        new AlertDialog.Builder(this)
                .setTitle(heading)
                .setSingleChoiceItems(names, checked, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setter.set(tracks[which].id);
                        dialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        immersive();
                        scheduleHide();
                    }
                })
                .show();
    }

    /** Ana ekrana dönülürken oynatma küçük pencerede (resim içinde resim) sürer. */
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && player != null && player.isPlaying()
                && getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            try {
                enterPictureInPictureMode(new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(16, 9)).build());
            } catch (IllegalStateException ignored) {
                // cihaz PiP'e izin vermiyor
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean inPip, Configuration cfg) {
        super.onPictureInPictureModeChanged(inPip, cfg);
        if (inPip) {
            handler.removeCallbacks(hideBar);
            setBarsVisible(false);
        } else {
            showBar();
            scheduleHide();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                if (top.getVisibility() != View.VISIBLE) {
                    zap(1);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_CHANNEL_UP:
                if (top.getVisibility() != View.VISIBLE) {
                    zap(-1);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (top.getVisibility() != View.VISIBLE) {
                    showBar();
                    scheduleHide();
                    controls.getChildAt(1).requestFocus();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
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
        // Küçük pencere kapatıldıysa etkinliği de kapat
        if (isInPip()) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (instance == this) {
            instance = null;
        }
        handler.removeCallbacksAndMessages(null);
        player.release();
        libVLC.release();
        MainActivity.js("window.__vlcClosed&&window.__vlcClosed()");
        super.onDestroy();
    }
}
