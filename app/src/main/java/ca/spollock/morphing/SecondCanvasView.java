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
    private FirstCanvasView first;
    public ArrayList<Line> lines;
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
        for(Line l : lines) {
            canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Line tempLine = new Line(event.getX(), event.getY());
                lines.add(tempLine);
                first.lines.add(tempLine);
                invalidate();
                first.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                lines.get(idx).endX = event.getX();
                lines.get(idx).endY = event.getY();
                first.lines.get(idx).endX = event.getX();
                first.lines.get(idx).endY = event.getY();
                invalidate();
                first.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                lines.get(idx).endX = event.getX();
                lines.get(idx).endY = event.getY();
                first.lines.get(idx).endX = event.getX();
                first.lines.get(idx).endY = event.getY();
                idx++;
                invalidate();
                first.invalidate();
                break;
        }
        return true;
    }

    public void init(){
        lines = new ArrayList<>();
    }

    public void setFirst(FirstCanvasView sView){
        first = sView;
    }

    public void clearList(){
        lines.clear();
        idx = 0;
        invalidate();
    }
}