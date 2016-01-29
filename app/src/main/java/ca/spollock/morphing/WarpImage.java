package ca.spollock.morphing;

import android.graphics.Bitmap;

// This will be called inside of the main activity
public class WarpImage{
    private LineController lc;
    private int rightImgPixels[], leftImgPixels[];
    private Bitmap rightBm, leftBm, finalBmRight, finalBmLeft;

    public WarpImage(LineController controller, Bitmap left, Bitmap right){
        lc = controller;
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
    public void leftWarp(int i, int frames){
        for(int x = 0; x < leftBm.getWidth(); x++){
            for(int y = 0; y < leftBm.getHeight(); y++){
//////////////////////////////////////////----Testing----///////////////////////////////////////////
//        int x = 20, y = 20;
//////////////////////////////////////////----Testing----///////////////////////////////////////////
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

                    // This is used to generate the intermediate frames.
                        // Joe was saying something about just generating the final right away,
                        // then for each intermediate just take the original, go i / frames and
                        // put the new pixel in position x - x(i/frame), y - y(i/frame) test?
                    Point starts = interPoint(lc.leftCanvas.get(lines).start,
                            lc.rightCanvas.get(lines).start, i, frames),
                    ends = interPoint(lc.leftCanvas.get(lines).end,
                            lc.rightCanvas.get(lines).end, i, frames);

                    // SOMETHING WRONG HERE, CHECK README
                    Vector PQ = new Vector((ends.getX() - starts.getX()) , (ends.getY() - starts.getY())); // should be this way
//                    Vector PQ = new Vector((starts.getX() - ends.getX()) , (starts.getY() - ends.getY()));

                    Point Pprime = lc.rightCanvas.get(lines).start,
                            Qprime = lc.rightCanvas.get(lines).end,
                            P = lc.leftCanvas.get(lines).start,
                            Q = lc.leftCanvas.get(lines).end;
                    // Vector PQ == p---->q ((q.x - p.x), (q.y - p.y))
                    Vector PQprime = new Vector(Pprime, Qprime),
//                            PQ = new Vector(P, Q),
                            XPprime = new Vector(Xprime, Pprime),
                            PXprime = new Vector(Pprime, Xprime);

//////////////////////////////////////////----Testing----///////////////////////////////////////////
//                    Point X = new Point(20,20),
//                            P = new Point((float)377.7143, (float)176.82141),
//                            Q = new Point((float)358.85715, (float)711.6428),
//                            Pprime = new Point((float)377.7143, (float)176.82141),
//                            Qprime = new Point((float)358.85715, (float)711.6428);
//
//                    Vector PQ = new Vector((float)-18.857147, (float)534.8214),
//                           PQprime = new Vector(P,Q),
//                           XPprime = new Vector((float)357.7143, (float)156.82141),
//                           PXprime = new Vector((float)-357.7143, (float)-156.82141);
//////////////////////////////////////////----Testing----///////////////////////////////////////////

                    // project m onto n over n
                    double distance = project(PQprime.getNormal(), XPprime); // proj XP onto N |
                    double fraction = project(PQprime, PXprime); // | proj PX onto PQ |
                    double percent = fractionalPercentage(fraction, PQprime);
                    // get the "point" (correct right here if only 1 line, else use a weighted average)
                    calculatedSrc[lines] = calculateSourcePoint(P, percent, distance, PQ);
                    weights[lines] = weight(distance);

//////////////////////////////////////////----Testing----///////////////////////////////////////////
//System.out.println("X = " + x);
//System.out.println("Y = " + y);
//System.out.println("P = (" + P.getX() + ", " + P.getY() + ")");
//System.out.println("Q = (" + Q.getX() + ", " + Q.getY() + ")");
//System.out.println("Pprime = (" + Pprime.getX() + ", " + Pprime.getY() + ")");
//System.out.println("Qprime = (" + Qprime.getX() + ", " + Qprime.getY() + ")");
//System.out.println("PQnormal = (" + PQ.getNormal().getX() + ", " + PQ.getNormal().getY() + ")");
//System.out.println("PQprime = (" + PQprime.getX() + ", " + PQprime.getY() + ")");
//System.out.println("PQ = (" + PQ.getX() + ", " + PQ.getY() + ")");
//System.out.println("XP = (" + XPprime.getX() + ", " + XPprime.getY() + ")");
//System.out.println("PX = (" + PXprime.getX() + ", " + PXprime.getY() + ")");
//System.out.println("Distance = " + distance);
//System.out.println("Fraction = " + fraction);
//System.out.println("Percent = " + percent);
//System.out.println("newPoint(" + calculatedSrc[lines].getX() + ", " + calculatedSrc[lines].getY() + ")");
//System.out.println("Weight = " + weights[lines]);
//////////////////////////////////////////----Testing----///////////////////////////////////////////
                }

                // Now get the ACTUAl point based on the sum of the average
                Point srcPoint = sumWeights(Xprime, weights, calculatedSrc);
                // Now get the data and put it to the empty bitmap
                int outX = (int)srcPoint.getX(), outY = (int)srcPoint.getY();
//                int outX = (int)calculatedSrc[0].getX(), outY = (int)calculatedSrc[0].getY();

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
//////////////////////////////////////////----Testing----///////////////////////////////////////////
//        System.out.println("Getting = (" + outX + ", " + outY + ")");
//////////////////////////////////////////----Testing----///////////////////////////////////////////

//                finalBmLeft.setPixel(x, y, leftImgPixels[outX + (outY * leftBm.getWidth())]);
                finalBmLeft.setPixel(x, y, leftBm.getPixel(outX, outY)); // error here?
            }
        }
    }

