package com.sachin.flipclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class AutomationReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        SharedPreferences.Editor e = Prefs.sp(c).edit();
        if ("com.sachin.flipclock.RESET".equals(i.getAction())) {
            e.clear();
        } else {
            Bundle x = i.getExtras();
            if (x != null) {
                String key = String.valueOf(x.get("key"));
                if (x.containsKey("key") && x.containsKey("value")) put(e, key, x.get("value"));
                for (String k : x.keySet()) if (!k.equals("key") && !k.equals("value")) put(e, k, x.get(k));
            }
        }
        e.apply();
    }
    private void put(SharedPreferences.Editor e, String k, Object v) {
        if (v == null) return;
        if (v instanceof Boolean) { e.putBoolean(k, (Boolean) v); return; }
        if (v instanceof Integer) { e.putInt(k, (Integer) v); return; }
        String s = v.toString().trim();
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) e.putBoolean(k, Boolean.parseBoolean(s));
        else if (s.matches("-?\\d+")) e.putInt(k, Integer.parseInt(s));
        else e.putString(k, s);
    }
}
