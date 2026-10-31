package com.sachin.flipclock;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

public class FlipCard extends View {
    private String cur = "", prev = "", corner = "";
    private float p = 1f;
    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG), tx = new Paint(Paint.ANTI_ALIAS_FLAG),
            sm = new Paint(Paint.ANTI_ALIAS_FLAG), ln = new Paint(), sh = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float rad, gap;
    private final long dur;
    private final RectF r = new RectF();
    private final Rect b = new Rect();
    private ValueAnimator anim;

    public FlipCard(Context c, int card, int text, int line, Typeface tf, float rad, float gap, long dur) {
        super(c);
        this.rad = rad; this.gap = gap; this.dur = dur;
        bg.setColor(card);
        tx.setColor(text); tx.setTypeface(tf); tx.setTextAlign(Paint.Align.CENTER); tx.setLetterSpacing(0.04f);
        sm.setColor(text); sm.setTypeface(tf);
        ln.setColor(line);
    }

    public void setCorner(String s) {
        if (!s.equals(corner)) { corner = s; invalidate(); }
    }

    public void set(String t, boolean animate) {
        if (t.equals(cur)) return;
        if (anim != null) anim.cancel();
        if (!animate || cur.isEmpty()) { cur = t; p = 1f; invalidate(); return; }
        prev = cur; cur = t; p = 0f;
        anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(dur);
        anim.addUpdateListener(a -> { p = (Float) a.getAnimatedValue(); invalidate(); });
        anim.start();
    }

    @Override protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight(), m = gap / 2f;
        r.set(m, m, w - m, h - m);
        if (p >= 1f) {
            half(cv, cur, true, 1f, false);
            half(cv, cur, false, 1f, false);
        } else {
            half(cv, cur, true, 1f, false);
            half(cv, prev, false, 1f, false);
            if (p < 0.5f) half(cv, prev, true, 1f - 2f * p, true);
            else half(cv, cur, false, 2f * p - 1f, true);
        }
        float lw = Math.max(2f, h * 0.006f), cy = h / 2f;
        cv.drawRect(r.left, cy - lw / 2f, r.right, cy + lw / 2f, ln);
        if (!corner.isEmpty()) {
            sm.setTextSize(Math.min(r.height() * 0.1f, r.width() * 0.25f));
            cv.drawText(corner, r.left + r.width() * 0.06f, r.bottom - r.height() * 0.06f, sm);
        }
    }

    private void half(Canvas cv, String t, boolean top, float sy, boolean shade) {
        if (sy <= 0.001f) return;
        float w = getWidth(), h = getHeight(), cy = h / 2f;
        cv.save();
        cv.translate(0, cy); cv.scale(1f, sy); cv.translate(0, -cy);
        if (top) cv.clipRect(0f, 0f, w, cy); else cv.clipRect(0f, cy, w, h);
        cv.drawRoundRect(r, rad, rad, bg);
        drawText(cv, t);
        if (shade) {
            sh.setColor(((int) (110 * (1f - sy)) << 24));
            cv.drawRoundRect(r, rad, rad, sh);
        }
        cv.restore();
    }

    private void drawText(Canvas cv, String t) {
        if (t.isEmpty()) return;
        float size = r.height() * 0.95f;
        tx.setTextSize(size);
        float tw = tx.measureText(t), max = r.width() * 0.9f;
        if (tw > max) { size *= max / tw; tx.setTextSize(size); }
        tx.getTextBounds("0", 0, 1, b);
        cv.drawText(t, r.centerX(), r.centerY() - b.exactCenterY(), tx);
    }
}
