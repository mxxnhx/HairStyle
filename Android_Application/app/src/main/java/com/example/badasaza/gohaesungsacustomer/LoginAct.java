package com.example.badasaza.gohaesungsacustomer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class LoginAct extends AppCompatActivity implements View.OnClickListener{

    public static String IDCODE = "idcode";
    public static String ALBUMNUM = "album_num";

    private LoginTask lt;
    private EditText et;
    private final Context cxt = this;
    private final int PERMISSION_REQUEST = 0;
    private ProgressDialog pd;

    protected String DEBUG_TAG = "LoginAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Toast.makeText(this, "External SD card not mounted", Toast.LENGTH_LONG).show();
        }

        Button login = (Button) findViewById(R.id.login_button);
        Button signUp = (Button) findViewById(R.id.signup_button);
        et = (EditText) findViewById(R.id.id_input);
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER)
                    login();
                return false;
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        else {
            File imageDirectory = new File(Environment.getExternalStorageDirectory(), "GHSS/Image/");
            if (!imageDirectory.exists()) {
                if (!imageDirectory.mkdirs())
                    Log.e(DEBUG_TAG, "can't create directory");
                else
                    Log.i(DEBUG_TAG, "Image directory created!");
            } else {
                Log.i(DEBUG_TAG, "Image directory exists!");
            }
        }

        login.setOnClickListener(this);
        signUp.setOnClickListener(this);

        getSupportActionBar().hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        if(requestCode == PERMISSION_REQUEST){
            if(grantResults.length > 0){
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    File imageDirectory = new File(Environment.getExternalStorageDirectory(), "GHSS/Image/");
                    if (!imageDirectory.exists()) {
                        if (!imageDirectory.mkdirs())
                            Log.e(DEBUG_TAG, "can't create directory");
                        else
                            Log.i(DEBUG_TAG, "Image directory created!");
                    } else {
                        Log.i(DEBUG_TAG, "Image directory exists!");
                    }
                }else
                    Toast.makeText(this, "Permissions not granted. App may explode anytime", Toast.LENGTH_SHORT);
            }
        }
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.login_button) {
            login();
        }else if(v.getId() == R.id.signup_button){
            Intent i = new Intent(this, SignUpAct.class);
            startActivity(i);
        }
    }

    private void login(){
        lt = new LoginTask();
        final String str = et.getText().toString();
        if(str == null || str.matches("")){
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle(R.string.error).setMessage(R.string.server_internal_error);
            ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        final AppCompatActivity a = this;
        pd = ProgressDialog.show(this, getText(R.string.signup_wait_title), getText(R.string.signup_wait_text), true, false);
        if(str != null)
            lt.execute(str);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        public int resultCode;
        private String str;

        @Override
        protected String doInBackground(String... params) {
            str = params[0];
            InputStream is = null;
            int len = 10;

            String postData = "idcode=" + params[0];

            try {
                URL url = new URL("http://143.248.57.222:80/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream output = conn.getOutputStream();
                output.write(postData.getBytes("euc-kr"));
                output.flush();
                output.close();

                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();


                String contentAsString = readIt(is, len);
                Log.i(DEBUG_TAG, contentAsString);
                return contentAsString;

            } catch (SocketTimeoutException e) {
                Log.e(DEBUG_TAG, "Socket Timeout Exception");
                cancel(true);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
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
            if (str.charAt(0) == '-') {
                str = str.replaceAll("[^\\d.]", "");
                str = "-" + str;
            } else
                str = str.replaceAll("[^\\d.]", "");
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                resultCode = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                resultCode = -1;
            }
            pd.dismiss();
            if (resultCode == -2) {
                AlertDialog.Builder ab = new AlertDialog.Builder(cxt);
                ab.setTitle(R.string.error).setMessage(R.string.invalid_idcode);
                ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ab.create().show();
            } else if (resultCode == -1) {
                AlertDialog.Builder ab = new AlertDialog.Builder(cxt);
                ab.setTitle(R.string.error).setMessage(R.string.server_internal_error);
                ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ab.create().show();
            } else{
                Intent i = new Intent(cxt, CustomerHome.class);
                i.putExtra(IDCODE, str);
                i.putExtra(ALBUMNUM, resultCode);
                startActivity(i);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            resultCode = 99;
            pd.dismiss();
            AlertDialog.Builder ab = new AlertDialog.Builder(cxt);
            ab.setTitle(R.string.error).setMessage(R.string.server_connection_error).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
    }
}
