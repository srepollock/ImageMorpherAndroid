package ca.spollock.morphing;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.*;
import java.util.ArrayList;

public class FirstCanvasView extends View {
    private SecondCanvasView second;
    public ArrayList<Line> lines;
    private final Paint mPaint;
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private Line tempL;
    private int idx = 0;

    public FirstCanvasView(Context context, AttributeSet attrs) {
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
                second.lines.add(tempLine);
                invalidate();
                second.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                lines.get(idx).endX = event.getX();
                lines.get(idx).endY = event.getY();
                second.lines.get(idx).endX = event.getX();
                second.lines.get(idx).endY = event.getY();
                invalidate();
                second.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                lines.get(idx).endX = event.getX();
                lines.get(idx).endY = event.getY();
                second.lines.get(idx).endX = event.getX();
                second.lines.get(idx).endY = event.getY();
                idx++;
                invalidate();
                second.invalidate();
                break;
        }
        return true;
    }

    public void init(SecondCanvasView sView){
        lines = new ArrayList<>();
        second = sView;
    }

    public void clearList(){
        lines.clear();
        idx = 0;
        invalidate();
    }
}