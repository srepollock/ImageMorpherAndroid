package ca.spollock.morphing;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

    // Load the bitmaps
    private void loadBitmaps(){
        rightWarps = new Bitmap[totalFrames];
        leftWarps = new Bitmap[totalFrames];
        try{
            File rightImage, leftImage;
            for(int i = 0; i < totalFrames; i++){
                leftImage = new File(dir.getFilesDir(), "final_left_" + i + ".png");
                rightImage = new File(dir.getFilesDir(), "final_right_" + i + ".png");
//                leftWarps[i] = BitmapFactory.decodeStream(new FileInputStream(leftImage));
//                rightWarps[i] = BitmapFactory.decodeStream(new FileInputStream(rightImage));
                leftWarps[i] = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(leftImage)), 512, 512);
                rightWarps[i] = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(rightImage)), 512, 512);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void initFinalMorph(){
        for(int i = 0; i < totalFrames; i++){
            finalMorph[i] = Bitmap.createBitmap(orgLeft.getWidth(), orgLeft.getHeight(),
                    orgLeft.getConfig());
        }
    }

    // take the image values and set them to new image array
        // use this array later to go through the images when using the buttons
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
                    int leftWeight = i, rightWeight = totalFrames - i;
                    int leftPixel = leftWarps[i].getPixel(x,y);

                    float lAlpha = Color.alpha(leftPixel) + leftWeight;
                    float lRed = Color.red(leftPixel) + leftWeight;
                    float lGreen = Color.green(leftPixel) + leftWeight;
                    float lBlue = Color.blue(leftPixel) + leftWeight;

                    int rightPixel = rightWarps[i].getPixel(x,y);
                    float rAlpha = Color.alpha(rightPixel) + rightWeight;
                    float rRed = Color.red(rightPixel) + rightWeight;
                    float rGreen = Color.green(rightPixel) + rightWeight;
                    float rBlue = Color.blue(rightPixel) + rightWeight;

                    int oAlpha = (int)(lAlpha + rAlpha) / totalFrames;
                    int oRed = (int)(lRed + rRed) / totalFrames;
                    int oGreen = (int)(lGreen + rGreen) / totalFrames;
                    int oBlue = (int)(lBlue + rBlue) / totalFrames;

                    finalMorph[i].setPixel(x, y, Color.argb(oAlpha, oRed, oGreen, oBlue));
                }
            }
        }
    }

    private void loadOriginal(){
        try{
            File leftImage = new File(dir.getFilesDir(), getString(R.string.left_image_save));
            File rightImage = new File(dir.getFilesDir(), getString(R.string.right_image_save));
//            orgLeft = BitmapFactory.decodeStream(new FileInputStream(leftImage));
//            orgRight = BitmapFactory.decodeStream(new FileInputStream(rightImage));
            orgRight = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(rightImage)), 512, 512);
            orgLeft = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(leftImage)), 512, 512);
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
        }else if(imgCount >= finalMorph.length) { // change to final
            // set to the right image
            finalImage.setImageBitmap(orgRight);
            imgCount = finalMorph.length; // change to final
        }else{
            // set to whatever number the image is
//            finalImage.setImageBitmap(finalMorph[imgCount]); // This is correct
            /* HERE FOR DISPLAY */
            finalImage.setImageBitmap(leftWarps[imgCount]);
        }
    }
}
