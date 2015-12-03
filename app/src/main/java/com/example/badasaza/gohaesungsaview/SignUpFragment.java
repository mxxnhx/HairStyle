package com.example.badasaza.gohaesungsaview;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.badasaza.gohaesungsacustomer.R;
import com.example.badasaza.gohaesungsacustomer.SignUpAct;
import com.example.badasaza.gohaesungsamodel.RatingSender;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/* ToDo: Optional: if time allows, do snackbar instead of dialog */
/**
 * Created by Badasaza on 2015-11-30.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {
    private int pageNum = -1;

    public static final String PAGE_KEY = "pgkey";
    private final String DEBUG_TAG = "SignUpFragment";
    private TestTask tt;
    private RatingBar rtb;
    private ImageView img;
    private String fileName;
    private String idcode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        Bundle args = getArguments();
        if(args==null)
            Log.e("SignUpFragment", "no argument");
        else{
            pageNum = args.getInt(PAGE_KEY, -1);
            if(pageNum == -1)
                Log.e("SignUpFragment", "no page number");
            TextView tv = (TextView) rootView.findViewById(R.id.signup_eval_text);
            tv.setText(getActivity().getText(R.string.signup_eval) + " ("+(pageNum+1)+"/5)");
        }

        SignUpAct sua = (SignUpAct) getActivity();
        idcode = sua.getIdcode();

        rtb = (RatingBar) rootView.findViewById(R.id.signup_rate);
        Button b = (Button) rootView.findViewById(R.id.signup_to_next2);
        b.setText((pageNum < 4 ? getText(R.string.signup_next) : getText(R.string.signup_end)));
        b.setOnClickListener(this);
        img = (ImageView) rootView.findViewById(R.id.signup_sample_image);
        tt = new TestTask();
        tt.execute(pageNum + "");

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(pageNum < 4) {
            new Thread(new RatingSender(idcode, rtb, fileName)).start();
            FragmentManager fm = getFragmentManager();
            SignUpFragment suf = new SignUpFragment();
            Bundle a = new Bundle();
            a.putInt(SignUpFragment.PAGE_KEY, pageNum + 1);
            suf.setArguments(a);
            fm.beginTransaction().replace(R.id.signup_frag_container, suf).addToBackStack(null).commit();
        }else{
            new Thread(new RatingSender(idcode, rtb, fileName)).start();
            SignUpAct sua = (SignUpAct) getActivity();
            sua.finisherDialog().create().show();
        }
    }

    private class TestTask extends AsyncTask<String, Void, Bitmap> {
        public Bitmap result;
        /* ToDo: consider while loop here */
        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream is = null;
            Bitmap btm = null;

            try {
                URL url = new URL("http://143.248.57.222:80/sendtest/"+params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();
                String raw = conn.getHeaderField("Content-Disposition");
                //String raw = conn.getHeaderField(0);
                //String raw = conn.getHeaderField("filename");
                Log.i(DEBUG_TAG, (raw==null? "sdfsdf" : raw));
                if(raw != null && raw.indexOf('=') != -1)
                    fileName = raw.split("=")[1];
                else
                    Log.i(DEBUG_TAG, "not found file name");
                btm = BitmapFactory.decodeStream(is);

                return btm;

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

        @Override
        protected void onPostExecute(Bitmap result){
            Log.i(DEBUG_TAG, "got "+result.getByteCount()+" amount of data");
            this.result = result;
            img.setImageBitmap(result);
            Log.i(DEBUG_TAG, img.getLayoutParams().width + "");
            img.invalidate();
        }
    }


}
