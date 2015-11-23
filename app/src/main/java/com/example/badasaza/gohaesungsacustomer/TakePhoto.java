package com.example.badasaza.gohaesungsacustomer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePhoto extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_IMAGE_CAPTURE_FRONT = 1;
    public static final int REQUEST_IMAGE_CAPTURE_SIDE = 2;

    private LinearLayout fc;
    private LinearLayout sc;
    private ImageView imV;
    private String front;
    private String side1;
    private String side2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fc = (LinearLayout) findViewById(R.id.front_container);
        sc = (LinearLayout) findViewById(R.id.side_container);
        fc.addView(addPlus(REQUEST_IMAGE_CAPTURE_FRONT));
        sc.addView(addPlus(REQUEST_IMAGE_CAPTURE_SIDE));

        Button cancel = (Button) findViewById(R.id.cancel_button);
        Button save = (Button) findViewById(R.id.save_button);

        cancel.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_take_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    private View addPlus(int code){
        imV = new ImageView(this);
        imV.setImageResource(R.drawable.plus_button);
        imV.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imV.setOnClickListener(this);
        imV.setTag(new Integer(code));
        return imV;
    }

    private void delPlus(LinearLayout ll){
        View remView = ll.getChildAt(ll.getChildCount() - 1);
        ll.removeView(remView);
    }

    public void onClick(View v){
        switch (v.getId()) {
            case R.id.save_button:
                /* Do picture transfer here*/
                delTempImages();
                finish();
                break;
            case R.id.cancel_button:
                delTempImages();
                finish();
                break;
            default:
                dispatchTakePictureIntent(v);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE_FRONT) {
                delPlus(fc);
                fc.addView(addImage(setPic(front)));
            }else if (requestCode== REQUEST_IMAGE_CAPTURE_SIDE){
                delPlus(sc);
                if(sc.getChildCount()<1) {
                    sc.addView(addImage(setPic(side1)));
                    sc.addView(addPlus(REQUEST_IMAGE_CAPTURE_SIDE));
                }else{
                    sc.addView(addImage(setPic(side2)));
                }
            }
        }
    }

    private View addImage(Bitmap res){
        ImageView temp = new ImageView(this);
        temp.setImageBitmap(res);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int leftMargin = Math.round( 8 * this.getResources().getDisplayMetrics().density);
        lp.setMargins(0, 0, leftMargin, 0);
        temp.setLayoutParams(lp);
        return temp;
    }

    private File createImageFile(int rc) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        if (rc == 0)
            front = image.getAbsolutePath();
        else if (rc == 1)
            side1 = image.getAbsolutePath();
        else if (rc == 2)
            side2 = image.getAbsolutePath();
        return image;
    }

    private Bitmap setPic(String path) {
        // Get the dimensions of the View
        int targetW = 256;
        int targetH = 256;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return bitmap;
    }

    private void dispatchTakePictureIntent(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                int decodeCode = (Integer) v.getTag();
                if(decodeCode ==REQUEST_IMAGE_CAPTURE_FRONT)
                    photoFile = createImageFile(0);
                else if (decodeCode == REQUEST_IMAGE_CAPTURE_SIDE){
                    if(sc.getChildCount() == 1)
                        photoFile = createImageFile(1);
                    else
                        photoFile = createImageFile(2);
                }
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Error: ", "can't create directory");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, (Integer) v.getTag());
            }
        }
    }

    private void delTempImages(){
        File f = null;
        if(front!= null) {
            f = new File(front);
            f.delete();
        }
        if(side1 != null) {
            f = new File(side1);
            f.delete();
        }
        if(side2 != null) {
            f = new File(side2);
            f.delete();
        }
    }
}
