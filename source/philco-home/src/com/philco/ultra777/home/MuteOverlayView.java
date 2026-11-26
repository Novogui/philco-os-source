package com.philco.ultra777.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Lightweight, static TV-style mute OSD shown while media output is silent. */
final class MuteOverlayView extends View {
    private final float density;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path speaker = new Path();
    private final RectF panel = new RectF();

    MuteOverlayView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        setFocusable(false);
        setClickable(false);
        setWillNotDraw(false);
    }

    private float dp(float value) { return value * density; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();

        panel.set(dp(1), dp(1), width - dp(1), height - dp(1));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xE8121C2A);
        canvas.drawRoundRect(panel, dp(21), dp(21), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(0x8877D8E7);
        canvas.drawRoundRect(panel, dp(21), dp(21), paint);

        // The solid speaker carries more visual weight than the thin x, so shift
        // the combined silhouette slightly left for optical centering.
        float iconX = width * .5f - dp(2);
        float iconY = height * .5f;
        float size = dp(52);
        speaker.rewind();
        speaker.moveTo(iconX - size * .42f, iconY - size * .15f);
        speaker.lineTo(iconX - size * .20f, iconY - size * .15f);
        speaker.lineTo(iconX + size * .04f, iconY - size * .35f);
        speaker.lineTo(iconX + size * .04f, iconY + size * .35f);
        speaker.lineTo(iconX - size * .20f, iconY + size * .15f);
        speaker.lineTo(iconX - size * .42f, iconY + size * .15f);
        speaker.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFF5FAFF);
        canvas.drawPath(speaker, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3.2f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(0xFF70DCD2);
        float markX = iconX + size * .18f;
        float markY = iconY;
        canvas.drawLine(markX, markY - size * .18f,
                markX + size * .31f, markY + size * .18f, paint);
        canvas.drawLine(markX + size * .31f, markY - size * .18f,
                markX, markY + size * .18f, paint);
    }
}
