package ca.spollock.morphing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private boolean firstImageSelected = true;
    private boolean takePicture = false;
    private boolean selectPicture = false;
    private Context dir; // Applications context
    private FrameLayout firstFrame, secondFrame;
    private ImageView firstPic, secondPic;
    private int firstPicW, firstPicH, secondPicW, secondPicH;
    private LineController lc;
    private EditingView firstCanvas, secondCanvas;
    private File firstPicture, secondPicture;
    private int closestIndex = -1;
    private boolean drawingMode = true;
    private int framesEntered;
    private WarpImage warp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        firstPic = (ImageView) findViewById(R.id.FirstImage);
        secondPic = (ImageView) findViewById(R.id.SecondImage);
        dir = getApplicationContext();

        lc = new LineController();
        firstCanvas = new EditingView(this);
        secondCanvas = new EditingView(this);
        firstCanvas.viewIndex(0);
        secondCanvas.viewIndex(1);
        firstCanvas.init(lc);
        secondCanvas.init(lc);
        firstCanvas.setOnTouchListener(new TouchListener());
        secondCanvas.setOnTouchListener(new TouchListener());
        firstFrame = (FrameLayout) findViewById(R.id.firstFrame);
        secondFrame = (FrameLayout) findViewById(R.id.secondFrame);
        firstFrame.addView(firstCanvas);
        secondFrame.addView(secondCanvas);
        firstPicW = firstPic.getWidth();
        firstPicH = firstPic.getHeight();
        secondPicW = secondPic.getWidth();
        secondPicH = secondPic.getHeight();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // switch for the item menu selected
        switch(id){
            case R.id.action_draw:
                drawingMode();
                break;
            case R.id.action_edit:
                editMode();
                break;
            case R.id.action_undo:
                removeLastLine();
                return true;
            case R.id.action_settings:
                displayTempDialog("helping...");
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_camera:
                displayImageDialog("Replace First or Second Image?");
                break;
            case R.id.action_gallery:
                dialogSelectImage("Replace First or Second Image?");
                break;
            case R.id.action_new:
                displayClearDialog("Do you want to clear your images and start over?");
                break;
            case R.id.action_save:
                displayTempDialog("saved");
                saveSession();
                break;
            case R.id.action_load:
                loadSession();
                break;
            case R.id.action_draw:
                drawingMode();
                break;
            case R.id.action_morph:
                dialogEnterFrames();
                break;
            case R.id.action_edit:
                editMode();
                break;
            case R.id.action_clearLines:
                removeLines();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        else if (resultCode == RESULT_OK && selectPicture) {
            Uri selectedImageUri = data.getData();
            String selectedImagePath = selectedImageUri.getPath();
            Bitmap bm = BitmapFactory.decodeFile(selectedImagePath);
            if(firstImageSelected) {
//                setPhoto(selectedImageUri);
                firstPic.setImageURI(selectedImageUri);
                firstPicture = new File(selectedImagePath);
            }
            else {
//                setPhoto(selectedImageUri);
                secondPic.setImageURI(selectedImageUri);
                secondPicture = new File(selectedImagePath);
            }
        }
        takePicture = false;
        selectPicture = false;
    }

    private class TouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            EditingView temp = (EditingView)v;
            if(drawingMode) {
                // drawing mode
                temp.drawLine(event);
                updateCanvas();
            }else{
                // edit mode
                int lineIndex = temp.editLine(event);
                firstCanvas.showEditing(lineIndex);
                secondCanvas.showEditing(lineIndex);
                updateCanvas();
            }
            return true;
        }
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

    private void displayClearDialog(String Message){
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
                removeLines();
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

    public void dialogEnterFrames(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.title_enter_frames);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);
        alert.setPositiveButton((R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Close
                dialog.cancel();
            }
        });
        alert.setNegativeButton((R.string.dialog_done), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                framesEntered = Integer.parseInt(input.getText().toString());
                morphImages(framesEntered);
            }
        });
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

    private void setPhoto(Uri photoUri){
        if(firstImageSelected){
            Bitmap bitmap = BitmapFactory.decodeFile(photoUri.getPath());
            bitmap = Bitmap.createScaledBitmap(bitmap, firstPic.getWidth(),
                    firstPic.getHeight(), false);
            firstPic.setImageBitmap(bitmap);
        }else{
            Bitmap bitmap = BitmapFactory.decodeFile(photoUri.getPath());
            bitmap = Bitmap.createScaledBitmap(bitmap, secondPic.getWidth(),
                    secondPic.getHeight(), false);
            secondPic.setImageBitmap(bitmap);
        }
    }

    private void saveSession(){
        // Get the image name
        File rightSave = new File(dir.getFilesDir(), "rightImage.png"); //getApplicatonContext().getFilesDir()
                                                                        //dir = context
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
            rightBitmap = Bitmap.createScaledBitmap(rightBitmap, firstPic.getWidth(),
                    firstPic.getHeight(), false);
            leftBitmap = Bitmap.createScaledBitmap(leftBitmap, secondPic.getWidth(),
                    secondPic.getHeight(), false);
            firstPic.setImageBitmap(rightBitmap);
            secondPic.setImageBitmap(leftBitmap);
        }catch(Exception e){
            displayTempDialog("No session currently saved.");
            e.printStackTrace();
        }
    }

    public void morphImages(int frames){
        if(firstPic.getDrawable() != null && secondPic.getDrawable() != null){
            // first ask how many frames you want to make (default 1)
            // warp based on the frames
            new Thread(new Runnable() {
                public void run() {
                    Bitmap first = ((BitmapDrawable)firstPic.getDrawable()).getBitmap(),
                            second = ((BitmapDrawable)secondPic.getDrawable()).getBitmap();
                    warp = new WarpImage(lc, first, second); // this will call all the functions on the warp
                }
            }).start();

            Intent morphIntent = new Intent(this, MorphDisplayActivity.class);
            morphIntent.putExtra(getString(R.string.extra_frames), framesEntered);
            startActivity(morphIntent);
        }else{
            displayTempDialog("Cannot start morph. No images to morph.");
        }
    }

    private void updateCanvas(){
        firstCanvas.invalidate();
        secondCanvas.invalidate();
    }

    private void removeLines(){
        lc.clearLists();
        firstCanvas.clear();
        secondCanvas.clear();
        firstCanvas.invalidate();
        secondCanvas.invalidate();
    }

    private void drawingMode(){
        drawingMode = true;
        firstCanvas.drawingMode();
        secondCanvas.drawingMode();
    }

    private void editMode(){
        drawingMode = false;
        firstCanvas.editMode(closestIndex);
        secondCanvas.editMode(closestIndex);
    }

    private void removeLastLine(){
        lc.removeLast();
        firstCanvas.clear();
        secondCanvas.clear();
        firstCanvas.invalidate();
        secondCanvas.invalidate();
    }
}
