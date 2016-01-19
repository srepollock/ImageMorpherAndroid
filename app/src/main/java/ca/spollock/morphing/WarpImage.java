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
            // we already know the positions of the start and end points
            // go from the first image to the last
//            double time = System.currentTimeMillis();
//            System.out.println("Starting " + time);
            for(int i = 0; i < lc.secondCanvasVectors.size(); i++){
                // calculate distance to the pixel
                for(int x = 0; x < left.getWidth(); x++){
                    for(int y = 0; y < left.getHeight(); y++){
                        float first = lc.secondCanvasVectors.get(i).first,
                                second = lc.secondCanvasVectors.get(i).second;
                        Pair<Float, Float> xp = calculateVector(x, y, first, second);
                        Pair<Float, Float> n = normalVector(xp.first, xp.second);
                        int d = findDistanceFromLine(n.first, n.second, xp.first, xp.second);
                        double w = weight(d);
                        double frac = fractionOnLine(xp.first, xp.second, xp.first, xp.second);
                        double perc = fractionalPercentage(frac, xp.first, xp.second);
                        Pair<Float, Float> new_point = newPoint(x, y, perc, d, xp, n);
//                System.out.println("Calculating: " + x + ", " + y); // testing if there is a crash
                        // These are all relative to each pixel on the bitmap. Use the same index
                    }
//                System.out.println("Finished: " + x);
                }
            }
//            time = System.currentTimeMillis() - time;
//            System.out.println("Finito " + time);
        }
    }

    // find the distance to the pixel from the line
    // find the percentage to move along the line

    // PXx, PXy is the vector of the point to the start
    private int findDistanceFromLine(float Nx, float Ny, float PXx, float PXy){
        // p is the pixel we are looking for. This will be looped through all the pixels in the
        // picture
        double top, bottom, d;
        top = calculateDot(Nx, Ny, PXx, PXy);
        bottom = calculateMagnitude(PXx, PXy);
        d = top / bottom;
        return (int)d;
    }

    // projection of the pixel-start vector to the start-end vector
    private double fractionOnLine(float PXx, float PXy, float SEx, float SEy){
        double top, bottom, frac;
        top = calculateDot(PXx, PXy, SEx, SEy);
        bottom = calculateMagnitude(PXx, PXy);
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
        return new Pair<Float, Float>((w.first - org.first), (w.second - org.second));
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

    private Pair<Float, Float> newPoint(float orgX, float orgY, double percent, int d,
                                        Pair<Float, Float> vec, Pair<Float, Float> normal){
        float Px, Py, tempX, tempY;
        tempX = vec.first * (float)percent;
        tempY = vec.second * (float)percent;
        Px = orgX + tempX;
        Py = orgY + tempY;
        double normalMag = calculateMagnitude(normal.first, normal.second);
        Px = Px - (float)(d * (normal.first / normalMag));
        Py = Py - (float)(d * (normal.second / normalMag));
        return new Pair<>(Px, Py);
    }
}
