package ca.spollock.morphing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Pair;

import java.util.ArrayList;

// This will be called inside of the main activity
public class WarpImage{
    private LineController lc;
    private int firstImgPixels[], secondImgPixels[];
    private Bitmap firstBm, secondBm, finalBm;

    // pass in the images?
    public WarpImage(LineController controller, Uri firstUri, Uri secondUri){
        lc = controller;
        lc.calculateVectors(); // initializes vectors everytime we warp

        BitmapFactory bmf = new BitmapFactory();
        Bitmap first = bmf.decodeFile(firstUri.getPath()),
                second = bmf.decodeFile(secondUri.getPath());
        firstImgPixels = new int[(first.getWidth() * first.getHeight())];
        secondImgPixels = new int[(second.getWidth() * second.getHeight())];
        firstBm = first;
        secondBm = second;
        getImgPixels(first, second);
        finalBm = Bitmap.createBitmap(second.getWidth(), second.getHeight(), null);
        warping(finalBm);
    }

    public WarpImage(LineController controller, Bitmap first, Bitmap second){
        lc = controller;
        lc.calculateVectors(); // initializes vectors everytime we warp

        BitmapFactory bmf = new BitmapFactory();
        firstImgPixels = new int[(first.getWidth() * first.getHeight())];
        secondImgPixels = new int[(second.getWidth() * second.getHeight())];
        firstBm = first;
        secondBm = second;
        getImgPixels(first, second);
        finalBm = second;
        warping(finalBm);
    }

    private void getImgPixels(Bitmap first, Bitmap second){
        first.getPixels(firstImgPixels, 0, first.getWidth(), 0, 0, first.getWidth(),
                first.getHeight());
        second.getPixels(secondImgPixels, 0, second.getWidth(), 0, 0, second.getWidth(),
                second.getHeight());
    }

    // This will be all the data and such
        // takes the bitmap, and the side
    // this will be going from the right image to the left img (backwards?)
    private void warping(Bitmap img){
        for(int x = 0; x < img.getWidth(); x++){
            for(int y = 0; y < img.getHeight(); y++){
                // now for each line on the image
                Point points[] = new Point[lc.secondCanvas.size()];
                double[] weights = new double[lc.secondCanvas.size()];
                for(int lv = 0; lv < lc.secondCanvas.size(); lv++){
                    Vector orgVector = lc.secondCanvasVectors.get(lv),
                            vectorPrime = lc.firstCanvasVectors.get(lv);
                    float sPX = lc.secondCanvas.get(lv).start.getX(),
                            sPY = lc.secondCanvas.get(lv).start.getY(),
                            spPX = lc.firstCanvas.get(lv).start.getX(),
                            spPY = lc.firstCanvas.get(lv).start.getY();
                    Vector xoVector = new Vector((x - sPX), (y - sPY));
                    Vector normal = orgVector.getNormal(), normalPrime = vectorPrime.getNormal();

                    double distance = findDistanceFromLine(normal.getX(), normal.getY(),
                            xoVector.getX(), xoVector.getY());
                    double frac = fractionOnLine(orgVector.getX(), orgVector.getY(),
                            xoVector.getX(), xoVector.getY());
                    double percent = fractionalPercentage(frac, orgVector.getX(),
                            orgVector.getY());
                    // this should be from the src, while we are working on the dest
                    points[lv] = newPoint(spPX, spPY, percent, distance, vectorPrime, normalPrime);
                    weights[lv] = weight(distance);
                }
                // get the origin point of pixel(x) from the first img
                Point newPoint = sumWeights(weights, new Point(x,y), points);
                // set pixel
                int tempX = (int)newPoint.getX(), tempY = (int)newPoint.getY();
                if(tempX >= 0 && tempY >= 0 && tempX < img.getWidth() && tempY < img.getHeight()) {
                    img.setPixel(tempX, tempY, firstImgPixels[x + (y * firstBm.getWidth())]);
                }else if(tempX < 0 && tempY >= 0 && tempY < img.getHeight()){
                    img.setPixel(0, tempY, firstImgPixels[x + (y * firstBm.getWidth())]);
                }else if(tempX >= 0 && tempY < 0 && tempX < img.getWidth()){
                    img.setPixel(tempX, 0, firstImgPixels[x + (y * firstBm.getWidth())]);
                }else{
                    img.setPixel(0, 0, firstImgPixels[x + (y * firstBm.getWidth())]);
                }
            }
        }
    }

    public Bitmap getWarpedBitmap(){
        return finalBm;
    }

    // find the distance to the pixel from the line
    // find the percentage to move along the line

    // XPx, XPy is the vector of the point to the start
    private int findDistanceFromLine(float Nx, float Ny, float XSx, float XSy){
        // p is the pixel we are looking for. This will be looped through all the pixels in the
        // picture
        double top, bottom, d;
        top = calculateDot(Nx, Ny, XSx, XSy);
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
    private double fractionOnLine(float SEx, float SEy, float XSx, float XSy){
        double top, bottom, frac;
        top = calculateDot(SEx, SEy, XSx, XSy);
        bottom = calculateMagnitude(SEx, SEy);
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

    private Point newPoint(float xPrime, float yPrime, double percent, double distance,
                                        Vector vectorPrime, Vector normalPrime){
        float Px, Py, tempX, tempY;
        tempX = vectorPrime.getX() * (float)percent;
        tempY = vectorPrime.getY() * (float)percent;
        Px = xPrime + tempX;
        Py = yPrime + tempY;
        double normalMag = calculateMagnitude(normalPrime.getX(), normalPrime.getY());
        Px = Px - (float)(distance * (normalPrime.getX() / normalMag));
        Py = Py - (float)(distance * (normalPrime.getY() / normalMag));
        return new Point(Px, Py);
    }

    // This will generate weights for the position based on distance
    private double weight(double d){
        double weight, a = 0.01, b = 2;
        weight = Math.pow(((1) / (a + d)), b);
        return weight;
    }

    private Point changePoint(Point np, Point org){
        return new Point((np.getX() - org.getX()), (np.getY() - org.getY()));
    }

    private Point sumWeights(double[] weight, Point orgPixel, Point newPositions[]){
        // list of all the weights
        double totalWeightX = 0, totalWeightY = 0, topX = 0, topY = 0;
        for(int i = 0; i < newPositions.length; i++){
            Point changePoint = changePoint(newPositions[i], orgPixel);
            topX += changePoint.getX() * weight[i];
            topY += changePoint.getY() * weight[i];
            totalWeightX += weight[i];
            totalWeightY += weight[i];
        }
        return new Point((float)(topX / totalWeightX),
                (float)(topY / totalWeightY));
    }
}