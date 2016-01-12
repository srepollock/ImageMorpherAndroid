package ca.spollock.morphing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileInputStream;
import java.util.ArrayList;

public class SecondCanvasView extends View {
    private LineController lc;
    private FirstCanvasView firstCanvas;
    private final Paint mPaint;
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private Line tempL;
    private int idx = 0;

    public SecondCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.RED);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(Line l : lc.secondCanvas) {
            canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPaint);
        }
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Line tempLine = new Line(event.getX(), event.getY());
                lc.addLine(tempLine);
                invalidate();
                firstCanvas.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                lc.addX(idx, event.getX());
                lc.addY(idx, event.getY());
                invalidate();
                firstCanvas.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                lc.addX(idx, event.getX());
                lc.addY(idx, event.getY());
                idx++;
                invalidate();
                firstCanvas.invalidate();
                break;
        }
        return true;
    }

    public void init(LineController controller, FirstCanvasView sView){
        lc = controller;
        firstCanvas = sView;
    }

    public void indexZero(){
        idx = 0;
    }

    public void removed(){
        idx--;
    }
}