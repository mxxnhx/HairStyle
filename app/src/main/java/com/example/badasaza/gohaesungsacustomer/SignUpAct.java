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

import com.example.badasaza.gohaesungsaview.SignUpFragment;
import com.example.badasaza.gohaesungsaview.SignUpHomeFrag;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        fm.beginTransaction().add(R.id.signup_frag_container, suhf).addToBackStack("SUHF").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed(){
        String s = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        if(s == "SUHF")
            finish();
        else
            super.onBackPressed();
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
        return sut.result;
    }

    /* ToDo: Optional: move to asynctask */
    public void notifyFinished(){
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
                    if(sut.result.matches("-1") || sut.result == null) {
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
                    }else if(sut.result.matches("-2")){
                        /* ToDo: Existing telephone */
                    }else if(sut.result.matches("-3")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                quickBuilder(R.string.error, R.string.signup_warning_re, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SignUpFragment suf = new SignUpFragment();
                                Bundle args = new Bundle();
                                args.putInt(SignUpFragment.PAGE_KEY, 0);
                                suf.setArguments(args);
                                getSupportFragmentManager().beginTransaction().replace(R.id.signup_frag_container, suf).addToBackStack(null).commit();
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public AlertDialog.Builder finisherDialog(){
        AlertDialog.Builder ab = new AlertDialog.Builder(cxt);
        ab.setTitle(R.string.signup_idcode_title).setMessage(getText(R.string.signup_idcode_text1) + sut.result + getText(R.string.signup_idcode_text2));
        ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        return ab;
    }

    private AlertDialog.Builder quickBuilder(int titleId, int contentId, AlertDialog.OnClickListener ocl){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(titleId).setMessage(contentId).setPositiveButton(R.string.ok, ocl);
        return ab;
    }

    private class SignUpTask extends AsyncTask<String, Void, String> {

        public String result;
        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            int len = 10;

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";
            File f = new File(params[2]);

            try {
                FileInputStream fis = new FileInputStream(f);
                byte[] bytes = new byte[(int) f.length()];
                fis.read(bytes);
                fis.close();
                URL url = new URL("http://143.248.57.222:80/signup");
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
                output.writeBytes("Content-Disposition: form-data; name=\"name\"" + lineEnd);
                output.writeBytes(lineEnd);
                output.write(params[0].getBytes("euc-kr"));
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"tel\"" + lineEnd);
                output.writeBytes(lineEnd);
                output.write(params[1].getBytes("euc-kr"));
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + lineEnd);
                Log.i("TTest", f.getName());
                output.writeBytes("Content-Disposition: form-data; name=\"face\";filename=\"" + f.getName()+"\"" + lineEnd);
                output.writeBytes(lineEnd);

                int bufferLength = 1024;
                for (int i = 0; i < bytes.length; i += bufferLength) {

                    if (bytes.length - i >= bufferLength) {
                        output.write(bytes, i, bufferLength);
                    } else {
                        output.write(bytes, i, bytes.length - i);
                    }
                }
                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
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
            if(str.charAt(0) == '-') {
                str = str.replaceAll("[^\\d.]", "");
                str = "-" + str;
            }else
                str = str.replaceAll("[^\\d.]", "");
            return str;
        }

        @Override
        protected void onPostExecute(String str){
            result = (str == null? "-1" : str);
            Log.i(DEBUG_TAG, result);
        }


    }
}
