package ca.spollock.morphing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Pair;

import java.util.ArrayList;

public class WarpImage {
    private LineController lc;  // This is a copy from the main,
                                // this will be used to process the lines
    private Bitmap right, left;
    private int pixels1[], pixels2[];

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
        right.getPixels(pixels1, 0, right.getWidth(), 0, 0, right.getWidth(), right.getHeight());
        left.getPixels(pixels2, 0, left.getWidth(), 0, 0, left.getWidth(), left.getHeight());
    }

    // Gets the points from both arrays and adds them to the respective arrays
    private void getPointPixels(){
        if(!lc.firstCanvas.isEmpty()){
            // we already know the positions of the start and end points
            // go from the first image to the last
            for(int i = 0; i < lc.secondCanvasVectors.size(); i++){
                // calculate distance to the pixel
                for(int x = 0; x < left.getWidth(); x++){
                    for(int y = 0; y < left.getHeight(); y++){
                        float first = lc.secondCanvasVectors.get(i).first,
                                second = lc.secondCanvasVectors.get(i).second;
                        Pair<Float, Float> xp = calculateVector(x, y, first, second);
                        Pair<Float, Float> n = normalVector(xp.first, xp.second);
                         = findDistanceFromLine(n.first, n.second, xp.first, xp.second);
                        double frac = fractionOnLine(xp.first, xp.second, xp.first, xp.second);
                         = fractionalPercentage(frac, xp.first, xp.second);
                System.out.println("Calculating: " + x + ", " + y); // testing if there is a crash
                        // These are all relative to each pixel on the bitmap. Use the same index
                    }
                }
            }
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
        return new Pair(Xy, Xx);
    }

    private Pair<Float, Float> calculateVector(float Xx, float Xy, float Px, float Py){
        return new Pair((Xx - Px), (Xy - Py));
    }

}
