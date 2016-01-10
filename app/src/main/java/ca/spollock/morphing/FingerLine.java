package ca.spollock.morphing;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.*;

import java.util.ArrayList;

public class FingerLine extends View {
    private ArrayList<Line> lines = new ArrayList<Line>();
    private final Paint mPaint;
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private Line tempL;
    private int idx = 0;

    private boolean firstCanContext = false;

    public FingerLine(Context context, AttributeSet attrs) {
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
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lines.add(new Line(event.getX(), event.getY()));
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                lines.get(idx).endX = event.getX();
                lines.get(idx).endY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP: // when the user lifts up
                lines.get(idx).endX = event.getX();
                lines.get(idx).endY = event.getY();
                idx++;
                invalidate();
                break;
        }
        return true;
    }

    public void clearList(){
        lines.clear();
        idx = 0;
        invalidate();
    }

    public void removeLineNumber(int indexR){
        lines.remove(indexR);
        idx--;
        invalidate();
    }
}