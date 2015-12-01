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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        login.setOnClickListener(this);
        signUp.setOnClickListener(this);

        getSupportActionBar().hide();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean permissionGranted = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE )== PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.login_button) {
            lt = new LoginTask();
            final String str = et.getText().toString();
            if(str != null)
                lt.execute(str);
            final AppCompatActivity a = this;
            final ProgressDialog pd = ProgressDialog.show(this, getText(R.string.signup_wait_title), getText(R.string.signup_wait_text), true, false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!taskFinished() && !lt.isCancelled()){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    pd.dismiss();
                    if(lt.isCancelled()){
                        Log.i(DEBUG_TAG, "in task cancelled");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder ab = new AlertDialog.Builder(cxt);
                                ab.setTitle(R.string.error).setMessage(R.string.server_connection_error).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                ab.create().show();
                            }
                        });
                    }else if(taskFinished()) {
                        int res = lt.resultCode;
                        Log.i(DEBUG_TAG, "in task finished" + res);
                        if (res == 1) {
                            Intent i = new Intent(a, CustomerHome.class);
                            i.putExtra(IDCODE, str);
                            i.putExtra(ALBUMNUM, res);
                            startActivity(i);
                            finish();
                        } else if (res == -2) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder ab = new AlertDialog.Builder(cxt);
                                    ab.setTitle(R.string.error).setMessage(R.string.invalid_idcode);
                                    ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    ab.create().show();
                                }
                            });
                        } else if (res == -1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder ab = new AlertDialog.Builder(cxt);
                                    ab.setTitle(R.string.error).setMessage(R.string.server_internal_error);
                                    ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    ab.create().show();
                                }
                            });
                        }
                    }else { Log.i(DEBUG_TAG, "out of if");}
                }
            }).start();

        }else if(v.getId() == R.id.signup_button){
            Intent i = new Intent(this, SignUpAct.class);
            startActivity(i);
        }
    }

    public boolean taskFinished(){
        if(lt.getStatus() == AsyncTask.Status.FINISHED)
            return true;
        return false;
    }

    private class LoginTask extends AsyncTask<String, Void, String>{

        public int resultCode;

        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 10;

            String postData = "idcode="+params[0];

            /* ToDo: consider while loop here */
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

            }catch(SocketTimeoutException e){
                Log.e(DEBUG_TAG, "Socket Timeout Exception");
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
            str = str.replaceAll("[^\\d.]", "");
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                resultCode = Integer.parseInt(s);
            }catch(NumberFormatException e){
                resultCode = 0;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            resultCode = 99;
        }
    }
}