    // right image pixels are being copied to the position based on the lines drawn on the left image
    // Warping right (second) to left (first) lines
    public void rightWarp(int i, int frames){
        for(int x = 0; x < rightBm.getWidth(); x++){
            for(int y = 0; y < rightBm.getHeight(); y++){
                Point Xprime = new Point(x, y);
                Point[] calculatedSrc = new Point[lc.rightCanvas.size()];
                double[] weights = new double[lc.rightCanvas.size()];

                for(int lines = 0; lines < lc.rightCanvas.size(); lines++){
                    // Source is the left hand image (REGULAR)
                    // Destination is the right hand image (PRIME)

                    //leftCanvas; // left canvas // (REGULAR)
                    //rightCanvas; // right canvas // (PRIME)

                    //leftCanvasVectors; // left canvas // (REGULAR)
                    //rightCanvasVectors; // right canvas // (PRIME)

                    Point starts = interPoint(lc.rightCanvas.get(lines).start,
                            lc.leftCanvas.get(lines).start, i, frames),
                            ends = interPoint(lc.rightCanvas.get(lines).end,
                                    lc.leftCanvas.get(lines).end, i, frames);
                    Vector PQ = new Vector((ends.getX() - starts.getX()) , (ends.getY() - starts.getY()));

                    Point Pprime = lc.leftCanvas.get(lines).start,
                            Qprime = lc.leftCanvas.get(lines).end,
                            P = lc.rightCanvas.get(lines).start,
                            Q = lc.rightCanvas.get(lines).end;
                    // Vector PQ == p---->q ((q.x - p.x), (q.y - p.y))
                    Vector PQprime = new Vector(Pprime, Qprime),
//                            PQ = new Vector(P, Q),
                            XPprime = new Vector(Xprime, Pprime),
                            PXprime = new Vector(Pprime, Xprime);

                    // project m onto n over n
                    double distance = project(PQprime.getNormal(), XPprime); // proj XP onto N |
                    double fraction = project(PQprime, PXprime); // | proj PX onto PQ |
                    double percent = fractionalPercentage(fraction, PQprime);
                    // get the "point" (correct right here if only 1 line, else use a weighted average)
                    calculatedSrc[lines] = calculateSourcePoint(P, percent, distance, PQ);
                    weights[lines] = weight(distance);
                }

                // Now get the ACTUAl point based on the sum of the average
//                Point srcPoint = sumWeights(Xprime, weights, calculatedSrc);
                // Now get the data and put it to the empty bitmap
//                int outX = (int)srcPoint.getX(), outY = (int)srcPoint.getY();
                int outX = (int)calculatedSrc[0].getX(), outY = (int)calculatedSrc[0].getY();

                if(outX >= rightBm.getWidth())
                    outX = (rightBm.getWidth() - 1); // -1 ???
                else if(outX < 0)
                    outX = 0;
                // else stay how it is
                if(outY >= rightBm.getHeight())
                    outY = (rightBm.getHeight() - 1); // -1 ???
                else if(outY < 0)
                    outY = 0;
                // else stay how it is
                finalBmRight.setPixel(x, y, rightBm.getPixel(outX, outY)); // error here?
            }
        }
    }

    public Bitmap getFinalBmRight(){ return finalBmRight; }
    public Bitmap getFinalBmLeft() { return finalBmLeft; }

    private double project(Vector n, Vector m){
        double top, bottom, d;
        top = calculateDot(n, m);
        bottom = Math.abs(calculateMagnitude(n));
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
    private Point calculateSourcePoint(Point P, double percent, double distance, Vector PQ){
        float Px, Py, tempPQx, tempPQy, tempNx, tempNy;
        double normalMagnitude = Math.abs(calculateMagnitude(PQ.getNormal()));
        tempPQx = (float)percent * PQ.getX();
        tempPQy = (float)percent * PQ.getY();
        tempNx = (float)distance * (PQ.getNormal().getX() / (float)normalMagnitude);
        tempNy = (float)distance * (PQ.getNormal().getY() / (float)normalMagnitude);
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

    private Point deltaPoint(Point src, Point dest){
        return new Point((src.getX() - dest.getX()), (src.getY() - dest.getY()));
    }

    private Point sumWeights(Point Xprime, double[] weight, Point newPositions[]){
        // list of all the weights
        double totalWeight = 0, topX = 0, topY = 0;
        float outX, outY;
        for(int i = 0; i < newPositions.length; i++){
            Point changePoint = deltaPoint(Xprime, newPositions[i]);
            topX += changePoint.getX() * weight[i];
            topY += changePoint.getY() * weight[i];
            totalWeight += weight[i];
        }
        outX = (float)(topX / totalWeight);
        outY =  (float)(topY / totalWeight);

        outX += Xprime.getX();
        outY += Xprime.getY();

        return new Point( outX, outY );
    }

    private Point interPoint(Point src, Point dest, int i, int max){
        int tempX, tempY;
        tempX = (int)(src.getX() + (i / max) * (dest.getX() - src.getX()));
        tempY = (int)(src.getY() + (i / max) * (dest.getY() - src.getY()));
        return new Point(tempX, tempY); // point on the line to create a vector
    }
}