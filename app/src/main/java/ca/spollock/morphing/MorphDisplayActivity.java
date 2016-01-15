package ca.spollock.morphing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class MorphDisplayActivity extends AppCompatActivity {

    private int totalFrames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morph_dispaly);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        totalFrames = getIntent().getIntExtra(getString(R.string.extra_frames), 1); // default of 1 frame,
        totalFrames++; // accounting for the final image, there needs to be one more
        TextView framesDisplay = (TextView)findViewById(R.id.frameDisplayText);
        framesDisplay.setText(getString(R.string.text_frames) + totalFrames);
    }

}
