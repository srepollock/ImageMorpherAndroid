package ca.spollock.morphing;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class MorphDisplayActivity extends AppCompatActivity {

    private Context dir;
    private int totalFrames;
    private Bitmap orgLeft, orgRight;
    private Bitmap[] rightWarps, leftWarps, finalMorph;
    private ImageView finalImage;
    private int imgCount = -1; // this starts the view out at the original image
    // Get button presses to then change the picture in the imageview
        // just set the image to the next one in the array using a counter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morph_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        finalImage = (ImageView)findViewById(R.id.FinalWarp);
        totalFrames = getIntent().getIntExtra(getString(R.string.extra_frames), 1);
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
            public void onClick(View v){
                // set the image view of the final image to the previous image \
                    // (when zero, go to the original left image)
                imgCount--;
                setFinalImageView();
            }
        });
        // load images
        loadBitmaps();
        // now that we have the images we need to cross dissolve
        crossDissolve();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            // delete all the pictures as well
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop(){
        super.onStop();
        // This is where I should clear the saved images
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

    // Should take out for testing purposes
    /*
    @Override
    protected void onDestroy(){
        super.onDestroy();
        // This is where I should clear the saved images
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
    */

    // Load the bitmaps
    private void loadBitmaps(){
        rightWarps = new Bitmap[totalFrames];
        leftWarps = new Bitmap[totalFrames];
        try{
            File rightImage, leftImage;
            for(int i = 0; i < totalFrames; i++){
                rightImage = new File(dir.getFilesDir(), "final_right_" + i + ".png");
                leftImage = new File(dir.getFilesDir(), "final_left_" + i + ".png");
                rightWarps[i] = BitmapFactory.decodeStream(new FileInputStream(rightImage));
                leftWarps[i] = BitmapFactory.decodeStream(new FileInputStream(leftImage));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    // take the image values and set them to new image array
        // use this array later to go through the images when using the buttons
    private void crossDissolve(){
        // what do I need to do here?

    }

    private void loadOriginal(){
        try{
            File rightImage = new File(dir.getFilesDir(), getString(R.string.right_image_save));
            File leftImage = new File(dir.getFilesDir(), getString(R.string.left_image_save));
            orgRight = BitmapFactory.decodeStream(new FileInputStream(rightImage));
            orgLeft = BitmapFactory.decodeStream(new FileInputStream(leftImage));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Sets the image of the image view based to the count
    private void setFinalImageView(){
        if(imgCount < 0){
            // set to the left image
            finalImage.setImageBitmap(orgLeft);
            imgCount = -1;
        }else if(imgCount >= leftWarps.length) { // change to final
            // set to the right image
            finalImage.setImageBitmap(orgRight);
            imgCount = leftWarps.length; // change to final
        }else{
            // set to whatever number the image is
            finalImage.setImageBitmap(leftWarps[imgCount]); // change to final
        }
    }
}
