package ca.spollock.morphing;

import android.graphics.Bitmap;

// This will be called inside of the main activity
public class WarpImage{
    private LineController lc;
    private int firstImgPixels[], secondImgPixels[];
    private Bitmap firstBm, secondBm, finalBmLeft, finalBmRight;

    public WarpImage(LineController controller, Bitmap first, Bitmap second){
        lc = controller;
        lc.calculateVectors(); // initializes vectors everytime we warp

        firstImgPixels = new int[(first.getWidth() * first.getHeight())];
        secondImgPixels = new int[(second.getWidth() * second.getHeight())];
        firstBm = first;
        secondBm = second;
        getImgPixels(first, second);
        finalBmLeft = Bitmap.createBitmap(first.getWidth(), first.getHeight(), first.getConfig());
        finalBmRight = Bitmap.createBitmap(second.getWidth(), second.getHeight(), second.getConfig());
    }

    private void getImgPixels(Bitmap first, Bitmap second){
        first.getPixels(firstImgPixels, 0, first.getWidth(), 0, 0, first.getWidth(),
                first.getHeight());
        second.getPixels(secondImgPixels, 0, second.getWidth(), 0, 0, second.getWidth(),
                second.getHeight());
    }

    public void leftWarping(){
        for(int x = 0; x < firstBm.getWidth(); x++){
            for(int y = 0; y < firstBm.getHeight(); y++){
                // now for each line on the image
                Point points[] = new Point[lc.secondCanvas.size()];
                double[] weights = new double[lc.secondCanvas.size()];
                for(int lv = 0; lv < lc.secondCanvas.size(); lv++){
                    float Px = lc.secondCanvas.get(lv).start.getX(),
                            Py = lc.secondCanvas.get(lv).start.getY();
                    Point Pprime = new Point(lc.secondCanvas.get(lv).start.getX(),
                            lc.secondCanvas.get(lv).start.getY());
                    Vector PQ = lc.secondCanvasVectors.get(lv), // gives me the line vector of PQ on second canvas. This is the drawn line
                            XP = new Vector((Px - x), (Py - y)),
                            PX = new Vector((x - Px), (y - Py)), // inverse of XP
                            PQnormal = PQ.getNormal();
                    double distance = project(PQnormal, XP);
                    double fraction = project(PQ, PX);
                    double percent = fractionalPercentage(fraction, PQ);
                    points[lv] = newPoint(Pprime, percent, distance, lc.firstCanvasVectors.get(lv),
                            lc.firstCanvasVectors.get(lv).getNormal());
                    weights[lv] = weight(distance);
                }
                // get the origin point of pixel(x) from the first img
                Point newPoint = sumWeights(weights, new Point(x,y), points);
                newPoint.setX(x + newPoint.getX());
                newPoint.setY(y + newPoint.getY());
                // set pixels
                int tempX = (int)newPoint.getX(), tempY = (int)newPoint.getY();
                int outX, outY;
                int w = firstBm.getWidth(), h = firstBm.getHeight();

                if(tempX >= 0 && tempX < w){
                    outX = tempX;
                }else if(tempX < 0){
                    outX = 0;
                }else{
                    outX = w;
                }
                if(tempY >= 0 && tempY < h){
                    outY = tempY;
                }else if(tempY < 0){
                    outY = 0;
                }else{
                    outY = h;
                }
                if(outX + (outY * firstBm.getWidth()) < firstImgPixels.length)
                    finalBmLeft.setPixel(x, y, firstImgPixels[outX + (outY * firstBm.getWidth())]);
            }
        }
    }

