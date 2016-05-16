package com.menupicture.menupicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wenjie on 5/15/16.
 */
public class HighlightView extends View {
    private Bitmap bitmap;
    // canvas is used to store all committed previous path.
    private Canvas canvas;
    private Path path;
    private Paint highlight_paint;
    private Paint bitmap_paint;

    // Path current point.
    private float x;
    private float y;
    private static final float TOUCH_TOLERANCE = 4;

    public HighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);

        path = new Path();
        highlight_paint = new Paint(Paint.DITHER_FLAG);
        highlight_paint.setAntiAlias(true);
        highlight_paint.setColor(Color.RED);
        highlight_paint.setStyle(Paint.Style.STROKE);
        highlight_paint.setStrokeJoin(Paint.Join.ROUND);
        highlight_paint.setStrokeCap(Paint.Cap.SQUARE);
        highlight_paint.setStrokeWidth(12);

        bitmap_paint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitmap, 0, 0, bitmap_paint);
        canvas.drawPath(path, highlight_paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;

    }

    private void touch_start(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x-this.x);
        float dy = Math.abs(y-this.y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            path.quadTo(this.x, this.y, (x+this.x)/2, (y+this.y)/2);
            this.x = x;
            this.y = y;
        }
    }

    private void touch_up() {
        path.lineTo(this.x, this.y);
        canvas.drawPath(path, highlight_paint);
        path.reset();
    }
}
