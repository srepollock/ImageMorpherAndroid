package ca.spollock.morphing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

/**
 * Edit view for the lines to be drawn and displayed on
 */
public class EditingView extends View {
    /**
     * Linecontroller containing all the lines to be drawn
     */
    private LineController lc;
    /**
     * Colour of the regular paint
     */
    private final Paint mPaint;
    /**
     * Boolean for drawing mode. Defaulted to true
     */
    private boolean drawingMode = true;
    /**
     * Index of the line
     */
    private int closestIndex = -1;
    /**
     * Colour of the regular dot
     */
    private Paint editDot;
    /**
     * Colour of the edit line
     */
    private Paint editLine;
    /**
     * Checks if the end of the line
     */
    private boolean endOfLine;
    /**
     * Checks if no line. Defualt true
     */
    private boolean noLine = true;
    /**
     * Last touch point
     */
    private Point lastTouch;
    /**
     * Distance before dot is enabled for editing
     */
    private final static int MAX_DISTANCE = 50;
    /**
     * Editing line variable
     */
    private Line editingLine = null;
    /**
     * Index of the view (0 or 1)
     */
    private int viewIndex;

    /**
     * Edit line takes in the context of the activity to draw to
     */
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

    /**
     * Initializes the line controller based on that passed in from main
     */
    public void init(LineController controller){
        lc = controller;
    }

    /**
     * onDraw override so that we can specifically draw lines based on the left or right being
     * selected
     */
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int counter = 0;
        if(viewIndex == 0){ // first View
            for (Line l : lc.leftCanvas) {
                if(closestIndex == -1) {
                    canvas.drawLine(l.start.getX(), l.start.getY(), l.end.getX(), l.end.getY(), mPaint);
                }else {
                    if (closestIndex == counter && !drawingMode) {
                        canvas.drawLine(l.start.getX(), l.start.getY(), l.end.getX(), l.end.getY(), editLine);
                    }else{
                        canvas.drawLine(l.start.getX(), l.start.getY(), l.end.getX(), l.end.getY(), mPaint);
                    }
                }
                counter++;
            }
            if(!drawingMode){
                // if in edit, draw a line around the index we found
                if(closestIndex != -1) {
                    canvas.drawCircle(lc.leftCanvas.get(closestIndex).start.getX(),
                            lc.leftCanvas.get(closestIndex).start.getY(), 20, editDot);
                    canvas.drawCircle(lc.leftCanvas.get(closestIndex).end.getX(),
                            lc.leftCanvas.get(closestIndex).end.getY(), 20, editDot);
                }
            }
        }else if(viewIndex == 1){
            for (Line l : lc.rightCanvas) {
                if(closestIndex == -1) {
                    canvas.drawLine(l.start.getX(), l.start.getY(), l.end.getX(), l.end.getY(), mPaint);
                }else {
                    if (closestIndex == counter && !drawingMode) {
                        canvas.drawLine(l.start.getX(), l.start.getY(), l.end.getX(), l.end.getY(), editLine);
                    }else{
                        canvas.drawLine(l.start.getX(), l.start.getY(), l.end.getX(), l.end.getY(), mPaint);
                    }
                }
                counter++;
            }
            if(!drawingMode){
                // if in edit, draw a line around the index we found
                if(closestIndex != -1) {
                    canvas.drawCircle(lc.rightCanvas.get(closestIndex).start.getX(),
                            lc.rightCanvas.get(closestIndex).start.getY(), 20, editDot);
                    canvas.drawCircle(lc.rightCanvas.get(closestIndex).end.getX(),
                            lc.rightCanvas.get(closestIndex).end.getY(), 20, editDot);
                }
            }
        }
    }

    /**
     * Drawing line mode
     */
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

    /**
     * Edit line mode
     */
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
                        editingLine.start.setX(event.getX());
                        editingLine.start.setY(event.getY());
                    }else{
                        editingLine.end.setX(event.getX());
                        editingLine.end.setY(event.getY());
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!noLine){
                    // we found one
                    if(!endOfLine){
                        editingLine.start.setX(event.getX());
                        editingLine.start.setY(event.getY());
                    }else{
                        editingLine.end.setX(event.getX());
                        editingLine.end.setY(event.getY());
                    }
                    editingLine = null;
                    invalidate();
                }
                lastTouch = null;
                break;
        }
        return closestIndex;
    }

    /**
     * Sets edit or drawing mode
     */
    public void editMode(int index){
        drawingMode = false;
        closestIndex = index;
    }

    /**
     * Clears the index of all the lines
     */
    public void clear() { closestIndex = -1; }

    /**
     * Turns on drawing mode
     */
    public void drawingMode(){
        drawingMode = true;
    }

    /**
     * Sets the view index
     */
    public void viewIndex(int index){
        viewIndex = index;
    }

    /**
     * Finds the closest line to edit
     */
    private Line findClosestLine(){
        int temp1, temp2;
        Line tempLine = null;
        int closestDistance = MAX_DISTANCE;
        // needs to loop through the array list, for both startX,Y and endX,Y of each line in the array
        // then needs to get the index to that point and draw a circle around that point
        // also change the colour of the line and the corresponding line based on that index

        if(viewIndex == 0){
            for(int i = 0; i < lc.leftCanvas.size(); i++){
                temp1 = checkPoint(lc.leftCanvas.get(i));
                if(temp1 < closestDistance && temp1 != -1) {
                    tempLine = lc.leftCanvas.get(i);
                    closestIndex = i;
                }
            }
        }else{
            for(int i = 0; i < lc.leftCanvas.size(); i++){
                temp2 = checkPoint(lc.rightCanvas.get(i));
                if(temp2 < closestDistance && temp2 != -1){
                    tempLine = lc.rightCanvas.get(i);
                    closestIndex = i;
                }
            }
        }
        return tempLine;
    }

    /**
     * Checks to see if the line passed in is the closest to the distance based on the last touch of
     * the user
     */
    private int checkPoint(Line l){
        int firstDistance = pyth((lastTouch.x - l.start.getX()), (lastTouch.y - l.start.getY()));
        int secondDistance = pyth((lastTouch.x - l.end.getX()), (lastTouch.y - l.end.getY()));
        if(MAX_DISTANCE > firstDistance) {
            return (int)firstDistance;
        }else if(MAX_DISTANCE > secondDistance){
            return (int)secondDistance;
        }
        return -1;
    }

    /**
     * Checks the line we have fround for the clost point being the start or end
     */
    private boolean checkPointStartEnd(Line l){
        boolean start = false;
        int firstDistance = pyth((lastTouch.x - l.start.getX()), (lastTouch.y - l.start.getY()));
        int secondDistance = pyth((lastTouch.x - l.end.getX()), (lastTouch.y - l.end.getY()));
        if(MAX_DISTANCE < firstDistance) {
            start = true;
        }else if(MAX_DISTANCE < secondDistance){
            start = false;
        }
        return start;
    }

    /**
     * Returns the value of the pythagorian theorum
     */
    private int pyth(double x, double y){
        int z;
        z = (int)Math.sqrt(((x * x) + (y * y)));
        return z;
    }

    /**
     * Sets line at the index to editing type
     */
    public void showEditing(int index){
        closestIndex = index;
    }
}