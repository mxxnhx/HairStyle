package com.example.badasaza.gohaesungsaview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.badasaza.gohaesungsacustomer.R;
import com.example.badasaza.gohaesungsacustomer.SignUpAct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Badasaza on 2015-12-01.
 */
public class SignUpWarnFrag extends Fragment implements View.OnClickListener {

    private final String DEBUG_TAG="SignUpWarnFrag";
    private int FACE_REQUIREMENT=5;
    public static final String NAME_KEY="name!";
    public static final String TEL_KEY="telephone!";

    private File photoFile;
    private String name;
    private String tel;
    public String loc;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_sign_up_warning, container ,false);
        Button btn = (Button) rootView.findViewById(R.id.signup_to_next3);
        btn.setOnClickListener(this);
        Bundle args = getArguments();
        name = args.getString(NAME_KEY, null);
        tel = args.getString(TEL_KEY, null);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        try{
            File f = new File(Environment.getExternalStorageDirectory(), "GHSS/Image");
            if(!f.exists()){
                if(!f.mkdirs())
                    Log.e(DEBUG_TAG, "can't create directory");
            }
            photoFile = File.createTempFile(imageFileName, ".jpg", f);
            loc = photoFile.getAbsolutePath();
        }catch(IOException e){
            Log.e(DEBUG_TAG, "can't create file");
        }
        if(photoFile != null){
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(photoIntent, FACE_REQUIREMENT);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == FACE_REQUIREMENT && resultCode == Activity.RESULT_OK){
            setPic(loc);
            SignUpAct sua = (SignUpAct) getActivity();
            Log.i("SignUpWarnFrag", loc);
            sua.initTask(name, tel, loc);
            sua.notifyFinished();
        }
    }

    private void setPic(String path) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 4;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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

}
