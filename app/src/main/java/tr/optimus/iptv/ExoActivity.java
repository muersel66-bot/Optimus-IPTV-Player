package tr.optimus.iptv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

/**
 * ExoPlayer (Media3) oynatıcısı: HLS, DASH, MP4/MKV, HEVC, HDR; ses/altyazı/kalite seçimi denetleyici çubuğundaki ayar düğmesindedir.
 * Oynatma hata verirse otomatik olarak VLC oynatıcıya (PlayerActivity) geçer.
 */
public class ExoActivity extends Activity {
    static ExoActivity instance;

    private ExoPlayer player;
    private PlayerView view;
    private TextView title;
    private String curUrl = "";
    private String curName = "";
    private boolean fellBack = false;
    private boolean pipRequested = false;
    private ChannelPanel panel;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        instance = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        view = new PlayerView(this);
        view.setUseController(true);
        view.setControllerShowTimeoutMs(4000);
        view.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        root.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        title = new TextView(this);
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setPadding(32, 24, 32, 24);
        title.setShadowLayer(4, 0, 0, Color.BLACK);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START);
        root.addView(title, lp);
        panel = new ChannelPanel(this, root, new ChannelPanel.Pick() {
            @Override
            public void onPick(String url, String name) {
                load(url, name);
                MainActivity.js("window.__nativePick&&window.__nativePick(" + org.json.JSONObject.quote(url) + ")");
            }
        });
        setContentView(root);

        DefaultRenderersFactory rf = new DefaultRenderersFactory(this)
                .setEnableDecoderFallback(true);
        player = new ExoPlayer.Builder(this, rf).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                fallBackToVlc(error);
            }
        });
        view.setPlayer(player);
        handle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handle(intent);
    }

    private void handle(Intent i) {
        if (i == null) return;
        String u = i.getStringExtra("url");
        String n = i.getStringExtra("name");
        if (u != null && u.length() > 0) load(u, n == null ? "" : n);
    }

    /** Başka bir yayına geç (sayfadan kanal değişince). */
    void load(String url, String name) {
        curUrl = url;
        curName = name == null ? "" : name;
        fellBack = false;
        title.setText(curName);
        try {
            player.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
            player.prepare();
            player.setPlayWhenReady(true);
        } catch (Throwable e) {
            fallBackToVlc(null);
        }
    }

    private void fallBackToVlc(PlaybackException error) {
        if (fellBack || curUrl.length() == 0) {
            Toast.makeText(this, "Yayın açılamadı.", Toast.LENGTH_LONG).show();
            return;
        }
        fellBack = true;
        Toast.makeText(this, "ExoPlayer bu yayını çözemedi, VLC açılıyor…", Toast.LENGTH_LONG).show();
        try {
            Intent i = new Intent(this, PlayerActivity.class);
            i.putExtra("url", curUrl);
            i.putExtra("name", curName);
            startActivity(i);
        } catch (Throwable e) { }
        finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (panel != null && panel.handleKey(e)) return true;
        // Geri tuşu kapatır; diğer tuşları (yön, TAMAM, ses, oynat/duraklat) PlayerView işler.
        if (e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (e.getAction() == KeyEvent.ACTION_UP) {
                if (Pip.enter(this)) {
                    pipRequested = true;
                } else {
                    finish();
                }
            }
            return true;
        }
        if (view != null && view.dispatchKeyEvent(e)) return true;
        return super.dispatchKeyEvent(e);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean inPip, android.content.res.Configuration cfg) {
        super.onPictureInPictureModeChanged(inPip, cfg);
        if (view != null) view.setUseController(!inPip);
        if (inPip && panel != null) panel.close();
        if (title != null) title.setVisibility(inPip ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pipRequested = false;
        if (player != null) player.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) player.pause();
        if (pipRequested) {
            // küçük pencere kapatıldı
            pipRequested = false;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (instance == this) instance = null;
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
