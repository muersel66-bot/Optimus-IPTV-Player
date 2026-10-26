package tr.optimus.iptv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * OPTİMUS İptv Player PRO: oynatıcı sayfasını (assets/index.html) bir WebView içinde çalıştırır.
 * Sayfa, ağ isteklerini "Native" köprüsü üzerinden yapabilir; böylece CORS ve Cookie başlığı sınırı olmaz.
 */
public class MainActivity extends Activity {

    private static final int REQ_FILE = 11;
    private static final int CHUNK = 400000;
    private static final int MAX_BODY = 80 * 1024 * 1024;
    private static final int BG_DARK = Color.parseColor("#0D1014");
    private static final int BG_LIGHT = Color.parseColor("#EEF0F3");
    private static final long EXIT_WINDOW_MS = 2000;

    static MainActivity self;

    private WebView web;
    private FrameLayout root;
    private View customView;
    private WebChromeClient.CustomViewCallback customCb;
    private ValueCallback<Uri[]> fileCb;
    private boolean darkUi = true;
    private boolean edgeToEdge;
    private long lastBack;
    private int[] insetsDp = {0, 0, 0, 0};

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<Integer, String> bodies = new ConcurrentHashMap<Integer, String>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        self = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        root = new FrameLayout(this);
        web = new WebView(this);
        // Pencere arka planı (tema rengi) sayfa çizilene kadar görünür: beyaz parlama olmaz
        web.setBackgroundColor(Color.TRANSPARENT);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        setupSystemBars();

        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        // hls.js / mpegts.js akışları doğrudan çeker: file:// sayfasından CORS sınırı olmadan
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        web.addJavascriptInterface(new Net(), "Native");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("file".equals(u.getScheme())) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (ActivityNotFoundException e) {
                    // açacak uygulama yok
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pushInsets();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customCb = callback;
                web.setVisibility(View.GONE);
                root.addView(view, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                immersive(true);
            }

            @Override
            public void onHideCustomView() {
                hideCustom();
            }

            @Override
            public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (fileCb != null) {
                    fileCb.onReceiveValue(null);
                }
                fileCb = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(i, "Dosya seç"), REQ_FILE);
                } catch (ActivityNotFoundException e) {
                    fileCb = null;
                    cb.onReceiveValue(null);
                }
                return true;
            }
        });

        if (state != null) {
            web.restoreState(state);
        }
        if (web.getUrl() == null) {
            web.loadUrl("file:///android_asset/index.html");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    /**
     * Android 11+: uçtan uca çizim (kamera deliği ve sistem çubukları dahil). Güvenli alan boşlukları
     * sayfaya CSS değişkeni olarak iletilir, klavye açıkken sayfa klavyenin üstünde kalır.
     * Daha eski sürümlerde düz renkli sistem çubukları kullanılır.
     */
    private void setupSystemBars() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            w.setAttributes(lp);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            edgeToEdge = true;
            w.setDecorFitsSystemWindows(false);
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.TRANSPARENT);
            w.setNavigationBarContrastEnforced(false);
            root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                    Insets ime = insets.getInsets(WindowInsets.Type.ime());
                    boolean imeOn = insets.isVisible(WindowInsets.Type.ime());
                    v.setPadding(0, 0, 0, imeOn ? ime.bottom : 0);
                    float d = getResources().getDisplayMetrics().density;
                    insetsDp = new int[]{
                            Math.round(bars.top / d), Math.round(bars.right / d),
                            imeOn ? 0 : Math.round(bars.bottom / d), Math.round(bars.left / d)};
                    pushInsets();
                    return WindowInsets.CONSUMED;
                }
            });
        } else {
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        applyBarColors();
    }

    private void pushInsets() {
        if (web == null) {
            return;
        }
        web.evaluateJavascript("(function(s){s.setProperty('--native-inset-top','" + insetsDp[0] + "px');"
                + "s.setProperty('--native-inset-right','" + insetsDp[1] + "px');"
                + "s.setProperty('--native-inset-bottom','" + insetsDp[2] + "px');"
                + "s.setProperty('--native-inset-left','" + insetsDp[3] + "px')})(document.documentElement.style)", null);
    }

    /** Sistem çubuğu simgeleri temaya uyar: açık temada koyu simgeler, koyu temada açık simgeler. */
    private void applyBarColors() {
        Window w = getWindow();
        int bg = darkUi ? BG_DARK : BG_LIGHT;
        root.setBackgroundColor(bg);
        if (edgeToEdge) {
            WindowInsetsController c = w.getInsetsController();
            if (c != null) {
                int light = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                c.setSystemBarsAppearance(darkUi ? 0 : light, light);
            }
        } else {
            w.setStatusBarColor(bg);
            w.setNavigationBarColor(bg);
            View d = w.getDecorView();
            int flags = d.getSystemUiVisibility();
            int light = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            d.setSystemUiVisibility(darkUi ? (flags & ~light) : (flags | light));
        }
    }

    /** Başka bir ekrandan sayfaya JavaScript gönderir. */
    static void js(final String code) {
        final MainActivity m = self;
        if (m == null || m.web == null) {
            return;
        }
        m.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m.web.evaluateJavascript(code, null);
            }
        });
    }

    /** Yayını MX Player Pro'da (yoksa MX Player'da) açar; ikisi de yoksa Play Store'u açar. */
    private void openInMx(String url, String name) {
        String[] pkgs = {"com.mxtech.videoplayer.pro", "com.mxtech.videoplayer.ad"};
        for (int i = 0; i < pkgs.length; i++) {
            try {
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setDataAndType(Uri.parse(url), "video/*");
                it.setPackage(pkgs[i]);
                it.putExtra("title", name == null ? "" : name);
                it.putExtra("return_result", false);
                startActivity(it);
                return;
            } catch (ActivityNotFoundException e) {
                // sıradaki paketi dene
            }
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mxtech.videoplayer.pro")));
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.mxtech.videoplayer.pro")));
            } catch (ActivityNotFoundException ignored) {
                // tarayıcı da yok
            }
        }
        js("window.__mxMissing&&window.__mxMissing()");
    }

    private void immersive(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                if (on) {
                    c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    c.hide(WindowInsets.Type.systemBars());
                } else {
                    c.show(WindowInsets.Type.systemBars());
                }
            }
            return;
        }
        View d = getWindow().getDecorView();
        if (on) {
            d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } else {
            d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            applyBarColors();
        }
    }

    private void hideCustom() {
        if (customView == null) {
            return;
        }
        root.removeView(customView);
        customView = null;
        web.setVisibility(View.VISIBLE);
        immersive(false);
        if (customCb != null) {
            customCb.onCustomViewHidden();
            customCb = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE && fileCb != null) {
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                result = new Uri[]{data.getData()};
            }
            fileCb.onReceiveValue(result);
            fileCb = null;
        }
    }

    /**
     * Geri: önce tam ekran, sonra sayfadaki açık öğeler (arama, liste yükleme paneli…), en sonda
     * "çıkmak için tekrar basın". Uygulama ilk geri hareketinde kapanmaz.
     */
    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustom();
            return;
        }
        web.evaluateJavascript("(function(){try{return !!(window.__nativeBack&&window.__nativeBack());}catch(e){return false;}})()",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String handled) {
                        if ("true".equals(handled)) {
                            return;
                        }
                        long now = SystemClock.elapsedRealtime();
                        if (now - lastBack < EXIT_WINDOW_MS) {
                            finish();
                        } else {
                            lastBack = now;
                            Toast.makeText(MainActivity.this, "Çıkmak için tekrar geri tuşuna basın", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Kumanda/Android TV: geri tuşu tam ekrandan çıkarır
        if (keyCode == KeyEvent.KEYCODE_BACK && customView != null) {
            hideCustom();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (self == this) {
            self = null;
        }
        pool.shutdownNow();
        root.removeAllViews();
        web.destroy();
        super.onDestroy();
    }

    /** Sayfanın çağırdığı yerel ağ köprüsü. */
    public class Net {

        @JavascriptInterface
        public void request(final int id, final String url, final String headersJson, final boolean probe) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    doRequest(id, url, headersJson, probe);
                }
            });
        }

        @JavascriptInterface
        public boolean hasVlc() {
            return true;
        }

        @JavascriptInterface
        public String version() {
            return BuildConfig.VERSION_NAME;
        }

        /** Sayfanın teması değişince sistem çubuklarını uyarlar. */
        @JavascriptInterface
        public void setTheme(final boolean dark) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    darkUi = dark;
                    applyBarColors();
                }
            });
        }

        @JavascriptInterface
        public void playNative(final String url, final String name) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PlayerActivity p = PlayerActivity.instance;
                    if (p != null && !p.isFinishing()) {
                        p.load(url, name);
                    } else {
                        Intent i = new Intent(MainActivity.this, PlayerActivity.class);
                        i.putExtra("url", url);
                        i.putExtra("name", name);
                        startActivity(i);
                    }
                }
            });
        }

        @JavascriptInterface
        public void playMx(final String url, final String name) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    openInMx(url, name);
                }
            });
        }

        @JavascriptInterface
        public String chunk(int id, int index) {
            String b = bodies.get(id);
            if (b == null) {
                return "";
            }
            int start = index * CHUNK;
            if (start >= b.length()) {
                return "";
            }
            return b.substring(start, Math.min(b.length(), start + CHUNK));
        }

        @JavascriptInterface
        public void release(int id) {
            bodies.remove(id);
        }
    }

    private void doRequest(int id, String url, String headersJson, boolean probe) {
        int status = -1;
        String ctype = "";
        int chunks = 0;
        String err = "";
        HttpURLConnection c = null;
        try {
            JSONObject hs = (headersJson == null || headersJson.length() == 0)
                    ? new JSONObject() : new JSONObject(headersJson);
            String cur = url;
            for (int hop = 0; hop < 6; hop++) {
                c = (HttpURLConnection) new URL(cur).openConnection();
                c.setInstanceFollowRedirects(false);
                c.setConnectTimeout(15000);
                c.setReadTimeout(30000);
                c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) OPTIMUS-IPTV");
                Iterator<String> it = hs.keys();
                while (it.hasNext()) {
                    String k = it.next();
                    c.setRequestProperty(k, hs.getString(k));
                }
                status = c.getResponseCode();
                if (status >= 300 && status < 400) {
                    String loc = c.getHeaderField("Location");
                    if (loc != null) {
                        cur = new URL(new URL(cur), loc).toString();
                        c.disconnect();
                        c = null;
                        continue;
                    }
                }
                break;
            }
            if (c == null) {
                throw new IOException("Çok fazla yönlendirme");
            }
            String ct = c.getContentType();
            ctype = ct == null ? "" : ct;
            if (!probe) {
                InputStream in = status >= 400 ? c.getErrorStream() : c.getInputStream();
                String body = "";
                if (in != null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[16384];
                    int n;
                    int total = 0;
                    try {
                        while ((n = in.read(buf)) != -1) {
                            total += n;
                            if (total > MAX_BODY) {
                                throw new IOException("Yanıt çok büyük");
                            }
                            out.write(buf, 0, n);
                        }
                    } finally {
                        in.close();
                    }
                    body = new String(out.toByteArray(), "UTF-8");
                }
                bodies.put(id, body);
                chunks = (body.length() + CHUNK - 1) / CHUNK;
            }
        } catch (Exception e) {
            status = -1;
            err = String.valueOf(e.getMessage());
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
        final String js = "window.__nat&&window.__nat(" + id + "," + status + ","
                + JSONObject.quote(ctype) + "," + chunks + "," + JSONObject.quote(err) + ")";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (web != null) {
                    web.evaluateJavascript(js, null);
                }
            }
        });
    }
}
