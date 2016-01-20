package ca.spollock.morphing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

public class MorphDisplayActivity extends AppCompatActivity {

    private int totalFrames;
    private String fileString;
    private ImageView finalWarp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morph_dispaly);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        finalWarp = (ImageView)findViewById(R.id.FinalWarp);

        totalFrames = getIntent().getIntExtra(getString(R.string.extra_frames), 1); // default of 1 frame,
        totalFrames++; // accounting for the final image, there needs to be one more
        TextView framesDisplay = (TextView)findViewById(R.id.frameDisplayText);
        framesDisplay.setText(getString(R.string.text_frames) + totalFrames);

        fileString = getIntent().getStringExtra(getString(R.string.extra_image));
        File file = new File(this.getApplicationContext().getFilesDir(), "final.png");
        try {
            Bitmap finalBitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            finalWarp.setImageBitmap(finalBitmap);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
