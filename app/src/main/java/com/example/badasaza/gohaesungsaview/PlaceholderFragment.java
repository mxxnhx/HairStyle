package com.example.badasaza.gohaesungsaview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.badasaza.gohaesungsacustomer.CustomerHome;
import com.example.badasaza.gohaesungsacustomer.R;
import com.example.badasaza.gohaesungsamodel.ItemModel;
import com.example.badasaza.gohaesungsamodel.RatingSender;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Badasaza on 2015-12-03.
 */
public class PlaceholderFragment extends Fragment implements View.OnClickListener{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String DEBUG_TAG = "PlaceholderFragment";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    int position;
    private String idcode;
    private ImageView userPhoto;
    private RecImageTask rit;
    private String currentRecFile;
    private RatingBar rtb;
    private ImageButton accept;
    private FrameLayout bu;
    private ImageView temp;
    private FrameLayout canvas;
    private final int ANIMATION_DURATION = 200;

    private View rootView;

    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        position = (this.getArguments() == null ? 0 : this.getArguments().getInt(ARG_SECTION_NUMBER));
        if(position == 1) {
            rootView = inflater.inflate(R.layout.fragment_customer_home, container, false);

            CustomerHome ch = (CustomerHome) getActivity();
            ArrayList<ItemModel> als = ch.getAls();
            RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.album_list);
            //LayoutManager
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
            glm.setOrientation(GridLayoutManager.VERTICAL);
            rv.setLayoutManager(glm);

            /* ToDo: changjo gyeung jae */
            /*als.add(new ItemModel(Arrays.asList("ddcut1", "ddcut2", "ddcut3"), "2015-07-22", true));
            als.add(new ItemModel(Arrays.asList("rgcut1","rgcut2","rgcut3"), "2015-05-02", true));
            als.add(new ItemModel(Arrays.asList("tblock1","tblock2","tblock3"), "2015-02-15", true));*/
            AlbumRecyclerAdapter ara = new AlbumRecyclerAdapter(als);
            rv.setAdapter(ara);
        }
        else if(position == 2) {
            rootView = inflater.inflate(R.layout.fragment_customer_rec, container, false);

            DragListener listen = new DragListener();
            idcode = ((CustomerHome) getActivity()).getIdcode();
            rit = new RecImageTask();
            rit.execute(idcode);
            ImageView imgV = new ImageView(getActivity());
            imgV.setImageBitmap(rit.result);
            imgV.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(140), dpToPx(140)));
            imgV.setOnLongClickListener(new LongClickDragListener());

            canvas = (FrameLayout) rootView.findViewById(R.id.rec_canvas);
            bu = (FrameLayout) rootView.findViewById(R.id.bald_user);
            canvas.setOnDragListener(listen);

            rtb = (RatingBar) rootView.findViewById(R.id.rec_rate);
            rtb.setVisibility(View.GONE);

            userPhoto = (ImageView) rootView.findViewById(R.id.user_photo);
            userPhoto.setOnDragListener(listen);

            accept = (ImageButton) rootView.findViewById(R.id.rec_accept);
            ImageButton reject = (ImageButton) rootView.findViewById(R.id.rec_reject);

            accept.setTag(true);

            accept.setOnClickListener(this);
            reject.setOnClickListener(this);
        }
        else if(position == 3)
            rootView = inflater.inflate(R.layout.fragment_customer_settings, container, false);
        return rootView;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public void addItemToRecycler(ItemModel item){
        if(rootView != null) {
            AlbumRecyclerAdapter ara = (AlbumRecyclerAdapter) ((RecyclerView) rootView.findViewById(R.id.album_list)).getAdapter();
            ara.addItem(item);
            ara.notifyDataSetChanged();
        }
    }

    private void toSpectatorMode(boolean doRating){
        /* ToDo : gotta check rit before finished */
        rit = new RecImageTask();
        rit.execute(idcode);
        if(doRating)
            new Thread(new RatingSender(idcode, rtb, currentRecFile)).start();
    }

    private void toTryMode(){
        userPhoto.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/GHSS/Image/"+idcode+"_face.jpg"));
        userPhoto.invalidate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rec_accept:
                if((Boolean)v.getTag()){
                    toTryMode();
                    RecHairTask rht = new RecHairTask();
                    rht.execute(idcode);
                    ratingBarShow();
                    v.setTag(false);
                    ((ImageView) v).setImageResource(R.drawable.ic_check_black_36dp);
                    v.invalidate();
                }else{
                    toSpectatorMode(true);
                    v.setTag(true);
                    ratingBarShow();
                    ((ImageView) v).setImageResource(R.drawable.ic_arrow_forward_black_36dp);
                    v.invalidate();
                    if(temp != null){
                        canvas.removeView(temp);
                        temp = null;
                    }
                }
                break;
            case R.id.rec_reject:
                toSpectatorMode(false);
                ratingBarHide();
                accept.setTag(true);
                accept.setImageResource(R.drawable.ic_arrow_forward_black_36dp);
                accept.invalidate();
                if(temp != null){
                    canvas.removeView(temp);
                    temp = null;
                }
                break;
        }
    }

    private void ratingBarShow(){
        rtb.setAlpha(0.0f);
        rtb.setVisibility(View.VISIBLE);
        rtb.animate().alpha(1f).setDuration(ANIMATION_DURATION).setListener(null);
    }

    private void ratingBarHide(){
        rtb.animate().alpha(0f).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rtb.setVisibility(View.GONE);
            }
        });
    }

    private class RecImageTask extends AsyncTask<String, Void, Bitmap> {

        public String fileName;
        public Bitmap result;

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream is = null;
            Bitmap btm = null;
            try {
                URL url = new URL("http://143.248.57.222:80/sendrec1/"+params[0]);
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
            this.result = result;
            userPhoto.setImageBitmap(result);
            userPhoto.invalidate();
            currentRecFile = fileName;
        }
    }

    private class RecHairTask extends AsyncTask<String, Void, Bitmap> {

        public Bitmap result;

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream is = null;
            Bitmap btm = null;
            try {
                URL url = new URL("http://143.248.57.222:80/sendrec2/"+params[0]);
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
            this.result = result;
            temp = new ImageView(getContext());
            temp.setImageBitmap(result);
            ViewGroup.LayoutParams flp = canvas.getLayoutParams();
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(flp.width/2 - lp.width/2 , flp.height/2 - lp.height/2 , 0, 0);
            temp.setLayoutParams(lp);
            temp.setOnLongClickListener(new LongClickDragListener());
            temp.setAlpha(1.0f);
            canvas.addView(temp);
            temp.bringToFront();
            canvas.invalidate();
            temp.invalidate();
        }
    }
}
