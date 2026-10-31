package com.sachin.flipclock;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler hd = new Handler(Looper.getMainLooper());
    private FlipCard day, d1, d2, d3, d4, s1, s2, dd, mm, yy;
    private TextView batt;
    private boolean dirty, h24, ampm, upper, flip;
    private int[] th; private Typeface tf; private float rad, gap; private long dur;

    private final Runnable tick = new Runnable() {
        public void run() { update(); hd.postDelayed(this, 1000 - System.currentTimeMillis() % 1000 + 10); }
    };
    private final Runnable rb = () -> { if (dirty) { dirty = false; build(); } };
    private final SharedPreferences.OnSharedPreferenceChangeListener lis = (s, k) -> {
        dirty = true; hd.removeCallbacks(rb); hd.postDelayed(rb, 250);
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Prefs.sp(this).registerOnSharedPreferenceChangeListener(lis);
        build();
    }
    @Override protected void onResume() { super.onResume(); hd.removeCallbacks(tick); hd.post(tick); }
    @Override protected void onPause() { super.onPause(); hd.removeCallbacks(tick); }
    @Override protected void onDestroy() {
        super.onDestroy();
        Prefs.sp(this).unregisterOnSharedPreferenceChangeListener(lis);
        hd.removeCallbacksAndMessages(null);
    }
    @Override public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private Typeface font(int f) {
        switch (f) {
            case 1: return Typeface.create("sans-serif", Typeface.BOLD);
            case 2: return Typeface.create("serif", Typeface.BOLD);
            case 3: return Typeface.create("monospace", Typeface.BOLD);
            case 4: return Typeface.create("sans-serif-black", Typeface.NORMAL);
            default: return Typeface.create("sans-serif-condensed", Typeface.BOLD);
        }
    }
    private FlipCard mk() { return new FlipCard(this, th[1], th[2], th[3], tf, rad, gap, dur); }
    private LinearLayout box(int o) { LinearLayout l = new LinearLayout(this); l.setOrientation(o); return l; }
    private LinearLayout.LayoutParams lp(float w) { return new LinearLayout.LayoutParams(-1, 0, w); }
    private LinearLayout.LayoutParams wp(float w) { return new LinearLayout.LayoutParams(0, -1, w); }

    private void build() {
        float dp = getResources().getDisplayMetrics().density;
        th = Prefs.theme(this);
        tf = font(Prefs.i(this, "font", 0));
        rad = Prefs.i(this, "radius", 12) * dp;
        gap = Prefs.i(this, "gap", 6) * dp;
        dur = Prefs.i(this, "speed", 600);
        h24 = Prefs.b(this, "h24", false);
        ampm = Prefs.b(this, "ampm", true);
        upper = Prefs.b(this, "upper", true);
        flip = Prefs.b(this, "flip", true);
        boolean secs = Prefs.b(this, "secs", false), showDay = Prefs.b(this, "day", true),
                showDate = Prefs.b(this, "date", true), showBatt = Prefs.b(this, "batt", true);
        int o = Prefs.i(this, "orient", 0);
        setRequestedOrientation(o == 1 ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                : o == 2 ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                : ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        if (Prefs.b(this, "keepon", true)) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams wl = getWindow().getAttributes();
        int br = Prefs.i(this, "bright", 0);
        wl.screenBrightness = br <= 0 ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE : Math.max(0.01f, br / 100f);
        getWindow().setAttributes(wl);

        boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        day = s1 = s2 = dd = mm = yy = null; batt = null;
        LinearLayout root = box(LinearLayout.VERTICAL);
        root.setBackgroundColor(th[0]);
        int pd = (int) gap; root.setPadding(pd, pd, pd, pd);
        if (showBatt) {
            batt = new TextView(this); batt.setTextColor(th[2]); batt.setTextSize(14);
            batt.setGravity(Gravity.END); batt.setPadding(0, 0, (int) (8 * dp), 0);
            root.addView(batt, new LinearLayout.LayoutParams(-1, -2));
        }
        if (showDay) { day = mk(); root.addView(day, lp(1.1f)); }
        d1 = mk(); d2 = mk(); d3 = mk(); d4 = mk();
        if (land) {
            LinearLayout r = box(LinearLayout.HORIZONTAL);
            r.addView(d1, wp(1)); r.addView(d2, wp(1)); r.addView(new View(this), wp(0.15f));
            r.addView(d3, wp(1)); r.addView(d4, wp(1));
            root.addView(r, lp(4f));
        } else {
            LinearLayout c = box(LinearLayout.VERTICAL), a = box(LinearLayout.HORIZONTAL), z = box(LinearLayout.HORIZONTAL);
            a.addView(d1, wp(1)); a.addView(d2, wp(1)); z.addView(d3, wp(1)); z.addView(d4, wp(1));
            c.addView(a, lp(1)); c.addView(z, lp(1));
            root.addView(c, lp(5f));
        }
        if (secs) {
            s1 = mk(); s2 = mk();
            LinearLayout r = box(LinearLayout.HORIZONTAL);
            r.addView(new View(this), wp(1.5f)); r.addView(s1, wp(1)); r.addView(s2, wp(1)); r.addView(new View(this), wp(1.5f));
            root.addView(r, lp(1.3f));
        }
        if (showDate) {
            dd = mk(); mm = mk(); yy = mk();
            LinearLayout r = box(LinearLayout.HORIZONTAL);
            r.addView(dd, wp(1)); r.addView(mm, wp(2.2f)); r.addView(yy, wp(1.6f));
            root.addView(r, lp(1.1f));
        }
        setContentView(root);
        root.setLongClickable(true);
        root.setOnLongClickListener(v -> { startActivity(new Intent(this, SettingsActivity.class)); return true; });
        update();
    }

    private String two(int v) { return String.format(Locale.US, "%02d", v); }

    private void update() {
        if (d1 == null) return;
        Date n = new Date();
        Calendar c = Calendar.getInstance();
        int H = c.get(Calendar.HOUR_OF_DAY);
        int hh = h24 ? H : (H % 12 == 0 ? 12 : H % 12);
        String hs = two(hh), ms = two(c.get(Calendar.MINUTE));
        d1.set(hs.substring(0, 1), flip); d2.set(hs.substring(1), flip);
        d3.set(ms.substring(0, 1), flip); d4.set(ms.substring(1), flip);
        d1.setCorner(ampm && !h24 ? (H < 12 ? "AM" : "PM") : "");
        if (s1 != null) { String ss = two(c.get(Calendar.SECOND)); s1.set(ss.substring(0, 1), flip); s2.set(ss.substring(1), flip); }
        Locale l = Locale.getDefault();
        if (day != null) { String t = new SimpleDateFormat("EEEE", l).format(n); day.set(upper ? t.toUpperCase(l) : t, flip); }
        if (dd != null) {
            String t = new SimpleDateFormat("MMMM", l).format(n);
            dd.set(two(c.get(Calendar.DAY_OF_MONTH)), flip);
            mm.set(upper ? t.toUpperCase(l) : t, flip);
            yy.set(String.valueOf(c.get(Calendar.YEAR)), flip);
        }
        if (batt != null) {
            Intent bi = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (bi != null) {
                int lv = bi.getIntExtra("level", 0), sc = Math.max(1, bi.getIntExtra("scale", 100)), st = bi.getIntExtra("status", 0);
                batt.setText((st == 2 || st == 5 ? "\u26A1 " : "") + (lv * 100 / sc) + "%");
            }
        }
    }
}
