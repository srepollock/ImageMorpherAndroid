package ca.spollock.morphing;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.*;

public class FirstCanvasView extends View {
    private LineController lc;
    private SecondCanvasView secondCanvas;
    private final Paint mPaint;
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private Line tempL;
    private int idx = 0;
    private boolean drawingMode = true;
    private Point lastTouch;
    private int closestIndex = -1;
    private Paint arcPaint;

    public FirstCanvasView(Context context){
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.RED);
        arcPaint.setColor(Color.BLUE);
    }

    public FirstCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.RED);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(lc.firstCanvas != null) {
            for (Line l : lc.firstCanvas) {
                canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPaint);
            }
        }
        if(!drawingMode){
            if(closestIndex != -1){
                // if in edit, draw a line around the index we found
                canvas.drawCircle(lc.firstCanvas.get(closestIndex).startX,
                        lc.firstCanvas.get(closestIndex).startY, 2, arcPaint);
                canvas.drawCircle(lc.firstCanvas.get(closestIndex).endX,
                        lc.firstCanvas.get(closestIndex).endY, 2, arcPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(drawingMode) {
            // drawing mode
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Line tempLine = new Line(event.getX(), event.getY());
                    lc.addLine(tempLine);
                    invalidate();
                    secondCanvas.invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    lc.addX(idx, event.getX());
                    lc.addY(idx, event.getY());
                    invalidate();
                    secondCanvas.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    lc.addX(idx, event.getX());
                    lc.addY(idx, event.getY());
                    idx++;
                    invalidate();
                    secondCanvas.invalidate();
                    secondCanvas.updateIndex();
                    lastTouch = new Point((int)event.getX(), (int)event.getY());
                    break;
            }
        }else{
            // edit mode

            // get the closest line based on the touch of the screen
                // should this also be last touch point?

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    // run function to find the closest point based on that object
                    findClosestLine();
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // edit the point to have the new points from the touch event
                    if(closestIndex != -1){
                        // we found one
                        lc.firstCanvas.get(closestIndex).startX = event.getX();
                        lc.firstCanvas.get(closestIndex).startY = event.getY();
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    if(closestIndex != -1){
                        lc.firstCanvas.get(closestIndex).startX = event.getX();
                        lc.firstCanvas.get(closestIndex).startY = event.getY();
                    }
                    lastTouch = new Point((int)event.getX(), (int)event.getY());
                    break;
            }
        }
        return true;
    }

    public void init(LineController controller, SecondCanvasView sView){
        lc = controller;
        secondCanvas = sView;
    }

    public void updateIndex() { idx++; }

    public void indexZero(){
        idx = 0;
    }

    public void removed(){
        idx--;
    }

    public void changeMode(boolean mode){
        drawingMode = mode;
    }

    private void findClosestLine(){
        boolean start;
        int tempDex = 0;
        int closestDistance = Integer.MAX_VALUE;
        // needs to loop through the array list, for both startX,Y and endX,Y of each line in the array
        // then needs to get the index to that point and draw a circle around that point
            // also change the colour of the line and the corresponding line based on that index

        // runs through all and finds the absolute closest
        for(Line l : lc.firstCanvas){
            // loops through the entire list
            int temp = checkPoint(l);
            if(temp < closestDistance && temp != -1){
                closestIndex = tempDex;
            }
            tempDex++;
        }
    }
    // returns the distance based on the index
    private int checkPoint(Line l){
        boolean close = false;
        int maxDistance = 100;
        double firstDistance = (Math.pow((double)(lastTouch.x - l.startX), (double)2) +
                (Math.pow((double)(lastTouch.y - l.startY), (double)2)));
        double secondDistance = (Math.pow((double)(lastTouch.x - l.endX), (double)2) +
                (Math.pow((double)(lastTouch.y - l.endY), (double)2)));

        if(maxDistance < firstDistance) {
            return (int)firstDistance;
        }else if(maxDistance < secondDistance){
            return (int)secondDistance;
        }
        return -1;
    }
}