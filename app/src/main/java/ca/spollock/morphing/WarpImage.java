package ca.spollock.morphing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.util.ArrayList;

public class WarpImage {
    private LineController lc;  // This is a copy from the main,
                                // this will be used to process the lines
    private Bitmap right, left;

    private ArrayList<Integer> rightStart = new ArrayList<>();
    private ArrayList<Integer> rightEnd = new ArrayList<>();
    private ArrayList<Integer> leftStart = new ArrayList<>();
    private ArrayList<Integer> leftEnd = new ArrayList<>();

    public WarpImage(LineController controller, Uri firstURI, Uri secondURI){
        lc = controller;
        BitmapFactory bmf = new BitmapFactory();
        right = bmf.decodeFile(firstURI.getPath());
        left = bmf.decodeFile(secondURI.getPath());

        getPointPixels(); // initializes everything for setup
    }

    // Gets the points from both arrays and adds them to the respective arrays
    private void getPointPixels(){
        if(!lc.firstCanvas.isEmpty()){
            for(int i = 0; i < lc.firstCanvas.size(); i++){
                // original points
                rightStart.add(right.getPixel((int) lc.firstCanvas.get(i).startX,
                        (int) lc.firstCanvas.get(i).startY));
                rightEnd.add(right.getPixel((int) lc.firstCanvas.get(i).endX,
                        (int) lc.firstCanvas.get(i).endY));

                // changed points
                leftStart.add(left.getPixel((int) lc.secondCanvas.get(i).startX,
                        (int) lc.secondCanvas.get(i).startY));
                leftEnd.add(left.getPixel((int) lc.secondCanvas.get(i).endX,
                        (int) lc.secondCanvas.get(i).endY));
            }
        }
    }


}
