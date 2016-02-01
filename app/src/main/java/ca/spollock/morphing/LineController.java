package ca.spollock.morphing;

import java.util.ArrayList;

/**
 *  Linecontroller for the all the lines drawn on the editview
 */
public class LineController {
    /**
     * ArrayList of the leftCanvas lines
     */
    public ArrayList<Line> leftCanvas; // left canvas
    /**
     * ArrayList of the rightCanvas lines
     */
    public ArrayList<Line> rightCanvas; // right canvas
    /**
     * ArrayList of the leftCanvas vectors
     */
    public ArrayList<Vector> leftCanvasVectors; // left canvas
    /**
     * ArrayList of the rightCanvas vectors
     */
    public ArrayList<Vector> rightCanvasVectors; // right canvas

    /**
     * Constructor
     */
    LineController(){
        leftCanvas = new ArrayList<>();
        rightCanvas = new ArrayList<>();
        leftCanvasVectors = new ArrayList<>();
        rightCanvasVectors = new ArrayList<>();
    }

    /**
     * Adds a line to the left/rightCanvas ArrayList
     */
    public void addLine(Line l){
        leftCanvas.add(l);
        rightCanvas.add(l);
    }

    /**
     * Adds a line to the left/rightCanvas ArrayList
     */
    public void addLine(float x, float y){
        leftCanvas.add(new Line(x, y));
        rightCanvas.add(new Line(x, y));
    }

    public void addX(int index, float x){
        if(index <= leftCanvas.size() - 1){
            leftCanvas.get(index).end.setX(x);
            rightCanvas.get(index).end.setX(x);
        }
    }

    /**
     * Adding X to the left and right canvas' end point
     */
    public void addX(float x){
        leftCanvas.get(leftCanvas.size() - 1).end.setX(x);
        rightCanvas.get(rightCanvas.size() - 1).end.setX(x);
    }

    public void addY(int index, float y){
        if(index <= leftCanvas.size() - 1){
            leftCanvas.get(index).end.setY(y);
            rightCanvas.get(index).end.setY(y);
        }
    }

    /**
     * Adding Y to the left and right canvas' end point
     */
    public void addY(float y){
        leftCanvas.get(leftCanvas.size() - 1).end.setY(y);
        rightCanvas.get(rightCanvas.size() - 1).end.setY(y);
    }

    /**
     * Clears all lines in the lists
     */
    public void clearLists(){
        leftCanvas.clear();
        rightCanvas.clear();
        leftCanvasVectors.clear();
        rightCanvasVectors.clear();
    }

    /**
     * Removes the last line in the ArrayLists
     */
    public boolean removeLast(){
        if(leftCanvas.isEmpty()){
            return false;
        }
        if(leftCanvas.size() == 1){
            leftCanvas.clear();
            rightCanvas.clear();
            leftCanvasVectors.clear();
            rightCanvasVectors.clear();
        }else{
            leftCanvas.remove(leftCanvas.size() - 1);
            rightCanvas.remove(rightCanvas.size() - 1);
            leftCanvasVectors.clear();
            rightCanvasVectors.clear();
        }
        return true;
    }
}
