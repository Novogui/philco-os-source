package com.philco.ultra777.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;

/** Single-photo backdrop: RGB_565 decoding, one image in memory, centered 16:9 crop. */
final class AuroraBackdropView extends View {
    static final int AURORA = 0, DAWN = 1, SUNSET = 2, NIGHT = 3, FOREST = 14;
    private static final String TAG = "PhilcoUltra777";
    private static final int[] WALLPAPERS = {
            R.drawable.wallpaper_01, R.drawable.wallpaper_02, R.drawable.wallpaper_03,
            R.drawable.wallpaper_04, R.drawable.wallpaper_05, R.drawable.wallpaper_06,
            R.drawable.wallpaper_07, R.drawable.wallpaper_08, R.drawable.wallpaper_09,
            R.drawable.wallpaper_10, R.drawable.wallpaper_11, R.drawable.wallpaper_12,
            R.drawable.wallpaper_13, R.drawable.wallpaper_14, R.drawable.wallpaper_15
    };

    private final Paint photoPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private Shader fallbackShader, topVignetteShader, bottomVignetteShader;
    private float shaderWidth = -1, shaderHeight = -1;
    private int scene = -1;
    private Bitmap wallpaper;

    AuroraBackdropView(Context context) {
        super(context);
    }

    void setScene(int value) {
        int next = Math.max(AURORA, Math.min(FOREST, value));
        if (scene == next && wallpaper != null && !wallpaper.isRecycled()) return;
        scene = next;
        loadWallpaper();
        invalidate();
    }

    int getScene() { return scene < 0 ? AURORA : scene; }

    private void loadWallpaper() {
        Bitmap next = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            options.inScaled = false;
            next = BitmapFactory.decodeResource(getResources(), WALLPAPERS[getScene()], options);
        } catch (Throwable t) {
            Log.w(TAG, "Could not decode photo wallpaper " + getScene(), t);
        }
        Bitmap old = wallpaper;
        wallpaper = next;
        if (old != null && old != next && !old.isRecycled()) old.recycle();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (wallpaper == null || wallpaper.isRecycled()) loadWallpaper();
    }

    @Override protected void onDetachedFromWindow() {
        Bitmap old = wallpaper;
        wallpaper = null;
        if (old != null && !old.isRecycled()) old.recycle();
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth(), height = getHeight();
        if (width <= 0 || height <= 0) return;
        ensureShaders(width, height);
        Bitmap image = wallpaper;
        if (image != null && !image.isRecycled()) {
            drawCenterCrop(canvas, image, width, height);
        } else {
            overlayPaint.setShader(fallbackShader);
            canvas.drawRect(0, 0, width, height, overlayPaint);
            overlayPaint.setShader(null);
        }

        // A restrained full-screen scrim preserves contrast over bright photographs.
        overlayPaint.setColor(0x26040A14);
        canvas.drawRect(0, 0, width, height, overlayPaint);
        drawVignette(canvas, width, height);
    }

    private void drawCenterCrop(Canvas canvas, Bitmap bitmap, float width, float height) {
        float imageWidth = bitmap.getWidth(), imageHeight = bitmap.getHeight();
        float viewRatio = width / height, imageRatio = imageWidth / imageHeight;
        float left = 0, top = 0, right = imageWidth, bottom = imageHeight;
        if (imageRatio > viewRatio) {
            float cropWidth = imageHeight * viewRatio;
            left = (imageWidth - cropWidth) * 0.5f;
            right = left + cropWidth;
        } else if (imageRatio < viewRatio) {
            float cropHeight = imageWidth / viewRatio;
            top = (imageHeight - cropHeight) * 0.5f;
            bottom = top + cropHeight;
        }
        source.set((int)left, (int)top, Math.min(bitmap.getWidth(), (int)Math.ceil(right)),
                Math.min(bitmap.getHeight(), (int)Math.ceil(bottom)));
        destination.set(0, 0, width, height);
        canvas.drawBitmap(bitmap, source, destination, photoPaint);
    }

    private void drawVignette(Canvas canvas, float width, float height) {
        overlayPaint.setShader(topVignetteShader);
        canvas.drawRect(0, 0, width, height * .46f, overlayPaint);
        overlayPaint.setShader(bottomVignetteShader);
        canvas.drawRect(0, height * .58f, width, height, overlayPaint);
        overlayPaint.setShader(null);
    }

    private void ensureShaders(float width, float height) {
        if (width == shaderWidth && height == shaderHeight) return;
        shaderWidth = width;
        shaderHeight = height;
        fallbackShader = new LinearGradient(0, 0, 0, height,
                new int[]{0xFF132038, 0xFF26354B, 0xFF101827}, null, Shader.TileMode.CLAMP);
        topVignetteShader = new LinearGradient(0, 0, 0, height * .46f,
                new int[]{0x99030A18, 0x00030A18}, null, Shader.TileMode.CLAMP);
        bottomVignetteShader = new LinearGradient(0, height * .58f, 0, height,
                new int[]{0x00030A18, 0xB3030A18}, null, Shader.TileMode.CLAMP);
    }
}
