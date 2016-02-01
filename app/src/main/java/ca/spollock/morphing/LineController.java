package ca.spollock.morphing;

import android.util.Pair;

import java.util.ArrayList;

public class LineController {
    public ArrayList<Line> leftCanvas; // left canvas
    public ArrayList<Line> rightCanvas; // right canvas
    public ArrayList<Vector> leftCanvasVectors; // left canvas
    public ArrayList<Vector> rightCanvasVectors; // right canvas
    LineController(){
        leftCanvas = new ArrayList<>();
        rightCanvas = new ArrayList<>();
        leftCanvasVectors = new ArrayList<>();
        rightCanvasVectors = new ArrayList<>();
    }
    public void addLine(Line l){
        leftCanvas.add(l);
        rightCanvas.add(l);
    }

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

    public void addY(float y){
        leftCanvas.get(leftCanvas.size() - 1).end.setY(y);
        rightCanvas.get(rightCanvas.size() - 1).end.setY(y);
    }

    public void clearLists(){
        leftCanvas.clear();
        rightCanvas.clear();
        leftCanvasVectors.clear();
        rightCanvasVectors.clear();
    }

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
