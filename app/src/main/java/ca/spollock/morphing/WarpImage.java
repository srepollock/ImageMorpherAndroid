package ca.spollock.morphing;

import android.graphics.Bitmap;

// This will be called inside of the main activity
public class WarpImage{
    private LineController lc;
    private int rightImgPixels[], leftImgPixels[];
    private Bitmap rightBm, leftBm, finalBmRight, finalBmLeft;

    public WarpImage(LineController controller, Bitmap left, Bitmap right){
        lc = controller;
        lc.calculateVectors(); // initializes vectors everytime we warp

        leftImgPixels = new int[(left.getWidth() * left.getHeight())];
        rightImgPixels = new int[(right.getWidth() * right.getHeight())];
        leftBm = left;
        rightBm = right;
        getImgPixels();
        finalBmLeft = Bitmap.createBitmap(left.getWidth(), left.getHeight(), left.getConfig());
        finalBmRight = Bitmap.createBitmap(right.getWidth(), right.getHeight(), right.getConfig());
    }

    private void getImgPixels(){
        leftBm.getPixels(leftImgPixels, 0, leftBm.getWidth(), 0, 0, leftBm.getWidth(),
                leftBm.getHeight());
        rightBm.getPixels(rightImgPixels, 0, rightBm.getWidth(), 0, 0, rightBm.getWidth(),
                rightBm.getHeight());
    }

    // left image pixels are being copied to the position based on the lines drawn on the right image
    // Warping left (first) to right (second) lines
    public void warp(int i, int frames){
        for(int x = 0; x < leftBm.getWidth(); x++){
            for(int y = 0; y < leftBm.getHeight(); y++){
                Point Xprime = new Point(x, y);
                Point[] calculatedSrc = new Point[lc.leftCanvas.size()];
                double[] weights = new double[lc.leftCanvas.size()];

                for(int lines = 0; lines < lc.leftCanvas.size(); lines++){
                    // Source is the left hand image (REGULAR)
                    // Destination is the right hand image (PRIME)

                    //leftCanvas; // left canvas // (REGULAR)
                    //rightCanvas; // right canvas // (PRIME)

                    //leftCanvasVectors; // left canvas // (REGULAR)
                    //rightCanvasVectors; // right canvas // (PRIME)
                    Point Pprime = lc.rightCanvas.get(lines).start,
                            Qprime = lc.rightCanvas.get(lines).end,
                            P = lc.leftCanvas.get(lines).start,
                            Q = lc.leftCanvas.get(lines).end;
                    // Vector PQ == p---->q ((q.x - p.x), (q.y - p.y))
                    Vector PQprime = new Vector(Pprime, Qprime),
                            PQ = new Vector(P, Q),
                            XPprime = new Vector(Xprime, Pprime),
                            PXprime = new Vector(Pprime, Xprime);
                    // project m onto n over n
                    double distance = project(PQprime.getNormal(), XPprime); // proj XP onto N |
                    double fraction = project(PQprime, PXprime); // | proj PX onto PQ |
                    double percent = fractionalPercentage(fraction, PQprime);
                    // get the "point" (correct right here if only 1 line, else use a weighted average)
                    calculatedSrc[lines] = calculateSourcePoint(P, percent, distance, PQ, PQ.getNormal());
                    weights[lines] = weight(distance);
                }
                // Now get the ACTUAl point based on the sum of the average
//                Point srcPoint = sumWeights(Xprime, weights, calculatedSrc);
                // Now get the data and put it to the empty bitmap
//                int outX = (int)srcPoint.getX(), outY = (int)srcPoint.getY();
                int outX = (int)calculatedSrc[0].getX(), outY = (int)calculatedSrc[0].getY();
                if(outX >= leftBm.getWidth())
                    outX = (leftBm.getWidth() - 1); // -1 ???
                else if(outX < 0)
                    outX = 0;
                // else stay how it is
                if(outY >= leftBm.getHeight())
                    outY = (leftBm.getHeight() - 1); // -1 ???
                else if(outY < 0)
                    outY = 0;
                // else stay how it is

                finalBmLeft.setPixel(x, y, leftImgPixels[outX + (outY * leftBm.getWidth())]);
                //finalBmLeft.setPixel(x, y, rightBm.getPixel(outX, outY));
            }
        }
    }

    public Bitmap getFinalBmRight(){ return finalBmRight; }
    public Bitmap getFinalBmLeft() { return finalBmLeft; }

    private double project(Vector n, Vector m){
        double top, bottom, d;
        top = calculateDot(n, m);
        bottom = calculateMagnitude(n);
        d = (top / bottom);
        return d;
    }

    // calculates dot notation
    private double calculateDot(Vector n, Vector m){
        return ((n.getX() * m.getX()) + (n.getY() * m.getY()));
    }

    // calculates magnitude of passed in vector
    private double calculateMagnitude(Vector v){
        return Math.sqrt((v.getX() * v.getX()) + (v.getY() * v.getY()));
    }

    // frac from fractionOnLine, x from line vector, y from line vector
    private double fractionalPercentage(double frac, Vector n){
        double bottom, perc;
        bottom = Math.abs(calculateMagnitude(n));
        perc = frac / bottom;
        return perc;
    }

    // finished
    private Point calculateSourcePoint(Point P, double percent, double distance, Vector PQ,
                                       Vector PQnormal){
        float Px, Py, tempPQx, tempPQy, tempNx, tempNy;
        double normalMagnitude = calculateMagnitude(PQnormal);
        tempPQx = (float)percent * PQ.getX();
        tempPQy = (float)percent * PQ.getY();
        tempNx = (float)distance * (PQ.getX() / (float)normalMagnitude);
        tempNy = (float)distance * (PQ.getY() / (float)normalMagnitude);
        Px = P.getX() + tempPQx;
        Py = P.getY() + tempPQy;
        Px = Px - tempNx;
        Py = Py - tempNy;
        return new Point(Px, Py);
    }

    // This will generate weights for the position based on distance
    private double weight(double d){
        double weight, a = 0.01, b = 2;
        weight = Math.pow(((1) / (a + d)), b);
        return weight;
    }

    private Point deltaPoint(Point dest, Point found){
        return new Point((dest.getX() - found.getX()), (dest.getY() - found.getY()));
    }

    private Point sumWeights(Point Xprime, double[] weight, Point newPositions[]){
        // list of all the weights
        double totalWeightX = 0, totalWeightY = 0, topX = 0, topY = 0;
        for(int i = 0; i < newPositions.length; i++){
            Point changePoint = deltaPoint(Xprime, newPositions[i]);
            topX += changePoint.getX() * weight[i];
            topY += changePoint.getY() * weight[i];
            totalWeightX += weight[i];
            totalWeightY += weight[i];
        }
        return new Point((float)(topX / totalWeightX),
                (float)(topY / totalWeightY));
    }

    private Point interPoint(Point x, Point xprime, int i, int max){
        int tempX, tempY;
        tempX = (int)(x.getX() + (i / max) * (xprime.getX() - x.getX()));
        tempY = (int)(x.getY() + (i / max) * (xprime.getY() - x.getY()));
        return new Point(tempX, tempY); // point on the line to create a vector
    }
}