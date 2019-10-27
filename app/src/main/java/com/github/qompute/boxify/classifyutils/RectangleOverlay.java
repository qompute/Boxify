package com.github.qompute.boxify.classifyutils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * A rectangular overlay that can be drawn on top of another view.
 */
public class RectangleOverlay extends View {
    private Paint paint;
    private Rect rect;

    public RectangleOverlay(Context context) {
        super(context);
        paint = new Paint();
    }

    public RectangleOverlay(Context context, AttributeSet attr) {
        super(context, attr);
        paint = new Paint();
    }

    public RectangleOverlay(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3.0f);

        if (rect != null) {
            canvas.drawRect(rect, paint);
        }
    }

    public void setRectangle(Rect newRect) {
        rect = newRect;
        invalidate();
    }
}
