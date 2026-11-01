package tr.optimus.iptv;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * Yerel ağda çalışan küçük HTTP sunucusu: telefondaki tarayıcıdan (QR kodu ile açılır)
 * kumanda komutu ve sunucu / M3U bilgisi göndermeyi sağlar. Yalnızca rastgele anahtar (k) ile erişilir.
 * Android'e bağımlı değildir.
 */
public class RemoteServer {
    public interface Handler {
        /** Telefondan gelen JSON komutu. */
        void onMessage(String json);
    }

    private final Handler handler;
    private final String pageHtml;
    private ServerSocket server;
    private Thread thread;
    private volatile boolean running;
    private String token = "";
    private int port;
    private static final int MAX_BODY = 64 * 1024;

    public RemoteServer(String pageHtml, Handler handler) {
        this.pageHtml = pageHtml;
        this.handler = handler;
    }

    public synchronized String start() throws Exception {
        if (running) return url();
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(rnd.nextInt(10));
        token = sb.toString();
        ServerSocket s = null;
        for (int p = 8765; p < 8785; p++) {
            try { s = new ServerSocket(p); port = p; break; } catch (Exception e) { s = null; }
        }
        if (s == null) throw new Exception("Boş bağlantı noktası yok");
        server = s;
        running = true;
        thread = new Thread(new Runnable() {
            @Override public void run() { loop(); }
        }, "optimus-remote");
        thread.setDaemon(true);
        thread.start();
        return url();
    }

    public synchronized void stop() {
        running = false;
        try { if (server != null) server.close(); } catch (Exception e) { }
        server = null;
    }

    public boolean isRunning() { return running; }

    public String url() {
        String ip = localIp();
        return "http://" + ip + ":" + port + "/?k=" + token;
    }

    public int port() { return port; }

    public String token() { return token; }

    public static String localIp() {
        String fallback = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> ad = ni.getInetAddresses();
                while (ad.hasMoreElements()) {
                    InetAddress a = ad.nextElement();
                    if (a instanceof Inet4Address && !a.isLoopbackAddress()) {
                        if (a.isSiteLocalAddress()) return a.getHostAddress();
                        fallback = a.getHostAddress();
                    }
                }
            }
        } catch (Exception e) { }
        return fallback;
    }

    private void loop() {
        while (running) {
            try {
                final Socket c = server.accept();
                c.setSoTimeout(8000);
                Thread t = new Thread(new Runnable() {
                    @Override public void run() { serve(c); }
                });
                t.setDaemon(true);
                t.start();
            } catch (Exception e) {
                if (!running) return;
            }
        }
    }

    private static String readLine(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            if (b != '\r') sb.append((char) b);
            if (sb.length() > 8192) throw new Exception("satır çok uzun");
        }
        if (b == -1 && sb.length() == 0) return null;
        return sb.toString();
    }

    private void serve(Socket c) {
        try {
            InputStream in = c.getInputStream();
            OutputStream out = c.getOutputStream();
            String req = readLine(in);
            if (req == null) { c.close(); return; }
            String[] rp = req.split(" ");
            String method = rp.length > 0 ? rp[0] : "";
            String target = rp.length > 1 ? rp[1] : "/";
            int len = 0;
            String line;
            while ((line = readLine(in)) != null && line.length() > 0) {
                int i = line.indexOf(':');
                if (i > 0 && line.substring(0, i).trim().equalsIgnoreCase("content-length")) {
                    try { len = Integer.parseInt(line.substring(i + 1).trim()); } catch (Exception e) { len = 0; }
                }
            }
            String path = target, query = "";
            int q = target.indexOf('?');
            if (q >= 0) { path = target.substring(0, q); query = target.substring(q + 1); }
            boolean ok = false;
            for (String kv : query.split("&")) {
                if (kv.equals("k=" + token)) ok = true;
            }
            if (!ok) { respond(out, 403, "text/plain; charset=utf-8", "Erişim anahtarı geçersiz.".getBytes("UTF-8")); c.close(); return; }
            if ("GET".equals(method) && path.equals("/")) {
                respond(out, 200, "text/html; charset=utf-8", pageHtml.getBytes("UTF-8"));
            } else if ("POST".equals(method) && path.equals("/api")) {
                if (len > MAX_BODY) { respond(out, 413, "text/plain", "Çok büyük".getBytes("UTF-8")); c.close(); return; }
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[2048];
                int left = len;
                while (left > 0) {
                    int n = in.read(buf, 0, Math.min(buf.length, left));
                    if (n < 0) break;
                    bo.write(buf, 0, n);
                    left -= n;
                }
                String body = new String(bo.toByteArray(), "UTF-8");
                try { handler.onMessage(body); } catch (Throwable e) { }
                respond(out, 200, "application/json; charset=utf-8", "{\"ok\":true}".getBytes("UTF-8"));
            } else {
                respond(out, 404, "text/plain", "Bulunamadı".getBytes("UTF-8"));
            }
            c.close();
        } catch (Exception e) {
            try { c.close(); } catch (Exception x) { }
        }
    }

    private static void respond(OutputStream out, int code, String type, byte[] body) throws Exception {
        String st = code == 200 ? "OK" : code == 403 ? "Forbidden" : code == 404 ? "Not Found" : "Error";
        String head = "HTTP/1.1 " + code + " " + st + "\r\nContent-Type: " + type + "\r\nContent-Length: " + body.length
                + "\r\nCache-Control: no-store\r\nConnection: close\r\n\r\n";
        out.write(head.getBytes("UTF-8"));
        out.write(body);
        out.flush();
    }
}
