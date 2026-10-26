package tr.optimus.iptv;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
    private static final String BG = "#0D1014";

    static MainActivity self;

    private WebView web;
    private FrameLayout root;
    private View customView;
    private WebChromeClient.CustomViewCallback customCb;
    private ValueCallback<Uri[]> fileCb;

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<Integer, String> bodies = new ConcurrentHashMap<Integer, String>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        self = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.parseColor(BG));
        getWindow().setNavigationBarColor(Color.parseColor(BG));

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor(BG));
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor(BG));
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
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

        web.loadUrl("file:///android_asset/index.html");
    }

    private DownloadManager dm() {
        return (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
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
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.mxtech.videoplayer.pro")));
        }
        js("window.__mxMissing&&window.__mxMissing()");
    }

    private void immersive(boolean on) {
        View d = getWindow().getDecorView();
        if (on) {
            d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } else {
            d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
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

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustom();
        } else {
            super.onBackPressed();
        }
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
        self = null;
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
        public boolean hasDownloads() {
            return true;
        }

        /** Sistem indirme yöneticisine bir indirme ekler; indirme numarasını döndürür, hata olursa -1. */
        @JavascriptInterface
        public double startDownload(String url, String filename, String title, boolean wifiOnly) {
            try {
                DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
                r.setTitle(title == null ? filename : title);
                r.setDescription("OPTİMUS İptv Player PRO");
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                r.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_MOVIES, filename);
                r.addRequestHeader("User-Agent", "Mozilla/5.0 (Linux; Android) OPTIMUS-IPTV");
                if (wifiOnly) {
                    r.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                }
                r.setAllowedOverMetered(!wifiOnly);
                r.setAllowedOverRoaming(!wifiOnly);
                return (double) dm().enqueue(r);
            } catch (Exception e) {
                return -1;
            }
        }

        /** Verilen indirme numaralarının durumunu JSON olarak döndürür. */
        @JavascriptInterface
        public String downloads(String idsJson) {
            Cursor c = null;
            try {
                JSONArray ids = new JSONArray(idsJson);
                long[] arr = new long[ids.length()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = ids.getLong(i);
                }
                c = dm().query(new DownloadManager.Query().setFilterById(arr));
                JSONArray out = new JSONArray();
                if (c != null) {
                    while (c.moveToNext()) {
                        JSONObject o = new JSONObject();
                        o.put("id", c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)));
                        o.put("status", c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)));
                        o.put("bytes", c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
                        o.put("total", c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)));
                        o.put("reason", c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)));
                        String uri = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                        o.put("uri", uri == null ? "" : uri);
                        out.put(o);
                    }
                }
                return out.toString();
            } catch (Exception e) {
                return "[]";
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        /** İndirmeyi durdurur ve dosyasını siler. */
        @JavascriptInterface
        public void removeDownload(double id) {
            try {
                dm().remove((long) id);
            } catch (Exception e) {
                // zaten silinmiş
            }
        }

        @JavascriptInterface
        public double freeBytes() {
            try {
                File d = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                if (d == null) {
                    d = getFilesDir();
                }
                return (double) new StatFs(d.getAbsolutePath()).getAvailableBytes();
            } catch (Exception e) {
                return 0;
            }
        }

        @JavascriptInterface
        public boolean hasVlc() {
            return true;
        }

        @JavascriptInterface
        public void playNative(final String url, final String name) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PlayerActivity p = PlayerActivity.instance;
                    if (p != null) {
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
                    while ((n = in.read(buf)) != -1) {
                        total += n;
                        if (total > MAX_BODY) {
                            throw new IOException("Yanıt çok büyük");
                        }
                        out.write(buf, 0, n);
                    }
                    in.close();
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
