package ca.spollock.morphing;

import java.util.ArrayList;

public class LineController {
    public ArrayList<Line> firstCanvas;
    public ArrayList<Line> secondCanvas;
    private int idx;
    LineController(){
        firstCanvas = new ArrayList<>();
        secondCanvas = new ArrayList<>();
        idx = 0;
    }
    public void addLine(Line l){
        firstCanvas.add(l);
        secondCanvas.add(l);
        idx++;
    }

    public void addLine(float x, float y){
        firstCanvas.add(new Line(x, y));
        secondCanvas.add(new Line(x, y));
        idx++;
    }

    public void addX(int index, float x){
        if(index <= idx){
            firstCanvas.get(index).endX = x;
            secondCanvas.get(index).endX = x;
        }
    }

    public void addX(float x){
        firstCanvas.get(firstCanvas.size() - 1).endX = x;
        secondCanvas.get(secondCanvas.size() - 1).endX = x;
    }

    public void addY(int index, float y){
        if(index <= idx){
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
        idx = 0;
    }

    public boolean removeLast(){
        if(firstCanvas.isEmpty()){
            return false;
        }
        if(firstCanvas.size() == 1){
            idx = 0;
            firstCanvas.clear();
            secondCanvas.clear();
        }else{
            idx--;
            firstCanvas.remove(idx);
            secondCanvas.remove(idx);
        }
        return true;
    }
}
