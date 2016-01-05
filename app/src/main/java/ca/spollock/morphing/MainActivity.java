package ca.spollock.morphing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_PICTURE = 1;

    private String selectedImageOne;
    private String selectedImageTwo;
    private String mCurrentPhotoPath;
    private boolean firstImageSelected = true;
    private boolean takePicture = false;
    private boolean selectPicture = false;

    private String selectedImagePath;
    private ImageView img;

    ImageView firstPic;
    ImageView secondPic;

    Button morphButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayImageDialog("Replace First or Second Image?");
            }
        });

        firstPic = (ImageView)findViewById(R.id.FirstImage);
        secondPic = (ImageView)findViewById(R.id.SecondImage);

        morphButton = (Button)findViewById(R.id.morphButton);
        morphButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                morphImages();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // switch for the item menu selected
        switch(id){
            case R.id.action_new:
                displayTempDialog("new");
                return true;

            case R.id.action_save:
                displayTempDialog("saved");
                return true;

            case R.id.action_load:
                displayTempDialog("loading");
                return true;

            case R.id.action_takePicture:
                // open camera to take picture
                displayImageDialog("Replace First or Second Image?");
                return true;

            case R.id.action_selectImages:
                // select picture from gallery
                dialogSelectImage("Replace First or Second Image?");
                return true;

            case R.id.action_settings:
                displayTempDialog("settings");
                return true;

            // shouldn't hit this
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayTempDialog(String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_done, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void displayImageDialog(String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_second, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                firstImageSelected = false;
                dialog.cancel();
                dispatchTakePictureIntent(); // choose picture one and two
            }
        });
        builder.setNegativeButton(R.string.dialog_first, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                firstImageSelected = true;
                dialog.cancel();
                dispatchTakePictureIntent(); // choose picture one and two
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void dialogSelectImage(String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_second, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                firstImageSelected = false;
                dialog.cancel();
                dispatchSelectPictureIntent();
            }
        });
        builder.setNegativeButton(R.string.dialog_first, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                firstImageSelected = true;
                dialog.cancel();
                dispatchSelectPictureIntent();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void dispatchSelectPictureIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectPicture = true;
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePicture = true;
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // this is for image capture with the camera
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && takePicture) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if(firstImageSelected)
                firstPic.setImageBitmap(imageBitmap);
            else
                secondPic.setImageBitmap(imageBitmap);
        }
        // this is for selection of pictures
        else if (resultCode == RESULT_OK && selectPicture) {
            Uri selectedImageUri = data.getData();
            selectedImagePath = getPath(selectedImageUri);
            if(firstImageSelected)
                firstPic.setImageURI(selectedImageUri);
            else
                secondPic.setImageURI(selectedImageUri);
        }
        takePicture = false;
        selectPicture = false;
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void morphImages(){
        if(firstPic.getDrawable() != null && secondPic.getDrawable() != null)
            displayTempDialog("Morphing...");
    }
}
