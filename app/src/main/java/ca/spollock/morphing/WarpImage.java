package ca.spollock.morphing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Timer;

public class WarpImage {
    private LineController lc;  // This is a copy from the main,
                                // this will be used to process the lines
    private Bitmap right, left;
    private int pixels1[], pixels2[], pixels3[];

//    // Arrays of the line start and end points in the graph
//    private ArrayList<Integer> rightStart = new ArrayList<>();
//    private ArrayList<Integer> rightEnd = new ArrayList<>();
//    private ArrayList<Integer> leftStart = new ArrayList<>();
//    private ArrayList<Integer> leftEnd = new ArrayList<>();

    // Setup frames later
    public WarpImage(LineController controller, Uri firstURI, Uri secondURI){
        lc = controller;
        BitmapFactory bmf = new BitmapFactory();
        right = bmf.decodeFile(firstURI.getPath());
        left = bmf.decodeFile(secondURI.getPath());

        getPixels();
        lc.calculateVectors(); // gets all the vectors and sets them to arrays inside of controller
        getPointPixels(); // initializes everything for setup, with the line points
    }

    public WarpImage(LineController controller, Bitmap first, Bitmap second){
        lc = controller;
        right = first;
        left = second;

        getPixels();
        lc.calculateVectors();
        getPointPixels();
    }

    private void getPixels(){
        pixels1 = new int[(right.getWidth() * right.getHeight())];
        pixels2 = new int[(left.getWidth() * left.getHeight())];
        pixels3 = new int[(left.getWidth() * left.getHeight())];
        right.getPixels(pixels1, 0, right.getWidth(), 0, 0, right.getWidth(), right.getHeight());
        left.getPixels(pixels2, 0, left.getWidth(), 0, 0, left.getWidth(), left.getHeight());
    }

    // Gets the points from both arrays and adds them to the respective arrays
    private void getPointPixels(){
        if(!lc.firstCanvas.isEmpty()){
            double time = System.currentTimeMillis();
            System.out.println("Starting " + time);
            for(int lv = 0; lv < lc.secondCanvasVectors.size(); lv++){
                for(int x = 0; x < left.getWidth(); x++){
                    for(int y = 0; y < left.getHeight(); y++){
                        float cVFirst = lc.secondCanvasVectors.get(lv).first,
                                cVSecond = lc.secondCanvasVectors.get(lv).second,
                                PstartX = lc.secondCanvas.get(lv).startX,
                                PstartY = lc.secondCanvas.get(lv).startY;
                                            // gets the line vector positions
                        Pair<Float, Float> xp = calculateVector(x, y,
                                PstartX, PstartY);
                                // xp is the vector to the pixel from the start
                        Pair<Float, Float> n = normalVector(cVFirst, cVSecond);
                        double distance = findDistanceFromLine(n.first, n.second, xp.first,
                                xp.second);
                        double frac = fractionOnLine(xp.first, xp.second, cVFirst, cVSecond);
                        double percent = fractionalPercentage(frac, cVFirst, cVSecond);
                        Pair<Float, Float> newPosition = newPoint(PstartX, PstartY, percent,
                                distance, lc.secondCanvasVectors.get(lv), n); // Save result
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            System.out.println("Finito " + time);
        }
    }

    // find the distance to the pixel from the line
    // find the percentage to move along the line

    // XPx, XPy is the vector of the point to the start
    private int findDistanceFromLine(float Nx, float Ny, float XPx, float XPy){
        // p is the pixel we are looking for. This will be looped through all the pixels in the
        // picture
        double top, bottom, d;
        top = calculateDot(Nx, Ny, XPx, XPy);
        bottom = calculateMagnitude(Nx, Ny);
        d = top / bottom;
        return (int)d;
    }

    // projection of the pixel-start vector to the start-end vector
    private double fractionOnLine(float XPx, float XPy, float SEx, float SEy){
        double top, bottom, frac;
        top = calculateDot(XPx, XPy, SEx, SEy);
        bottom = calculateMagnitude(XPx, XPy);
        frac = top / bottom;
        return frac;
    }

    // frac from fractionOnLine, x from line vector, y from line vector
    private double fractionalPercentage(double frac, float SEx, float SEy){
        double bottom, perc;
        bottom = calculateMagnitude(SEx, SEy);
        perc = frac / bottom;
        return perc;
    }

    // calculates dot notation
    private double calculateDot(float Xx, float Xy, float Yx, float Yy){
        return ((Xx * Yx) + (Xy * Yy));
    }

    // calculates magnitude of passed in vector
    private double calculateMagnitude(float Xx, float Xy){
        return Math.sqrt((Math.pow(Xx, 2) + (Math.pow(Xy, 2))));
    }

    // Pass in the start,end vector x,y
    private Pair<Float, Float> normalVector(float Xx, float Xy){
        float invXy = Xy * -1;
        return new Pair(invXy, Xx);
    }

    private Pair<Float, Float> calculateVector(float Xx, float Xy, float Px, float Py){
        return new Pair((Xx - Px), (Xy - Py));
    }

    // This will generate weights for the position based on distance
    private double weight(int d){
        double weight, a = 0.01, b = 2;
        weight = Math.pow(((1) / (a + d)), b);
        return weight;
    }

    private Pair<Float, Float> change(Pair<Float, Float> w, Pair<Float, Float> org){
        return new Pair<>((w.first - org.first), (w.second - org.second));
    }

    private Pair<Float, Float> sumWeights(ArrayList<Pair<Float, Float>> weight, float orgX, float orgY,
                            ArrayList<Pair<Float, Float>> newPositions){
        // list of all the weights
        double weightX = 0, weightY = 0, totalWeightX = 0, totalWeightY = 0;
        for(int i = 0; i < weight.size(); i++){
            Pair<Float, Float> change = change(newPositions.get(i),
                    new Pair<Float, Float>(orgX, orgY));
            weightX += change.first * weight.get(i).first;
            weightY += change.second * weight.get(i).second;
            totalWeightX += weight.get(i).first;
            totalWeightY += weight.get(i).second;
        }
        return new Pair<>((float)(weightX / totalWeightX),
                (float)(weightY / totalWeightY));
    }

    private Pair<Float, Float> newPoint(float orgX, float orgY, double percent, double distance,
                                        Pair<Float, Float> vec, Pair<Float, Float> normal){
        float Px, Py, tempX, tempY;
        tempX = vec.first * (float)percent;
        tempY = vec.second * (float)percent;
        Px = orgX + tempX;
        Py = orgY + tempY;
        double normalMag = calculateMagnitude(normal.first, normal.second);
        Px = Px - (float)(distance * (normal.first / normalMag));
        Py = Py - (float)(distance * (normal.second / normalMag));
        return new Pair<>(Px, Py);
    }
}