    public void leftWarping(int i, int frames){
        for(int x = 0; x < firstBm.getWidth(); x++){
            for(int y = 0; y < firstBm.getHeight(); y++){
                // now for each line on the image
                Point points[] = new Point[lc.secondCanvas.size()];
                double[] weights = new double[lc.secondCanvas.size()];
                for(int lv = 0; lv < lc.secondCanvas.size(); lv++){
                    float Px = lc.secondCanvas.get(lv).start.getX(),
                            Py = lc.secondCanvas.get(lv).start.getY();
                    Point Pprime = new Point(lc.secondCanvas.get(lv).start.getX(),
                            lc.secondCanvas.get(lv).start.getY());
                    //PQ = lc.secondCanvasVectors.get(lv), // gives me the line vector of PQ on second canvas. This is the drawn line
                    // get the percentage along the line of x->xprime & y->yprime

                    Point starts = interPoint(lc.firstCanvas.get(lv).start, lc.secondCanvas.get(lv).start, i, frames),
                            ends = interPoint(lc.firstCanvas.get(lv).end, lc.secondCanvas.get(lv).end, i, frames);
                    Vector PQ = new Vector((starts.getX() - ends.getX()) , (starts.getY() - ends.getY()));

                    Vector XP = new Vector((Px - x), (Py - y)),
                           PX = new Vector((x - Px), (y - Py)), // inverse of XP
                           PQnormal = PQ.getNormal();
                    double distance = project(PQnormal, XP);
                    double fraction = project(PQ, PX);
                    double percent = fractionalPercentage(fraction, PQ);
                    points[lv] = newPoint(Pprime, percent, distance, lc.firstCanvasVectors.get(lv),
                            lc.firstCanvasVectors.get(lv).getNormal());
                    weights[lv] = weight(distance);
                }
                // get the origin point of pixel(x) from the first img
                Point newPoint = sumWeights(weights, new Point(x,y), points);
                newPoint.setX(x + newPoint.getX());
                newPoint.setY(y + newPoint.getY());
                // set pixels
                int tempX = (int)newPoint.getX(), tempY = (int)newPoint.getY();
                int outX, outY;
                int w = firstBm.getWidth(), h = firstBm.getHeight();

                if(tempX >= 0 && tempX < w){
                    outX = tempX;
                }else if(tempX < 0){
                    outX = 0;
                }else{
                    outX = w;
                }
                if(tempY >= 0 && tempY < h){
                    outY = tempY;
                }else if(tempY < 0){
                    outY = 0;
                }else{
                    outY = h;
                }
                if(outX + (outY * firstBm.getWidth()) < firstImgPixels.length)
                    finalBmLeft.setPixel(x, y, firstImgPixels[outX + (outY * firstBm.getWidth())]);
            }
        }
    }

    public void rightWarping(){
        for(int x = 0; x < secondBm.getWidth(); x++){
            for(int y = 0; y < secondBm.getHeight(); y++){
                // now for each line on the image
                Point points[] = new Point[lc.firstCanvas.size()];
                double[] weights = new double[lc.firstCanvas.size()];
                for(int lv = 0; lv < lc.firstCanvas.size(); lv++){
                    float Px = lc.firstCanvas.get(lv).start.getX(),
                            Py = lc.firstCanvas.get(lv).start.getY();
                    Point Pprime = new Point(lc.firstCanvas.get(lv).start.getX(),
                            lc.firstCanvas.get(lv).start.getY());
                    Vector PQ = lc.firstCanvasVectors.get(lv), // gives me the line vector of PQ on second canvas. This is the drawn line
                            XP = new Vector((Px - x), (Py - y)),
                            PX = new Vector((x - Px), (y - Py)), // inverse of XP
                            PQnormal = PQ.getNormal();
                    double distance = project(PQnormal, XP);
                    double fraction = project(PQ, PX);
                    double percent = fractionalPercentage(fraction, PQ);
                    points[lv] = newPoint(Pprime, percent, distance, lc.secondCanvasVectors.get(lv),
                            lc.secondCanvasVectors.get(lv).getNormal());
                    weights[lv] = weight(distance);
                }
                // get the origin point of pixel(x) from the first img
                Point newPoint = sumWeights(weights, new Point(x,y), points);
                newPoint.setX(x + newPoint.getX());
                newPoint.setY(y + newPoint.getY());
                // set pixels
                int tempX = (int)newPoint.getX(), tempY = (int)newPoint.getY();
                int outX, outY;
                int w = secondBm.getWidth(), h = secondBm.getHeight();

                if(tempX >= 0 && tempX < w){
                    outX = tempX;
                }else if(tempX < 0){
                    outX = 0;
                }else{
                    outX = w;
                }
                if(tempY >= 0 && tempY < h){
                    outY = tempY;
                }else if(tempY < 0){
                    outY = 0;
                }else{
                    outY = h;
                }
                if(outX + (outY * secondBm.getWidth()) < secondImgPixels.length)
                    finalBmRight.setPixel(x, y, secondImgPixels[outX + (outY * secondBm.getWidth())]);
            }
        }
    }

