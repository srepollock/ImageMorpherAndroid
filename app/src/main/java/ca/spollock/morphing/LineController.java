package ca.spollock.morphing;

import android.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LineController {
    public ArrayList<Line> firstCanvas;
    public ArrayList<Line> secondCanvas;
    public ArrayList<Pair<Float, Float>> firstCanvasVectors;
    public ArrayList<Pair<Float, Float>> secondCanvasVectors;
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
            firstCanvas.get(index).endX = x;
            secondCanvas.get(index).endX = x;
        }
    }

    public void addX(float x){
        firstCanvas.get(firstCanvas.size() - 1).endX = x;
        secondCanvas.get(secondCanvas.size() - 1).endX = x;
    }

    public void addY(int index, float y){
        if(index <= firstCanvas.size() - 1){
            firstCanvas.get(index).endY = y;
            secondCanvas.get(index).endY = y;
        }
    }

    public void addY(float y){
        firstCanvas.get(firstCanvas.size() - 1).endY = y;
        secondCanvas.get(secondCanvas.size() - 1).endY = y;
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
                firstCanvasVectors.add(new Pair(firstCanvas.get(i).getVectorX(),
                        firstCanvas.get(i).getVectorY()));
                secondCanvasVectors.add(new Pair(secondCanvas.get(i).getVectorX(),
                        secondCanvas.get(i).getVectorY()));
            }
        }
    }
}
