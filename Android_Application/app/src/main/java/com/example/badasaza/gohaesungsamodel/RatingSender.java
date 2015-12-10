package com.example.badasaza.gohaesungsamodel;

import android.util.Log;
import android.widget.RatingBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Badasaza on 2015-12-03.
 */
public class RatingSender implements Runnable {

    public String result;
    public String idcode;
    public RatingBar rtb;
    public String fileName;
    public boolean isSignup;

    private final String DEBUG_TAG = "Rating Sender";

    public RatingSender(String idcode, RatingBar rtb, String fileName, boolean isSignup){
        this.idcode = idcode;
        this.rtb = rtb;
        this.fileName = fileName;
        this.isSignup = isSignup;
    }

    @Override
    public void run() {
        String sending = "idcode="+idcode+"&rate="+rtb.getRating()+"&filename="+fileName;
        InputStream is = null;
        try {
            URL url;
            if(isSignup)
                url = new URL("http://143.248.57.222:80/rating_signup");
            else
                url = new URL("http://143.248.57.222:80/rating_rec");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            OutputStream output = conn.getOutputStream();
            output.write(sending.getBytes("euc-kr"));
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            result = readIt(is, 10);

        }catch(SocketTimeoutException e){
            Log.i(DEBUG_TAG, "Socket Timeout Exception");
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
            /* ToDo: error case */
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
}
