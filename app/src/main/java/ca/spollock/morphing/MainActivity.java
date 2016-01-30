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
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private boolean firstImageSelected = true;
    private boolean takePicture = false;
    private boolean selectPicture = false;
    private Context dir; // Applications context
    private FrameLayout leftFrame, rightFrame;
    private ImageView leftPic, rightPic;

    private LineController lc;
    private EditingView leftEditing, rightEditing;
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

        leftPic = (ImageView) findViewById(R.id.LeftImage);
        rightPic = (ImageView) findViewById(R.id.RightImage);
        dir = getApplicationContext();

        lc = new LineController();
        leftEditing = new EditingView(dir);
        rightEditing = new EditingView(dir);
        leftEditing.viewIndex(0);
        rightEditing.viewIndex(1);
        leftEditing.init(lc);
        rightEditing.init(lc);
        leftEditing.setOnTouchListener(new TouchListener());
        rightEditing.setOnTouchListener(new TouchListener());
        leftFrame = (FrameLayout) findViewById(R.id.LeftFrame);
        rightFrame = (FrameLayout) findViewById(R.id.RightFrame);
        leftFrame.addView(leftEditing, 512, 512);
        rightFrame.addView(rightEditing, 512, 512);
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
    public void onResume(){
        super.onResume();
        warp = null;
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
            Bitmap bm;
            if(firstImageSelected) {
                try {
                    InputStream is = getContentResolver().openInputStream(data.getData());
                    bm = BitmapFactory.decodeStream(is);
                    is.close();
//                    Bitmap cropped = Bitmap.createBitmap(bm, ((bm.getWidth() / 2) - 600),
//                            ((bm.getHeight() / 2) - 600), 1200, 1200);
                    Bitmap cropped = ThumbnailUtils.extractThumbnail(bm, 512, 512);
                    leftPic.setImageBitmap(cropped);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else {
                try {
                    InputStream is = getContentResolver().openInputStream(data.getData());
                    bm = BitmapFactory.decodeStream(is);
                    is.close();
//                    Bitmap cropped = Bitmap.createBitmap(bm, ((bm.getWidth() / 2) - 600),
//                            ((bm.getHeight() / 2) - 600), 1200, 1200);
                    Bitmap cropped = ThumbnailUtils.extractThumbnail(bm, 512, 512);
                    rightPic.setImageBitmap(cropped);
                }catch(Exception e){
                    e.printStackTrace();
                }
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
                leftEditing.showEditing(lineIndex);
                rightEditing.showEditing(lineIndex);
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
                leftPic.setImageResource(0);
                rightPic.setImageResource(0);
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
                leftPic.setImageResource(0);
                rightPic.setImageResource(0);
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
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 512, 512);
//            bitmap = Bitmap.createScaledBitmap(bitmap, leftPic.getWidth(),
//                    leftPic.getHeight(), false);
            leftPic.setImageBitmap(bitmap);
        }else{
            Bitmap bitmap = BitmapFactory.decodeFile(photoUri.getPath());
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 512, 512);
//            bitmap = Bitmap.createScaledBitmap(bitmap, rightPic.getWidth(),
//                    rightPic.getHeight(), false);
            rightPic.setImageBitmap(bitmap);
        }
    }

    private void saveSession(){
        // Get the image name
        File rightSave = new File(dir.getFilesDir(), getString(R.string.right_image_save)); //getApplicatonContext().getFilesDir()
                                                                        //dir = context
        File leftSave = new File(dir.getFilesDir(), getString(R.string.left_image_save));
        FileOutputStream rightOS = null, leftOS = null;
        try {
            leftOS = new FileOutputStream(leftSave);
            Bitmap leftBitmap = ((BitmapDrawable)leftPic.getDrawable()).getBitmap();
            leftBitmap.compress(Bitmap.CompressFormat.PNG, 100, leftOS);
            rightOS = new FileOutputStream(rightSave);
            Bitmap rightBitmap = ((BitmapDrawable)rightPic.getDrawable()).getBitmap();
            rightBitmap.compress(Bitmap.CompressFormat.PNG, 100, rightOS);
            rightOS.close();
            leftOS.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String saveBitmap(Bitmap bm){
        File finalSave = new File(dir.getFilesDir(), "final.png");
        FileOutputStream finalOS = null;
        try {
            finalOS = new FileOutputStream(finalSave);
            bm.compress(Bitmap.CompressFormat.PNG, 100, finalOS);
            finalOS.close();
            return finalSave.getPath();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void saveBitmap(Bitmap bm, int i, String side){
        File finalSave = new File(dir.getFilesDir(), "final_" + side + "_" + i + ".png");
        FileOutputStream finalOS = null;
        try {
            finalOS = new FileOutputStream(finalSave);
            bm.compress(Bitmap.CompressFormat.PNG, 100, finalOS);
            finalOS.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadSession(){
        try{
            File rightImage = new File(dir.getFilesDir(), getString(R.string.right_image_save));
            File leftImage = new File(dir.getFilesDir(), getString(R.string.left_image_save));
//            Bitmap rightBitmap = BitmapFactory.decodeStream(new FileInputStream(rightImage));
//            Bitmap leftBitmap = BitmapFactory.decodeStream(new FileInputStream(leftImage));
            Bitmap leftBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(leftImage)), 512, 512);
            Bitmap rightBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(new FileInputStream(rightImage)), 512, 512);
            leftPic.setImageBitmap(leftBitmap);
            rightPic.setImageBitmap(rightBitmap);
        }catch(Exception e){
            displayTempDialog("No session currently saved.");
            e.printStackTrace();
        }
    }

    public void morphImages(View v){
        //its my time
    }

    public void morphImages(final int frames){
        warp = null;
        if(leftPic.getDrawable() != null && rightPic.getDrawable() != null){
            // first ask how many frames you want to make (default 1)
            // warp based on the frames

            // check if they entered 0 to just display the previously calculated frames
                // Extra feature

            final Bitmap first = ((BitmapDrawable)leftPic.getDrawable()).getBitmap(),
                    second = ((BitmapDrawable)rightPic.getDrawable()).getBitmap();
            final Intent morphIntent = new Intent(this, MorphDisplayActivity.class);
            morphIntent.putExtra(getString(R.string.extra_frames), frames);
            selectPicture = false;

            class WarpWorker extends AsyncTask<Integer, Void, Integer>{
                Bitmap[] tempLeft, tempRight;
                int tempFrames;
                // I will be the amount of frames
                protected Integer doInBackground(Integer... i){
                    tempLeft = new Bitmap[i[0]];
                    tempRight = new Bitmap[i[0]];
                    tempFrames = i[0];
                    warp = new WarpImage(lc, first, second, i[0]);
                    for(int f = 0; f < i[0]; f++){
                        int n = f + 1;
                        warp.leftWarp(n, i[0]);
                        warp.rightWarp(n, i[0]);
                    }
                    return 1;
                }
                // invoked on ui thread
                protected void onPostExecute(Integer j){
                    for(int i = 0; i < tempFrames; i++){
                        saveBitmap(warp.leftFinals[i], i, "left");
                        saveBitmap(warp.rightFinals[i], i, "right");
                    }
                    startActivity(morphIntent); // wont start activity until we are done
                }
            }

            try {
                WarpWorker worker = new WarpWorker();
                displayTempDialog("Morph is running for: " + frames + " frames. Close this dialog, " +
                        "but do not use the app.\n\nThe morph will be displayed shortly. " +
                        "\n(Press done if you would like to " +
                        "just look at the screen)");
                worker.execute(frames);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            displayTempDialog("Cannot start morph. No images to morph.");
        }
    }

    private void updateCanvas(){
        leftEditing.invalidate();
        rightEditing.invalidate();
    }

    private void removeLines(){
        lc.clearLists();
        leftEditing.clear();
        rightEditing.clear();
        leftEditing.invalidate();
        rightEditing.invalidate();
    }

    private void drawingMode(){
        drawingMode = true;
        leftEditing.drawingMode();
        rightEditing.drawingMode();
    }

    private void editMode(){
        drawingMode = false;
        leftEditing.editMode(closestIndex);
        rightEditing.editMode(closestIndex);
    }

    private void removeLastLine(){
        lc.removeLast();
        leftEditing.clear();
        rightEditing.clear();
        leftEditing.invalidate();
        rightEditing.invalidate();
    }
}
