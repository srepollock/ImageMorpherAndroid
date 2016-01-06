package ca.spollock.morphing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private boolean firstImageSelected = true;
    private boolean takePicture = false;
    private boolean selectPicture = false;

    private Context dir; // Applications context

    private ImageView firstPic;
    private File firstPicture;
    private ImageView secondPic;
    private File secondPicture;

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
        dir = getApplicationContext();
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
                displayQuestionDialog("Do you want to clear your images and start over?");
                return true;

            case R.id.action_save:
                displayTempDialog("saved");
                saveSession();
                return true;

            case R.id.action_load:
                loadSession();
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

    private void displayQuestionDialog(String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                firstPic.setImageResource(0);
                secondPic.setImageResource(0);
                dialog.cancel();
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
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePicture = true;
            File photo = null;
            try {
                photo = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "newImage.jpg");
            } catch (Exception e) {
                displayTempDialog("Error saving photo temp.");
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // this is for image capture with the camera
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && takePicture) {
            if(firstImageSelected) {
                File photo = null;
                try{
                    photo = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "newImage.jpg");
                    if(photo.exists()){
                        Uri photoUri = Uri.fromFile(photo);
                        setPhoto(photoUri);
                    }
                }catch (Exception e){
                    displayTempDialog("Photo not found.");
                }
            }
            else {
                File photo = null;
                try{
                    photo = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "newImage.jpg");
                    if(photo.exists()){
                        Uri photoUri = Uri.fromFile(photo);
                        setPhoto(photoUri);
                    }
                }catch (Exception e){
                    displayTempDialog("Photo not found.");
                }
            }
        }
        // this is for selection of pictures
        else if (resultCode == RESULT_OK && selectPicture) {
            Uri selectedImageUri = data.getData();
            String selectedImagePath = selectedImageUri.getPath();
            if(firstImageSelected) {
                setPhoto(selectedImageUri);
                firstPicture = new File(selectedImagePath);
            }
            else {
                setPhoto(selectedImageUri);
                secondPicture = new File(selectedImagePath);
            }
        }
        // reset
        takePicture = false;
        selectPicture = false;
    }

    private void setPhoto(Uri photoUri){
        int targetW, targetH;
        if(firstImageSelected){
            targetH = firstPic.getMaxHeight();
            targetW = firstPic.getMaxWidth();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoUri.getPath(), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(photoUri.getPath(), bmOptions);
            firstPic.setImageBitmap(bitmap);
        }else{
            targetH = secondPic.getMaxHeight();
            targetW = secondPic.getMaxWidth();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoUri.getPath(), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(photoUri.getPath(), bmOptions);
            secondPic.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features what required the permission
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void saveSession(){
        // look into output streams

        // Get the image name
        File rightSave = new File(dir.getFilesDir(), "rightImage.png");
        File leftSave = new File(dir.getFilesDir(), "leftImage.png");

        FileOutputStream rightOS = null, leftOS = null;
        try {
            rightOS = new FileOutputStream(rightSave);
            Bitmap rightBitmap = ((BitmapDrawable)firstPic.getDrawable()).getBitmap();
            rightBitmap.compress(Bitmap.CompressFormat.PNG, 100, rightOS);
            leftOS = new FileOutputStream(leftSave);
            Bitmap leftBitmap = ((BitmapDrawable)secondPic.getDrawable()).getBitmap();
            leftBitmap.compress(Bitmap.CompressFormat.PNG, 100, leftOS);
            rightOS.close();
            leftOS.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadSession(){
        try{
            File rightImage = new File(dir.getFilesDir(), "rightImage.png");
            File leftImage = new File(dir.getFilesDir(), "leftImage.png");
            Bitmap rightBitmap = BitmapFactory.decodeStream(new FileInputStream(rightImage));
            Bitmap leftBitmap = BitmapFactory.decodeStream(new FileInputStream(leftImage));
            firstPic.setImageBitmap(rightBitmap);
            secondPic.setImageBitmap(leftBitmap);
        }catch(Exception e){
            displayTempDialog("No session currently saved.");
            e.printStackTrace();
        }
    }

    public void morphImages(){
        if(firstPic.getDrawable() != null && secondPic.getDrawable() != null)
            displayTempDialog("Morphing...");
    }

    public void morphImages(View v){

    }
}
