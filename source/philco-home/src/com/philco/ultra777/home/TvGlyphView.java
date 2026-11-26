package com.philco.ultra777.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Small vector-like glyphs drawn at display resolution without image allocations. */
final class TvGlyphView extends View {
    static final int PLAY=0, ANTENNA=1, RECEIVER=2, RCA=3, HDMI1=4, HDMI2=5,
            APPS=6, SETTINGS=7, WEATHER=8, WIFI=9, VOLUME=10, SCENE=11,
            BRIGHTNESS=12, BACKLIGHT=13, STANDBY=14;
    private static final int[] RCA_COLORS={0xFFFFD34F,0xFFE95F5F,0xFF72C987};
    private static final int[] APP_COLORS={0xFFFF625E,0xFFFFC04D,0xFF55D69A,0xFF54B7F2,
            0xFFAA7AF6,0xFFF276BC,0xFF76D6D1,0xFFE6EAFF};
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Path path = new Path();
    // Unknown/offline is a neutral cloud, never a fabricated sunny forecast.
    private int type, color = 0xFFFFFFFF, level = 3, weatherCode = -1;
    private boolean muted;

    TvGlyphView(Context context, int type, int color) { super(context); this.type=type; this.color=color; }
    void setLevel(int level) { this.level=Math.max(0,Math.min(4,level)); invalidate(); }
    void setMuted(boolean muted) { this.muted=muted; invalidate(); }
    void setWeatherCode(int code) { weatherCode=code; invalidate(); }
    int getLevel() { return level; }
    boolean isMuted() { return muted; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w=getWidth(), h=getHeight(), s=Math.min(w,h), cx=w/2f, cy=h/2f;
        p.setColor(color); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.055f); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
        switch(type) {
            case PLAY: drawPlay(c,cx,cy,s); break;
            case ANTENNA: drawAntenna(c,cx,cy,s); break;
            case RECEIVER: drawReceiver(c,cx,cy,s); break;
            case RCA: drawRca(c,cx,cy,s); break;
            case HDMI1: drawHdmi(c,cx,cy,s,"1"); break;
            case HDMI2: drawHdmi(c,cx,cy,s,"2"); break;
            case APPS: drawApps(c,cx,cy,s); break;
            case SETTINGS: drawGear(c,cx,cy,s); break;
            case WEATHER: drawWeather(c,cx,cy,s); break;
            case WIFI: drawWifi(c,cx,cy,s); break;
            case VOLUME: drawVolume(c,cx,cy,s); break;
            case SCENE: drawScene(c,cx,cy,s); break;
            case BRIGHTNESS: drawBrightness(c,cx,cy,s); break;
            case BACKLIGHT: drawBacklight(c,cx,cy,s); break;
            case STANDBY: drawStandby(c,cx,cy,s); break;
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void drawPlay(Canvas c,float x,float y,float s) {
        path.rewind(); path.moveTo(x-s*.27f,y-s*.37f); path.lineTo(x+s*.39f,y); path.lineTo(x-s*.27f,y+s*.37f); path.close();
        p.setStyle(Paint.Style.FILL); c.drawPath(path,p); p.setStyle(Paint.Style.STROKE);
    }
    private void drawAntenna(Canvas c,float x,float y,float s) {
        c.drawLine(x,y-s*.18f,x,y+s*.4f,p); c.drawLine(x-s*.28f,y+s*.4f,x+s*.28f,y+s*.4f,p);
        r.set(x-s*.5f,y-s*.48f,x+s*.5f,y+s*.52f); c.drawArc(r,225,90,false,p);
        r.set(x-s*.34f,y-s*.31f,x+s*.34f,y+s*.37f); c.drawArc(r,225,90,false,p);
        p.setStyle(Paint.Style.FILL); c.drawCircle(x,y-s*.18f,s*.065f,p); p.setStyle(Paint.Style.STROKE);
    }
    private void drawReceiver(Canvas c,float x,float y,float s) {
        r.set(x-s*.42f,y-s*.15f,x+s*.42f,y+s*.34f); c.drawRoundRect(r,s*.06f,s*.06f,p);
        c.drawLine(x-s*.27f,y-s*.18f,x-s*.12f,y-s*.38f,p); c.drawLine(x+s*.27f,y-s*.18f,x+s*.12f,y-s*.38f,p);
        p.setStyle(Paint.Style.FILL); c.drawCircle(x+s*.25f,y+s*.1f,s*.045f,p); p.setStyle(Paint.Style.STROKE);
    }
    private void drawRca(Canvas c,float x,float y,float s) {
        // Keep the outer rings inside a small status/tile glyph viewport.
        for(int i=0;i<3;i++){float cx=x+(i-1)*s*.27f;p.setColor(RCA_COLORS[i]);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,y,s*.145f,p);p.setColor(0xFFFFFFFF);p.setStyle(Paint.Style.STROKE);c.drawCircle(cx,y,s*.17f,p);}
    }
    private void drawHdmi(Canvas c,float x,float y,float s,String n) {
        // HDMI jack silhouette with its port number above and a compact HDMI wordmark.
        p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(s*.30f);c.drawText(n,x,y-s*.12f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s*.045f);
        path.rewind();path.moveTo(x-s*.40f,y-s*.02f);path.lineTo(x+s*.40f,y-s*.02f);
        path.lineTo(x+s*.30f,y+s*.31f);path.lineTo(x-s*.30f,y+s*.31f);path.close();c.drawPath(path,p);
        p.setStyle(Paint.Style.FILL);p.setTextSize(s*.19f);c.drawText("HDMI",x,y+s*.22f,p);p.setStyle(Paint.Style.STROKE);
    }
    private void drawApps(Canvas c,float x,float y,float s) {
        int k=0;for(int row=-1;row<=1;row++)for(int col=-1;col<=1;col++)if(!(row==0&&col==0)){p.setColor(APP_COLORS[k++]);p.setStyle(Paint.Style.FILL);r.set(x+col*s*.22f-s*.075f,y+row*s*.22f-s*.075f,x+col*s*.22f+s*.075f,y+row*s*.22f+s*.075f);c.drawRoundRect(r,s*.035f,s*.035f,p);}
    }
    private void drawGear(Canvas c,float x,float y,float s) {
        path.rewind();for(int i=0;i<24;i++){double angle=Math.PI*2*i/24.0-Math.PI/2;float radius=s*((i%3)==0?.40f:.31f);float px=x+(float)Math.cos(angle)*radius,py=y+(float)Math.sin(angle)*radius;if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}path.close();c.drawPath(path,p);c.drawCircle(x,y,s*.13f,p);
    }
    private void drawWeather(Canvas c,float x,float y,float s) {
        boolean clear = weatherCode == 0;
        boolean fog = weatherCode == 45 || weatherCode == 48;
        boolean partlyCloudy = weatherCode == 1 || weatherCode == 2;
        boolean snow = (weatherCode >= 71 && weatherCode <= 77) || weatherCode == 85 || weatherCode == 86;
        boolean storm = weatherCode >= 95;
        boolean rain = (weatherCode >= 51 && weatherCode <= 67) ||
                (weatherCode >= 80 && weatherCode <= 82);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(s*.045f);
        if (clear || partlyCloudy) {
            float sunX=x-s*.17f, sunY=y-s*.17f;
            p.setColor(0xFFFFC857);
            for(int i=0;i<8;i++){double a=Math.PI*i/4;float dx=(float)Math.cos(a),dy=(float)Math.sin(a);
                c.drawLine(sunX+dx*s*.25f,sunY+dy*s*.25f,sunX+dx*s*.34f,sunY+dy*s*.34f,p);}
            p.setStyle(Paint.Style.FILL);c.drawCircle(sunX,sunY,s*.17f,p);
        }
        if (clear) return;
        if (fog) {
            p.setColor(0xFFB9D7E8);p.setStrokeWidth(s*.055f);
            for(int i=0;i<3;i++){float yy=y-s*.04f+i*s*.17f;
                c.drawLine(x-s*.34f,yy,x+s*.34f,yy,p);}
            return;
        }
        if (weatherCode < 0) {
            // Neutral cloud for unknown data; never imply a sunny forecast.
            drawCloud(c, x, y, s*.94f, 0xFFE6EDF5);
            return;
        }
        drawCloud(c, x+(partlyCloudy ? s*.09f : 0f), y+(partlyCloudy ? s*.10f : 0f),
                s*(partlyCloudy ? .78f : .94f), 0xFFF4F8FC);
        if (rain) {
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s*.045f);p.setColor(0xFF63CFFF);
            for(int i=0;i<3;i++){float dropX=x-s*.20f+i*s*.20f;
                c.drawLine(dropX,y+s*.26f,dropX-s*.055f,y+s*.39f,p);}
        } else if (snow) {
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s*.035f);p.setColor(0xFF9BE6FF);
            for(int i=0;i<3;i++){float sx=x-s*.19f+i*s*.19f, sy=y+s*.31f;
                c.drawLine(sx-s*.045f,sy-s*.045f,sx+s*.045f,sy+s*.045f,p);
                c.drawLine(sx-s*.045f,sy+s*.045f,sx+s*.045f,sy-s*.045f,p);}
        } else if (storm) {
            p.setStyle(Paint.Style.FILL);p.setColor(0xFFFFD166);
            path.rewind();path.moveTo(x+s*.02f,y+s*.22f);path.lineTo(x-s*.09f,y+s*.39f);
            path.lineTo(x+s*.01f,y+s*.39f);path.lineTo(x-s*.02f,y+s*.46f);
            path.lineTo(x+s*.15f,y+s*.32f);path.lineTo(x+s*.06f,y+s*.32f);path.close();c.drawPath(path,p);
        }
    }
    private void drawCloud(Canvas c,float x,float y,float s,int color) {
        p.setColor(color);p.setStyle(Paint.Style.FILL);
        path.rewind();
        path.moveTo(x-s*.37f,y+s*.18f);
        path.cubicTo(x-s*.47f,y+s*.15f,x-s*.48f,y-s*.01f,x-s*.36f,y-s*.08f);
        path.cubicTo(x-s*.31f,y-s*.23f,x-s*.12f,y-s*.24f,x-s*.03f,y-s*.10f);
        path.cubicTo(x+s*.07f,y-s*.31f,x+s*.37f,y-s*.25f,x+s*.39f,y-s*.02f);
        path.cubicTo(x+s*.48f,y+s*.01f,x+s*.46f,y+s*.20f,x+s*.34f,y+s*.21f);
        path.lineTo(x-s*.37f,y+s*.21f);path.close();c.drawPath(path,p);
    }
    private void drawWifi(Canvas c,float x,float y,float s) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(level >= 3 ? 0xFFF7FAFF : 0x667E91A6);
        path.rewind();
        path.moveTo(x-s*.46f,y-s*.07f);
        path.cubicTo(x-s*.22f,y-s*.34f,x+s*.22f,y-s*.34f,x+s*.46f,y-s*.07f);
        path.lineTo(x+s*.32f,y+s*.08f);
        path.cubicTo(x+s*.14f,y-s*.10f,x-s*.14f,y-s*.10f,x-s*.32f,y+s*.08f);
        path.close();c.drawPath(path,p);

        p.setColor(level >= 2 ? 0xFFF7FAFF : 0x667E91A6);
        path.rewind();path.moveTo(x-s*.29f,y+s*.12f);
        path.cubicTo(x-s*.13f,y-s*.06f,x+s*.13f,y-s*.06f,x+s*.29f,y+s*.12f);
        path.lineTo(x+s*.15f,y+s*.26f);
        path.cubicTo(x+s*.07f,y+s*.17f,x-s*.07f,y+s*.17f,x-s*.15f,y+s*.26f);
        path.close();c.drawPath(path,p);

        p.setColor(level > 0 ? 0xFFF7FAFF : 0x667E91A6);
        c.drawCircle(x,y+s*.36f,Math.max(1.5f,s*.075f),p);
        if(level==0){p.setColor(0xCCFFFFFF);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1.5f,s*.07f));
            c.drawLine(x-s*.37f,y-s*.28f,x+s*.37f,y+s*.28f,p);}
    }
    private void drawVolume(Canvas c,float x,float y,float s) {
        path.rewind();path.moveTo(x-s*.43f,y-s*.13f);path.lineTo(x-s*.23f,y-s*.13f);path.lineTo(x-s*.03f,y-s*.30f);path.lineTo(x-s*.03f,y+s*.30f);path.lineTo(x-s*.23f,y+s*.13f);path.lineTo(x-s*.43f,y+s*.13f);path.close();c.drawPath(path,p);
        p.setStrokeWidth(s*.065f);
        if (muted) {
            c.drawLine(x+s*.08f,y-s*.17f,x+s*.38f,y+s*.17f,p);
            c.drawLine(x+s*.38f,y-s*.17f,x+s*.08f,y+s*.17f,p);
            return;
        }
        if(level>0){r.set(x-s*.18f,y-s*.27f,x+s*.36f,y+s*.27f);c.drawArc(r,-58,116,false,p);}
        if(level>2){r.set(x-s*.12f,y-s*.40f,x+s*.46f,y+s*.40f);c.drawArc(r,-52,104,false,p);}
    }
    private void drawScene(Canvas c,float x,float y,float s) {
        r.set(x-s*.44f,y-s*.35f,x+s*.44f,y+s*.35f);c.drawRoundRect(r,s*.05f,s*.05f,p);
        path.rewind();path.moveTo(x-s*.4f,y+s*.22f);path.lineTo(x-s*.14f,y-s*.02f);path.lineTo(x+s*.02f,y+s*.1f);path.lineTo(x+s*.22f,y-s*.14f);path.lineTo(x+s*.4f,y+s*.22f);path.close();c.drawPath(path,p);
        p.setStyle(Paint.Style.FILL);c.drawCircle(x-s*.21f,y-s*.17f,s*.055f,p);
    }
    private void drawBrightness(Canvas c,float x,float y,float s) {
        c.drawCircle(x,y,s*.19f,p);
        for(int i=0;i<8;i++){double a=Math.PI*i/4;float r1=s*.31f,r2=s*.43f;
            c.drawLine(x+(float)Math.cos(a)*r1,y+(float)Math.sin(a)*r1,
                    x+(float)Math.cos(a)*r2,y+(float)Math.sin(a)*r2,p);}
    }
    private void drawBacklight(Canvas c,float x,float y,float s) {
        r.set(x-s*.37f,y-s*.29f,x+s*.37f,y+s*.29f);c.drawRoundRect(r,s*.07f,s*.07f,p);
        c.drawLine(x-s*.19f,y+s*.38f,x+s*.19f,y+s*.38f,p);
        c.drawLine(x,y+s*.29f,x,y+s*.38f,p);
        r.set(x-s*.23f,y-s*.15f,x+s*.23f,y+s*.15f);c.drawArc(r,-70,140,false,p);
    }
    private void drawStandby(Canvas c,float x,float y,float s) {
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s*.075f);
        r.set(x-s*.34f,y-s*.34f,x+s*.34f,y+s*.34f);c.drawArc(r,45,270,false,p);
        c.drawLine(x,y-s*.46f,x,y-s*.05f,p);
    }
}
