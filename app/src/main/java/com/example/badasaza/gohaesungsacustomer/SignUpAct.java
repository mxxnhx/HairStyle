package com.example.badasaza.gohaesungsacustomer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.badasaza.gohaesungsaview.SignUpHomeFrag;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class SignUpAct extends AppCompatActivity{

    private SignUpTask sut;
    private Context cxt = this;

    protected String DEBUG_TAG = "SignUpAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setTitle(R.string.signup_title);

        FragmentManager fm = getSupportFragmentManager();
        SignUpHomeFrag suhf = new SignUpHomeFrag();
        fm.beginTransaction().add(R.id.signup_frag_container, suhf).addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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

    public void initTask(String... param){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            sut = new SignUpTask();
            sut.execute(param);
        }else
            Log.e(DEBUG_TAG, "internet not connected");
    }

    public boolean taskFinished(){
        if(sut.getStatus() == AsyncTask.Status.FINISHED)
            return true;
        return false;
    }

    public String getIdcode(){
        if(sut.result == null)
            return "";
        else if(sut.result.matches("-2"))
            return "tel";
        else if(sut.result.matches("-1"))
            return "internal";
        return sut.result;
    }

    public void notifyFinished(){
        /* ToDo: take care of error cases */
        final ProgressDialog pd = ProgressDialog.show(this, getText(R.string.signup_wait_title), getText(R.string.signup_wait_text), true, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!taskFinished() && !sut.isCancelled())
                    try {
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                pd.dismiss();
                if(sut.isCancelled()){
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder ab = new AlertDialog.Builder(cxt);
                            ab.setTitle(R.string.signup_idcode_title).setMessage(getText(R.string.signup_idcode_text1) + getIdcode() + getText(R.string.signup_idcode_text2));
                            ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                            ab.create().show();
                        }
                    });
                }
            }
        }).start();
    }

    private class SignUpTask extends AsyncTask<String, Void, String> {

        public String result;
        /* ToDo: consider while loop here */
        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 10;

            String postData = "name="+params[0]+"&tel="+params[1];

            try {
                URL url = new URL("http://143.248.57.222:80/signup");
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
            str = str.replaceAll("[^\\d.]", "");
            return str;
        }

        @Override
        protected void onPostExecute(String str){
            Log.i(DEBUG_TAG, str);
            result = str;
        }
    }
}
