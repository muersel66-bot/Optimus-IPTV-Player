package tr.optimus.iptv;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Rational;

/** Geri tuşunda tam ekran oynatıcıyı küçük (resim içinde resim) pencereye alır; Android TV'de sağ üst köşede açılır. */
final class Pip {
    private Pip() { }

    static boolean enabled(Context c) {
        try {
            return !"0".equals(c.getSharedPreferences("optimus_kv", Context.MODE_PRIVATE).getString("sinyal.pip", "1"));
        } catch (Throwable e) {
            return true;
        }
    }

    /** Küçük pencereye geçmeyi dener; başarılıysa true. */
    static boolean enter(Activity a) {
        try {
            if (Build.VERSION.SDK_INT < 26 || !enabled(a)) return false;
            if (!a.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) return false;
            PictureInPictureParams p = new PictureInPictureParams.Builder().setAspectRatio(new Rational(16, 9)).build();
            return a.enterPictureInPictureMode(p);
        } catch (Throwable e) {
            return false;
        }
    }
}
