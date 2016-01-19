package ca.spollock.morphing;

import android.graphics.Bitmap;
import android.util.Pair;

// This will be called inside of the main activity
public class WarpImage{
    private LineController lc;

    // pass in the images?
    public WarpImage(LineController controller){
        lc = controller;
        lc.calculateVectors(); // initializes vectors everytime we warp
    }

    // This will be all the data and such
        // takes the bitmap, and the side
    // this will be going from the right image to the left img (backwards?)
    private void warping(Bitmap img){
        for(int x = 0; x < img.getWidth(); x++){
            for(int y = 0; y < img.getHeight(); y++){
                // now for each line on the image
                for(int lv = 0; lv < lc.secondCanvas.size(); lv++){
                    Vector orgV = lc.secondCanvasVectors.get(lv);
                    float orgPX = lc.secondCanvas.get(lv).start.getX(),
                            orgPY = lc.secondCanvas.get(lv).start.getY();
                    Vector xp = new Vector((x - orgPX), (y - orgPY));
                        // creates a new vector from point Start to (x,y)
                    Vector normal = orgV.getNormal();
                    double distance = findDistanceFromLine(normal.getX(), normal.getY(),
                            orgV.getX(), orgV.getY());
                    double fraction = fractionOnLine(xp.getX(), xp.getY(), orgV.getX(),
                            orgV.getY());
                    double percent = fractionalPercentage(fraction, orgV.getX(), orgV.getY());
                    // save new position
                    newPoint(orgPX, orgPY, percent, distance, orgV, normal);
                }
            }
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

    // calculates dot notation
    private double calculateDot(float Xx, float Xy, float Yx, float Yy){
        return ((Xx * Yx) + (Xy * Yy));
    }

    // calculates magnitude of passed in vector
    private double calculateMagnitude(float Xx, float Xy){
        return Math.sqrt((Math.pow(Xx, 2) + (Math.pow(Xy, 2))));
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

    private Vector newPoint(float orgX, float orgY, double percent, double distance,
                                        Vector vec, Vector normal){
        float Px, Py, tempX, tempY;
        tempX = vec.getX() * (float)percent;
        tempY = vec.getY() * (float)percent;
        Px = orgX + tempX;
        Py = orgY + tempY;
        double normalMag = calculateMagnitude(normal.getX(), normal.getY());
        Px = Px - (float)(distance * (normal.getX() / normalMag));
        Py = Py - (float)(distance * (normal.getY() / normalMag));
        return new Vector(Px, Py);
    }
}