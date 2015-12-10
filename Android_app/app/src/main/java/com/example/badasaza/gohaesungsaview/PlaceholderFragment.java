package com.example.badasaza.gohaesungsaview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;

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

/**
 * Created by Badasaza on 2015-12-03.
 */
public class PlaceholderFragment extends Fragment implements View.OnClickListener, View.OnTouchListener{

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
    private CustomerHome csh;

    private View rootView;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;
    private View.OnTouchListener f = this;

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

            AlbumRecyclerAdapter ara = new AlbumRecyclerAdapter(als);
            rv.setAdapter(ara);
        }
        else if(position == 2) {
            csh = (CustomerHome) getActivity();
            rootView = inflater.inflate(R.layout.fragment_customer_rec, container, false);

            DragListener listen = new DragListener();
            idcode = ((CustomerHome) getActivity()).getIdcode();
            rit = new RecImageTask();
            rit.execute(idcode);

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
        matrix = new Matrix();
        savedMatrix = new Matrix();
        Thread t = null;
        if(doRating) {
            t = new Thread(new RatingSender(idcode, rtb, currentRecFile, false));
            t.start();
        }
        if (t != null)
            try {
                t.join();
            }catch (InterruptedException e){

            }
        rit = new RecImageTask();
        rit.execute(idcode);
        rtb.setRating(0f);
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
                    ratingBarHide();
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
                //URL url = new URL("http://143.248.57.222:80/sendrec2/"+params[0]);
                URL url = new URL("http://143.248.57.222:80/sendrec2/"+currentRecFile);
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
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins((canvas.getWidth() / 2) - lp.width / 2, (canvas.getHeight() / 2) - lp.height / 2, 0, 0);
            temp.setLayoutParams(lp);
            temp.setImageBitmap(result);
            //temp.setAdjustViewBounds(true);
            temp.setClickable(true);
            temp.setFocusable(true);
            temp.setFocusableInTouchMode(true);
            temp.setScaleType(ImageView.ScaleType.MATRIX);
            temp.setOnTouchListener(f);
            temp.setAlpha(1.0f);
            canvas.addView(temp);
            temp.bringToFront();
            canvas.invalidate();
            temp.invalidate();
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                //csh.mViewPager.disableSwipe();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                //csh.mViewPager.enableSwipe();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }
        view.setImageMatrix(matrix);
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}
