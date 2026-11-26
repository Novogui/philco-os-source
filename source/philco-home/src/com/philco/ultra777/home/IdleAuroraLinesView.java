package com.philco.ultra777.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Static, low-cost golden aurora strokes used only over the idle photo screensaver. */
final class IdleAuroraLinesView extends View {
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path upper = new Path();
    private final Path lower = new Path();

    IdleAuroraLinesView(Context context) {
        super(context);
        line.setColor(0xB8FFD166);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(getResources().getDisplayMetrics().density * 1.6f);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        upper.rewind();
        upper.moveTo(0f, h * .56f);
        upper.cubicTo(w * .20f, h * .76f, w * .37f, h * .91f, w * .56f, h * .82f);
        upper.cubicTo(w * .73f, h * .73f, w * .84f, h * .63f, w, h * .72f);
        lower.rewind();
        lower.moveTo(0f, h * .76f);
        lower.cubicTo(w * .23f, h * .84f, w * .39f, h * .99f, w * .58f, h * .93f);
        lower.cubicTo(w * .76f, h * .87f, w * .86f, h * .80f, w, h * .88f);
        canvas.drawPath(upper, line);
        line.setColor(0x72FFF0B3);
        canvas.drawPath(lower, line);
        line.setColor(0xB8FFD166);
    }
}
