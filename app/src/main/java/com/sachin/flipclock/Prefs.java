package com.sachin.flipclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class Prefs {
    // bg, card, digits, split line
    static final int[][] T = {
        {0xFFE9E9E9, 0xFFF4F2EE, 0xFF3A3633, 0xFF8A8A8A},
        {0xFF101010, 0xFF1E1E1E, 0xFFEDEDED, 0xFF000000},
        {0xFF000000, 0xFF0A0A0A, 0xFFFFFFFF, 0xFF000000},
        {0xFF3B2F25, 0xFFE8DCC2, 0xFF4A3626, 0xFF7A6A55},
        {0xFF05080F, 0xFF0D1624, 0xFF00E5FF, 0xFF000000}
    };
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("flip", 0); }
    static Object o(Context c, String k) { return sp(c).getAll().get(k); }
    static int i(Context c, String k, int d) {
        Object o = o(c, k);
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Boolean) return ((Boolean) o) ? 1 : 0;
        if (o instanceof String) { try { return Integer.parseInt(((String) o).trim()); } catch (Exception e) { } }
        return d;
    }
    static boolean b(Context c, String k, boolean d) {
        Object o = o(c, k);
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Integer) return ((Integer) o) != 0;
        if (o instanceof String) return ((String) o).equalsIgnoreCase("true") || o.equals("1");
        return d;
    }
    static String s(Context c, String k, String d) {
        Object o = o(c, k);
        return o instanceof String ? (String) o : d;
    }
    static int col(Context c, String k, String d) {
        try { return Color.parseColor(s(c, k, d)); } catch (Exception e) { return Color.parseColor(d); }
    }
    static int[] theme(Context c) {
        int t = i(c, "theme", 0);
        if (t >= 0 && t < T.length) return T[t];
        return new int[]{col(c, "c_bg", "#202020"), col(c, "c_card", "#F4F2EE"),
                col(c, "c_text", "#3A3633"), col(c, "c_line", "#000000")};
    }
}
