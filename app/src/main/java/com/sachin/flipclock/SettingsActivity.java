package com.sachin.flipclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private LinearLayout box;
    private float dp;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        dp = getResources().getDisplayMetrics().density;
        ScrollView sv = new ScrollView(this);
        box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (16 * dp); box.setPadding(p, p, p, p);
        sv.addView(box); setContentView(sv);

        head("Look");
        choice("theme", "Theme", new String[]{"Classic light", "Dark", "AMOLED black", "Sepia", "Neon", "Custom colours"}, 0);
        color("c_bg", "Custom background", "#202020");
        color("c_card", "Custom card", "#F4F2EE");
        color("c_text", "Custom digits", "#3A3633");
        color("c_line", "Custom split line", "#000000");
        choice("font", "Font", new String[]{"Condensed bold", "Sans bold", "Serif bold", "Mono bold", "Black"}, 0);
        seek("radius", "Corner radius", 0, 40, 12, " dp");
        seek("gap", "Card spacing", 0, 24, 6, " dp");
        head("Clock");
        toggle("h24", "24-hour time", false);
        toggle("ampm", "Show AM/PM", true);
        toggle("secs", "Show seconds", false);
        toggle("day", "Show day name", true);
        toggle("date", "Show date", true);
        toggle("upper", "UPPERCASE names", true);
        toggle("batt", "Show battery", true);
        head("Flip");
        toggle("flip", "Flip animation", true);
        seek("speed", "Flip duration", 150, 1500, 600, " ms");
        head("Screen");
        toggle("keepon", "Keep screen on", true);
        choice("orient", "Orientation", new String[]{"Auto", "Landscape", "Portrait"}, 0);
        seek("bright", "Brightness (0 = system)", 0, 100, 0, " %");
        Button rs = new Button(this); rs.setText("Reset all");
        rs.setOnClickListener(v -> { Prefs.sp(this).edit().clear().apply(); recreate(); });
        box.addView(rs);
    }

    private void head(String s) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(13);
        t.setTextColor(Color.parseColor("#B05A00")); t.setPadding(0, (int) (20 * dp), 0, 0);
        box.addView(t);
    }
    private TextView row() {
        TextView t = new TextView(this); t.setTextSize(16);
        t.setPadding(0, (int) (12 * dp), 0, (int) (12 * dp)); box.addView(t); return t;
    }
    private void choice(String k, String label, String[] o, int d) {
        TextView t = row();
        Runnable up = () -> { int x = Prefs.i(this, k, d); if (x < 0 || x >= o.length) x = d; t.setText(label + ": " + o[x]); };
        up.run();
        t.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle(label)
                .setSingleChoiceItems(o, Prefs.i(this, k, d), (dg, w) -> {
                    Prefs.sp(this).edit().putInt(k, w).apply(); up.run(); dg.dismiss();
                }).show());
    }
    private void toggle(String k, String label, boolean d) {
        Switch s = new Switch(this); s.setText(label); s.setTextSize(16);
        s.setPadding(0, (int) (12 * dp), 0, (int) (12 * dp));
        s.setChecked(Prefs.b(this, k, d));
        s.setOnCheckedChangeListener((x, c) -> Prefs.sp(this).edit().putBoolean(k, c).apply());
        box.addView(s);
    }
    private void seek(String k, String label, int min, int max, int d, String u) {
        TextView t = row();
        int cur = Prefs.i(this, k, d);
        t.setText(label + ": " + cur + u);
        SeekBar s = new SeekBar(this); s.setMax(max - min); s.setProgress(cur - min);
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar sb, int p, boolean f) { t.setText(label + ": " + (p + min) + u); }
            public void onStartTrackingTouch(SeekBar sb) { }
            public void onStopTrackingTouch(SeekBar sb) {
                Prefs.sp(SettingsActivity.this).edit().putInt(k, sb.getProgress() + min).apply();
            }
        });
        box.addView(s);
    }
    private void color(String k, String label, String d) {
        TextView t = row();
        Runnable up = () -> t.setText(label + ": " + Prefs.s(this, k, d));
        up.run();
        t.setOnClickListener(v -> {
            EditText e = new EditText(this); e.setText(Prefs.s(this, k, d));
            new AlertDialog.Builder(this).setTitle(label + " (#RRGGBB)").setView(e)
                    .setPositiveButton("OK", (dg, w) -> {
                        String x = e.getText().toString().trim();
                        try { Color.parseColor(x); Prefs.sp(this).edit().putString(k, x).apply(); up.run(); }
                        catch (Exception ex) { Toast.makeText(this, "Invalid colour", Toast.LENGTH_SHORT).show(); }
                    }).setNegativeButton("Cancel", null).show();
        });
    }
}
