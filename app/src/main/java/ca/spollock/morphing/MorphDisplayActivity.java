package ca.spollock.morphing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

/**
 * Activity to display the morphed images
 */
public class MorphDisplayActivity extends AppCompatActivity {

    /**
     * Application context
     */
    private Context dir;
    /**
     * Frames implemented by the user to warp over
     */
    private int totalFrames;
    /**
     * Left original image and right original image
     */
    private Bitmap orgLeft, orgRight;
    /**
     * Bitmaps of the right warp, left warp and final morph
     */
    private Bitmap[] rightWarps, leftWarps, finalMorph;
    /**
     * Image view to display the images on
     */
    private ImageView finalImage;
    /**
     * Image count to show the images on
     */
    private int imgCount = -1; // this starts the view out at the original image

    /**
     * Called when the activity is created. Initializes all the variables
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morph_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        finalImage = (ImageView)findViewById(R.id.FinalWarp);
        totalFrames = getIntent().getIntExtra(getString(R.string.extra_frames), 0);
                            // default of 1 frame,
        TextView framesDisplay = (TextView)findViewById(R.id.frameDisplayText);
        framesDisplay.setText(getString(R.string.text_frames) + totalFrames);
        dir = getApplicationContext();
        loadOriginal();
        finalImage.setImageBitmap(orgLeft);
        // setup buttons
        Button forward = (Button)findViewById(R.id.pictureRight),
                backward = (Button)findViewById(R.id.pictureLeft);
        forward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // set the background of the final image to the the next index
                // (when array.length, go to the original right image)
                imgCount++;
                setFinalImageView();
            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // set the image view of the final image to the previous image \
                // (when zero, go to the original left image)
                imgCount--;
                setFinalImageView();
            }
        });
        // load images
        loadBitmaps();
        // setup finalMorph array to empty bitmaps
        finalMorph = new Bitmap[totalFrames];
        initFinalMorph();
        // now that we have the images we need to cross dissolve
        crossDissolve();
    }

    /**
     * Checks when the back button is pressed and deletes all the images saved in the context
     * (I would change this to save the images based on the number of frames inserted for a
     * special case (0))
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            try{
                File rightImage, leftImage;
                for(int i = 0; i < totalFrames; i++){
                    rightImage = new File(dir.getFilesDir(), "final_right_" + i + ".png");
                    leftImage = new File(dir.getFilesDir(), "final_left_" + i + ".png");
                    rightImage.delete();
                    leftImage.delete();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Called when the activity is stopped
     */
    @Override
    protected void onStop(){
        super.onStop();
        try{
            File rightImage, leftImage;
            for(int i = 0; i < totalFrames; i++){
                rightImage = new File(dir.getFilesDir(), "final_right_" + i + ".png");
                leftImage = new File(dir.getFilesDir(), "final_left_" + i + ".png");
                rightImage.delete();
                leftImage.delete();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Loads all the warped images to arrays in the activity from memory
     */
    private void loadBitmaps(){
        rightWarps = new Bitmap[totalFrames];
        leftWarps = new Bitmap[totalFrames];
        try{
            File rightImage, leftImage;
            for(int i = 0; i < totalFrames; i++){
                leftImage = new File(dir.getFilesDir(), "final_left_" + i + ".png");
                rightImage = new File(dir.getFilesDir(), "final_right_" + i + ".png");
                leftWarps[i] = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(leftImage)), 512, 512);
                rightWarps[i] = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(rightImage)), 512, 512);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Initializes the array of finalmorph to empty bitmaps
     */
    void initFinalMorph(){
        for(int i = 0; i < totalFrames; i++){
            finalMorph[i] = Bitmap.createBitmap(orgLeft.getWidth(), orgLeft.getHeight(),
                    orgLeft.getConfig());
        }
    }

    /**
     * Cross dissolves the right and left warped images based on their pixel data  and adding the
     * weight of the image they are on. Example: having 4 frames, the first on the left is 1/4 while
     * the last on the right is 3/4
     */
    private void crossDissolve(){
        // what do I need to do here?
        for(int i = 0; i < totalFrames; i++){
            for(int x = 0; x < orgLeft.getWidth(); x++){
                for(int y = 0; y < orgLeft.getHeight(); y++){
                            // get pixels
                            // get individual values
                            // apply weights
                            // put new pixels inside of the finalMorph
                            // add i or frames - i to each wy
                            // then at the end, add the values together and divide by frames
                    double leftWeight = ((i + 1) / totalFrames), rightWeight = ((totalFrames - i + 1) / totalFrames);

                    int leftPixel = leftWarps[i].getPixel(x,y);
                    float lRed = Color.red(leftPixel) + (float)leftWeight;
                    float lGreen = Color.green(leftPixel) + (float)leftWeight;
                    float lBlue = Color.blue(leftPixel) + (float)leftWeight;

                    int rightPixel = rightWarps[totalFrames - i - 1].getPixel(x,y);
                    float rRed = Color.red(rightPixel) + (float)rightWeight;
                    float rGreen = Color.green(rightPixel) + (float)rightWeight;
                    float rBlue = Color.blue(rightPixel) + (float)rightWeight;

                    int oRed = (int)(lRed + rRed) / totalFrames;
                    int oGreen = (int)(lGreen + rGreen) / totalFrames;
                    int oBlue = (int)(lBlue + rBlue) / totalFrames;

                    finalMorph[i].setPixel(x, y, Color.rgb(oRed, oGreen, oBlue));
                }
            }
        }
    }

    /**
     * Loads the original left and right images from memory
     */
    private void loadOriginal(){
        try{
            File leftImage = new File(dir.getFilesDir(), getString(R.string.left_image_save));
            File rightImage = new File(dir.getFilesDir(), getString(R.string.right_image_save));
            orgRight = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(rightImage)), 512, 512);
            orgLeft = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(leftImage)), 512, 512);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Sets the image of the image view based to the count
    /**
     * Sets the image of the image view based on the count inc/dec base on the buttons below
     */
    private void setFinalImageView(){
        if(imgCount < 0){
            // set to the left image
            finalImage.setImageBitmap(orgLeft);
            imgCount = -1;
        }else if(imgCount >= finalMorph.length) { // change to final
            // set to the right image
            finalImage.setImageBitmap(orgRight);
            imgCount = finalMorph.length; // change to final
        }else{
            finalImage.setImageBitmap(finalMorph[imgCount]);
        }
    }
}
