package tr.optimus.iptv;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

/** Tam ekran oynatıcıda (VLC / ExoPlayer) sol tarafta açılan kanal listesi. Android TV'de kumandanın SOL tuşuyla açılır. */
final class ChannelPanel {

    interface Pick {
        void onPick(String url, String name);
    }

    // Sayfanın gönderdiği liste (yalnızca canlı yayınlar için dolu)
    private static String[] names = new String[0];
    private static String[] urls = new String[0];
    private static String curUrl = "";

    static void set(String json, String current) {
        try {
            JSONArray a = new JSONArray(json == null ? "[]" : json);
            int n = Math.min(a.length(), 4000);
            String[] nm = new String[n];
            String[] ur = new String[n];
            for (int i = 0; i < n; i++) {
                JSONArray r = a.getJSONArray(i);
                nm[i] = r.getString(0);
                ur[i] = r.getString(1);
            }
            names = nm;
            urls = ur;
        } catch (Throwable e) {
            names = new String[0];
            urls = new String[0];
        }
        curUrl = current == null ? "" : current;
    }

    private final Activity act;
    private final Pick pick;
    private final LinearLayout box;
    private final ListView list;
    private final TextView head;
    private final Handler h = new Handler(Looper.getMainLooper());
    private final Runnable hide = new Runnable() {
        @Override
        public void run() {
            close();
        }
    };
    private final BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return names.length;
        }

        @Override
        public Object getItem(int i) {
            return names[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View v, ViewGroup parent) {
            TextView t = v instanceof TextView ? (TextView) v : new TextView(act);
            t.setText((i + 1) + "   " + names[i]);
            t.setTextColor(Color.WHITE);
            t.setTextSize(18);
            t.setSingleLine(true);
            t.setEllipsize(android.text.TextUtils.TruncateAt.END);
            t.setPadding(28, 22, 20, 22);
            boolean on = urls[i].equals(curUrl);
            t.setTypeface(null, on ? Typeface.BOLD : Typeface.NORMAL);
            t.setTextColor(on ? Color.parseColor("#FFD84A") : Color.WHITE);
            return t;
        }
    };

    ChannelPanel(Activity a, FrameLayout root, Pick p) {
        act = a;
        pick = p;
        DisplayMetrics dm = a.getResources().getDisplayMetrics();
        int w = Math.min((int) (dm.widthPixels * 0.40f), (int) (420 * dm.density));
        box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.parseColor("#EE0C0824"));
        box.setVisibility(View.GONE);
        head = new TextView(a);
        head.setTextColor(Color.parseColor("#19E3FF"));
        head.setTextSize(16);
        head.setTypeface(null, Typeface.BOLD);
        head.setPadding(28, 22, 20, 14);
        box.addView(head, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        list = new ListView(a);
        list.setAdapter(adapter);
        list.setDivider(null);
        list.setSelector(new android.graphics.drawable.ColorDrawable(Color.parseColor("#55A855F7")));
        list.setItemsCanFocus(false);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                if (i < 0 || i >= urls.length) return;
                curUrl = urls[i];
                adapter.notifyDataSetChanged();
                if (pick != null) pick.onPick(urls[i], names[i]);
                arm();
            }
        });
        box.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START);
        root.addView(box, lp);
    }

    static boolean available() {
        return names.length > 0;
    }

    boolean isOpen() {
        return box.getVisibility() == View.VISIBLE;
    }

    void open() {
        if (!available()) return;
        head.setText("Kanallar · " + names.length);
        adapter.notifyDataSetChanged();
        int idx = 0;
        for (int i = 0; i < urls.length; i++) {
            if (urls[i].equals(curUrl)) {
                idx = i;
                break;
            }
        }
        box.setVisibility(View.VISIBLE);
        list.requestFocus();
        list.setSelection(idx);
        arm();
    }

    void close() {
        h.removeCallbacks(hide);
        box.setVisibility(View.GONE);
        box.clearFocus();
    }

    private void arm() {
        h.removeCallbacks(hide);
        h.postDelayed(hide, 10000);
    }

    /** Tuş olayını işler; true = tüketildi. Oynatıcı etkinliğinin dispatchKeyEvent başında çağrılır. */
    boolean handleKey(KeyEvent e) {
        int k = e.getKeyCode();
        if (isOpen()) {
            if (k == KeyEvent.KEYCODE_BACK || k == KeyEvent.KEYCODE_DPAD_LEFT || k == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (e.getAction() == KeyEvent.ACTION_DOWN) close();
                return true;
            }
            arm();
            return false; // yukarı/aşağı/TAMAM listeye gider
        }
        if (k == KeyEvent.KEYCODE_DPAD_LEFT && available()) {
            if (e.getAction() == KeyEvent.ACTION_DOWN && e.getRepeatCount() == 0) open();
            return true;
        }
        return false;
    }
}