    public void rightWarping(int i, int frames){
        for(int x = 0; x < secondBm.getWidth(); x++){
            for(int y = 0; y < secondBm.getHeight(); y++){
                // now for each line on the image
                Point points[] = new Point[lc.firstCanvas.size()];
                double[] weights = new double[lc.firstCanvas.size()];
                for(int lv = 0; lv < lc.firstCanvas.size(); lv++){
                    float Px = lc.firstCanvas.get(lv).start.getX(),
                            Py = lc.firstCanvas.get(lv).start.getY();
                    Point Pprime = new Point(lc.firstCanvas.get(lv).start.getX(),
                            lc.firstCanvas.get(lv).start.getY());
//                    Vector PQ = lc.firstCanvasVectors.get(lv), // gives me the line vector of PQ on second canvas. This is the drawn line

                    Point starts = interPoint(lc.secondCanvas.get(lv).start, lc.firstCanvas.get(lv).start, i, frames),
                            ends = interPoint(lc.secondCanvas.get(lv).end, lc.firstCanvas.get(lv).end, i, frames);
                    Vector PQ = new Vector((starts.getX() - ends.getX()) , (starts.getY() - ends.getY()));

                    Vector XP = new Vector((Px - x), (Py - y)),
                            PX = new Vector((x - Px), (y - Py)), // inverse of XP
                            PQnormal = PQ.getNormal();
                    double distance = project(PQnormal, XP);
                    double fraction = project(PQ, PX);
                    double percent = fractionalPercentage(fraction, PQ);
                    points[lv] = newPoint(Pprime, percent, distance, lc.secondCanvasVectors.get(lv),
                            lc.secondCanvasVectors.get(lv).getNormal());
                    weights[lv] = weight(distance);
                }
                // get the origin point of pixel(x) from the first img
                Point newPoint = sumWeights(weights, new Point(x,y), points);
                newPoint.setX(x + newPoint.getX());
                newPoint.setY(y + newPoint.getY());
                // set pixels
                int tempX = (int)newPoint.getX(), tempY = (int)newPoint.getY();
                int outX, outY;
                int w = secondBm.getWidth(), h = secondBm.getHeight();

                if(tempX >= 0 && tempX < w){
                    outX = tempX;
                }else if(tempX < 0){
                    outX = 0;
                }else{
                    outX = w;
                }
                if(tempY >= 0 && tempY < h){
                    outY = tempY;
                }else if(tempY < 0){
                    outY = 0;
                }else{
                    outY = h;
                }
                if(outX + (outY * secondBm.getWidth()) < secondImgPixels.length)
                    finalBmRight.setPixel(x, y, secondImgPixels[outX + (outY * secondBm.getWidth())]);
            }
        }
    }

    public Bitmap getFinalBmRight(){
        return finalBmRight;
    }

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
    private Point newPoint(Point Pprime, double percent, double distance,
                                        Vector vectorPrime, Vector normalPrime){
        float Px, Py, tempPQx, tempPQy, tempNx, tempNy;
        double normalMagnitude = calculateMagnitude(normalPrime);
        tempPQx = (float)percent * vectorPrime.getX();
        tempPQy = (float)percent * vectorPrime.getY();
        tempNx = (float)distance * (normalPrime.getX() / (float)normalMagnitude);
        tempNy = (float)distance * (normalPrime.getY() / (float)normalMagnitude);
        Px = Pprime.getX() + tempPQx;
        Py = Pprime.getY() + tempPQy;
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

    private Point changePoint(Point dest, Point found){
        return new Point((dest.getX() - found.getX()), (dest.getY() - found.getY()));
    }

    private Point sumWeights(double[] weight, Point orgPixel, Point newPositions[]){
        // list of all the weights
        double totalWeightX = 0, totalWeightY = 0, topX = 0, topY = 0;
        for(int i = 0; i < newPositions.length; i++){
            Point changePoint = changePoint(orgPixel, newPositions[i]);
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