package ca.spollock.morphing;

import android.util.Pair;

import java.util.ArrayList;

public class LineController {
    public ArrayList<Line> firstCanvas; // left canvas
    public ArrayList<Line> secondCanvas; // right canvas
    public ArrayList<Vector> firstCanvasVectors; // left canvas
    public ArrayList<Vector> secondCanvasVectors; // right canvas
    LineController(){
        firstCanvas = new ArrayList<>();
        secondCanvas = new ArrayList<>();
        firstCanvasVectors = new ArrayList<>();
        secondCanvasVectors = new ArrayList<>();
    }
    public void addLine(Line l){
        firstCanvas.add(l);
        secondCanvas.add(l);
    }

    public void addLine(float x, float y){
        firstCanvas.add(new Line(x, y));
        secondCanvas.add(new Line(x, y));
    }

    public void addX(int index, float x){
        if(index <= firstCanvas.size() - 1){
            firstCanvas.get(index).end.setX(x);
            secondCanvas.get(index).end.setX(x);
        }
    }

    public void addX(float x){
        firstCanvas.get(firstCanvas.size() - 1).end.setX(x);
        secondCanvas.get(secondCanvas.size() - 1).end.setX(x);
    }

    public void addY(int index, float y){
        if(index <= firstCanvas.size() - 1){
            firstCanvas.get(index).end.setY(y);
            secondCanvas.get(index).end.setY(y);
        }
    }

    public void addY(float y){
        firstCanvas.get(firstCanvas.size() - 1).end.setY(y);
        secondCanvas.get(secondCanvas.size() - 1).end.setY(y);
    }

    public void clearLists(){
        firstCanvas.clear();
        secondCanvas.clear();
        firstCanvasVectors.clear();
        secondCanvasVectors.clear();
    }

    public boolean removeLast(){
        if(firstCanvas.isEmpty()){
            return false;
        }
        if(firstCanvas.size() == 1){
            firstCanvas.clear();
            secondCanvas.clear();
            firstCanvasVectors.clear();
            secondCanvasVectors.clear();
        }else{
            firstCanvas.remove(firstCanvas.size() - 1);
            secondCanvas.remove(secondCanvas.size() - 1);
            firstCanvasVectors.clear();
            secondCanvasVectors.clear();
        }
        return true;
    }

    public void calculateVectors(){
        if(!firstCanvas.isEmpty()){
            for(int i = 0; i < firstCanvas.size(); i++){
                firstCanvasVectors.add(firstCanvas.get(i).getLineVector());
                secondCanvasVectors.add(secondCanvas.get(i).getLineVector());
            }
        }
    }
}
