package ca.spollock.morphing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class EditingView extends View {
    private LineController lc;
    private final Paint mPaint;
    private boolean drawingMode = true;
    private int closestIndex = -1;
    private Paint editDot;
    private Paint editLine;
    private boolean endOfLine;
    private boolean noLine = true;
    private Point lastTouch;
    private final static int MAX_DISTANCE = 50;
    private Line editingLine = null;
    private int viewIndex;

    public EditingView(Context context){
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        editDot = new Paint(Paint.ANTI_ALIAS_FLAG);
        editLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.RED);
        editDot.setColor(Color.BLUE);
        editLine.setStrokeWidth(5);
        editLine.setColor(Color.CYAN);
    }

    public void init(LineController controller){
        lc = controller;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int counter = 0;
        if(viewIndex == 0){ // first View
            for (Line l : lc.firstCanvas) {
                if(closestIndex == -1) {
                    canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPaint);
                }else {
                    if (closestIndex == counter && !drawingMode) {
                        canvas.drawLine(l.startX, l.startY, l.endX, l.endY, editLine);
                    }else{
                        canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPaint);
                    }
                }
                counter++;
            }
            if(!drawingMode){
                // if in edit, draw a line around the index we found
                if(closestIndex != -1) {
                    canvas.drawCircle(lc.firstCanvas.get(closestIndex).startX,
                            lc.firstCanvas.get(closestIndex).startY, 20, editDot);
                    canvas.drawCircle(lc.firstCanvas.get(closestIndex).endX,
                            lc.firstCanvas.get(closestIndex).endY, 20, editDot);
                }
            }
        }else if(viewIndex == 1){
            for (Line l : lc.secondCanvas) {
                if(closestIndex == -1) {
                    canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPaint);
                }else {
                    if (closestIndex == counter && !drawingMode) {
                        canvas.drawLine(l.startX, l.startY, l.endX, l.endY, editLine);
                    }else{
                        canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPaint);
                    }
                }
                counter++;
            }
            if(!drawingMode){
                // if in edit, draw a line around the index we found
                if(closestIndex != -1) {
                    canvas.drawCircle(lc.secondCanvas.get(closestIndex).startX,
                            lc.secondCanvas.get(closestIndex).startY, 20, editDot);
                    canvas.drawCircle(lc.secondCanvas.get(closestIndex).endX,
                            lc.secondCanvas.get(closestIndex).endY, 20, editDot);
                }
            }
        }
    }

    public void drawLine(MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lc.addLine(event.getX(), event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                lc.addX(event.getX());
                lc.addY(event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                lc.addX(event.getX());
                lc.addY(event.getY());
                invalidate();
                break;
        }
    }
    public int editLine(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // run function to find the closest point based on that object
                lastTouch = new Point((int)event.getX(), (int)event.getY());
                editingLine = findClosestLine(); // this should be for any point on the screen
                if(editingLine == null){
                    noLine = true;
                    lastTouch = null;
                    closestIndex = -1;
                }else{
                    noLine = false;
                    endOfLine = checkPointStartEnd(editingLine);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                // edit the point to have the new points from the touch event
                if(!noLine){
                    // we found one
                    if(!endOfLine){
                        editingLine.startX = event.getX();
                        editingLine.startY = event.getY();
                    }else{
                        editingLine.endX = event.getX();
                        editingLine.endY = event.getY();
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!noLine){
                    // we found one
                    if(!endOfLine){
                        editingLine.startX = event.getX();
                        editingLine.startY = event.getY();
                    }else{
                        editingLine.endX = event.getX();
                        editingLine.endY = event.getY();
                    }
                    editingLine = null;
                    invalidate();
                }
                lastTouch = null;
                break;
        }
        return closestIndex;
    }

    public void editMode(int index){
        drawingMode = false;
        closestIndex = index;
    }

    public void clear() { closestIndex = -1; }

    public void drawingMode(){
        drawingMode = true;
    }

    public void viewIndex(int index){
        viewIndex = index;
    }

    // finds the closest line in either array (checks the screen right?)
    private Line findClosestLine(){
        int temp1, temp2;
        Line tempLine = null;
        int closestDistance = MAX_DISTANCE;
        // needs to loop through the array list, for both startX,Y and endX,Y of each line in the array
        // then needs to get the index to that point and draw a circle around that point
        // also change the colour of the line and the corresponding line based on that index

        if(viewIndex == 0){
            for(int i = 0; i < lc.firstCanvas.size(); i++){
                temp1 = checkPoint(lc.firstCanvas.get(i));
                if(temp1 < closestDistance && temp1 != -1) {
                    tempLine = lc.firstCanvas.get(i);
                    closestIndex = i;
                }
            }
        }else{
            for(int i = 0; i < lc.firstCanvas.size(); i++){
                temp2 = checkPoint(lc.secondCanvas.get(i));
                if(temp2 < closestDistance && temp2 != -1){
                    tempLine = lc.secondCanvas.get(i);
                    closestIndex = i;
                }
            }
        }
        return tempLine;
    }
    // Checks the point of a line to see if it is close
    private int checkPoint(Line l){
        int firstDistance = pyth((lastTouch.x - l.startX), (lastTouch.y - l.startY));
        int secondDistance = pyth((lastTouch.x - l.endX), (lastTouch.y - l.endY));
        if(MAX_DISTANCE > firstDistance) {
            return (int)firstDistance;
        }else if(MAX_DISTANCE > secondDistance){
            return (int)secondDistance;
        }
        return -1;
    }
    // Checks the line we have found for the close point being start or end
    private boolean checkPointStartEnd(Line l){
        boolean start = false;
        int firstDistance = pyth((lastTouch.x - l.startX), (lastTouch.y - l.startY));
        int secondDistance = pyth((lastTouch.x - l.endX), (lastTouch.y - l.endY));
        if(MAX_DISTANCE < firstDistance) {
            start = true;
        }else if(MAX_DISTANCE < secondDistance){
            start = false;
        }
        return start;
    }
    // returns pythagorian theorum
    private int pyth(double x, double y){
        int z;
        z = (int)Math.sqrt(((x * x) + (y * y)));
        return z;
    }

    public void showEditing(int index){
        closestIndex = index;
    }
}