package com.example.badasaza.gohaesungsacustomer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
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

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class TakePhoto extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_IMAGE_CAPTURE_FRONT = 1;
    public static final int REQUEST_IMAGE_CAPTURE_SIDE = 2;
    public static final String FILE_PATHS= "fp";
    public static final String DATE_STRING = "ds";

    private LinearLayout fc;
    private LinearLayout sc;
    private ImageView imV;
    private String[] filePaths = new String[3];
    private String[] fileNames = new String[3];
    private final String DEBUG_TAG = "TakePhoto";
    private UploadTask ut;
    private String idcode;
    private int count;
    private ProgressDialog pd;

    /* ToDo: optional: save 1/2 and send 1/4 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idcode = getIntent().getStringExtra(LoginAct.IDCODE);
        count = getIntent().getIntExtra(CustomerHome.ALBUMNUM_REQUEST, -1);

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
        }else if (id == android.R.id.home) {
            this.setResult(RESULT_CANCELED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();

        if(pd!= null)
            pd.dismiss();
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
                if(filePaths[0] != null && filePaths[1] != null && filePaths[2] !=null) {
                    /* Check: write to log file */
                    ut = new UploadTask();
                    ut.execute(idcode, fileNames[0], fileNames[1], fileNames[2], filePaths[0], filePaths[1], filePaths[2]);
                    pd = ProgressDialog.show(this, getText(R.string.signup_wait_title), getText(R.string.signup_wait_text), true, false);
                    new Thread(new FRunnable()).start();
                    Date now = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Intent b = new Intent();
                    ArrayList<String> dummy = new ArrayList<>();
                    dummy.addAll(Arrays.asList(filePaths));
                    b.putStringArrayListExtra(FILE_PATHS, dummy);
                    b.putExtra(DATE_STRING, sdf.format(now));
                    this.setResult(RESULT_OK, b);
                    finish();
                }else{
                    /* ToDo: make it 2 instead of 3 */

                }
                break;
            case R.id.cancel_button:
                this.setResult(RESULT_CANCELED);
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
                compressSave(filePaths[0]);
                fc.addView(addImage(setPic(filePaths[0])));
            }else if (requestCode== REQUEST_IMAGE_CAPTURE_SIDE){
                delPlus(sc);
                if(sc.getChildCount()<1) {
                    compressSave(filePaths[1]);
                    sc.addView(addImage(setPic(filePaths[1])));
                    sc.addView(addPlus(REQUEST_IMAGE_CAPTURE_SIDE));
                }else{
                    compressSave(filePaths[2]);
                    sc.addView(addImage(setPic(filePaths[2])));
                }
            }
        }
    }

    private View addImage(Bitmap res){
        ImageView temp = new ImageView(this);
        temp.setImageBitmap(res);        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int leftMargin = Math.round(8 * this.getResources().getDisplayMetrics().density);
        lp.setMargins(0, 0, leftMargin, 0);
        temp.setLayoutParams(lp);
        return temp;
    }

    private File createImageFile(int rc) throws IOException {
        String imageFileName = idcode+"_"+(count+1)+"_";
        switch(rc){
            case 0:
                imageFileName += "h.jpg";
                break;
            case 1:
                imageFileName += "s1.jpg";
                break;
            case 2:
                imageFileName += "s2.jpg";
                break;
            default:
                break;
        }
        File storageDir = Environment.getExternalStorageDirectory();


        /* ToDo: optional: runtime permissions for Android 6.0+ */
        File image = new File(storageDir, "GHSS/Image/"+imageFileName);
        Log.d(DEBUG_TAG, image.getAbsolutePath());
        try{
            if(!image.createNewFile())
                Log.d(DEBUG_TAG, image.getName()+ "file already exists");
        }catch(IOException e){
            Log.d(DEBUG_TAG, "file ioexception");
        }

        // Save a file: path for use with ACTION_VIEW intents
        filePaths[rc] = image.getAbsolutePath();
        fileNames[rc] = image.getName();
        return image;
    }

    private Bitmap setPic(String path) {
        int targetW = 256;
        int targetH = 256;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.max(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return bitmap;
    }

    private void compressSave(String path){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 4;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            } catch (IOException e) {
                // Error occurred while creating the File
                Log.e("Error: ", "can't create directory");
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, (Integer) v.getTag());
            }
        }
    }

    private void delTempImages(){
        File f = null;
        for(String s : filePaths){
            if (s != null){
                f = new File(s);
                f.delete();
            }
        }
    }

    private AlertDialog.Builder quickBuilder(int titleId, int contentId, AlertDialog.OnClickListener ocl){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(titleId).setMessage(contentId).setPositiveButton(R.string.ok, ocl);
        return ab;
    }

    private class UploadTask extends AsyncTask<String, Void, String>{
        public String result;

        private int response;
        /* ToDo: consider while loop here */
        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            int len = 10;

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";
            File f1 = new File(params[4]);
            File f2 = new File(params[5]);
            File f3 = new File(params[6]);
            FileInputStream fis;
            byte[] front = new byte[(int) f1.length()];
            byte[] side1 = new byte[(int) f2.length()];
            byte[] side2 = new byte[(int) f3.length()];

            try {
                fis = new FileInputStream(f1);
                fis.read(front);
                fis = new FileInputStream(f2);
                fis.read(side1);
                fis = new FileInputStream(f3);
                fis.read(side2);
                fis.close();
                URL url = new URL("http://143.248.57.222:80/upload");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(30000);
                conn.setConnectTimeout(30000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                DataOutputStream output = new DataOutputStream(conn.getOutputStream());
                output.writeBytes(twoHyphens + boundary + lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"idcode\"" + lineEnd);
                output.writeBytes(lineEnd);
                output.write(params[0].getBytes("euc-kr"));
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"pathname1\"" + lineEnd);
                output.writeBytes(lineEnd);
                output.write(params[1].getBytes("euc-kr"));
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"pathname2\"" + lineEnd);
                output.writeBytes(lineEnd);
                output.write(params[2].getBytes("euc-kr"));
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"pathname3\"" + lineEnd);
                output.writeBytes(lineEnd);
                output.write(params[3].getBytes("euc-kr"));
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"file1\";filename=\"" + f1.getName()+"\"" + lineEnd);
                output.writeBytes(lineEnd);

                int bufferLength = 1024;
                for (int i = 0; i < front.length; i += bufferLength) {

                    if (front.length - i >= bufferLength) {
                        output.write(front, i, bufferLength);
                    } else {
                        output.write(front, i, front.length - i);
                    }
                }
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary +  lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"file2\";filename=\"" + f2.getName()+"\"" + lineEnd);
                output.writeBytes(lineEnd);

                for (int i = 0; i < side1.length; i += bufferLength) {

                    if (side1.length - i >= bufferLength) {
                        output.write(side1, i, bufferLength);
                    } else {
                        output.write(side1, i, side1.length - i);
                    }
                }
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary +  lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"file3\";filename=\"" + f3.getName()+"\"" + lineEnd);
                output.writeBytes(lineEnd);

                for (int i = 0; i < side2.length; i += bufferLength) {

                    if (side2.length - i >= bufferLength) {
                        output.write(side2, i, bufferLength);
                    } else {
                        output.write(side2, i, side2.length - i);
                    }
                }
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                output.flush();
                output.close();

                conn.connect();
                response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();


                String contentAsString = readIt(is, len);
                return contentAsString;

            }catch(SocketTimeoutException e){
                Log.i(DEBUG_TAG, "Socket Timeout Exception");
                cancel(true);
                return null;
            }
            catch(IOException e){e.printStackTrace();}
            finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        private String readIt(InputStream stream, int len) throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "euc-kr");
            char[] buffer = new char[len];
            reader.read(buffer);
            String str = new String(buffer);
            if(str.charAt(0) == '-') {
                str = str.replaceAll("[^\\d.]", "");
                str = "-" + str;
            }else
                str = str.replaceAll("[^\\d.]", "");
            return str;
        }

        @Override
        protected void onPostExecute(String str){
            /* Note : Temporarily pass error code 500 here */
            result = (str == null ? "nothing here!" : str);
            Log.i(DEBUG_TAG, result);
            if((str != null && str.charAt(0) != '-') || response == 500) {
                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                File temp = new File(Environment.getExternalStorageDirectory(), "GHSS/Users/" + idcode + ".txt");
                Log.i(DEBUG_TAG, "writing to file " + temp.getAbsolutePath());
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(temp, true));
                    bw.write((count + 1) + "/" + sdf.format(now));
                    bw.newLine();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FRunnable implements Runnable{

        @Override
        public void run() {

            while (ut.getStatus() != AsyncTask.Status.FINISHED && !ut.isCancelled())
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            pd.dismiss();
            if (ut.isCancelled()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder ab = new AlertDialog.Builder(getApplicationContext());
                        ab.setTitle(R.string.error).setMessage(R.string.server_connection_error).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ab.create().show();
                    }
                });
            } else if (ut.result.matches("-1")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        quickBuilder(R.string.error, R.string.server_internal_error, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });
            } else if (ut.result.matches("-2")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        quickBuilder(R.string.error, R.string.upload_failed, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });
            }
        }
    }

}
