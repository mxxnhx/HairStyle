package com.example.badasaza.gohaesungsacustomer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.badasaza.gohaesungsamodel.ItemModel;
import com.example.badasaza.gohaesungsaview.AlbumRecyclerAdapter;
import com.example.badasaza.gohaesungsaview.DragListener;
import com.example.badasaza.gohaesungsaview.LongClickDragListener;

public class CustomerHome extends AppCompatActivity implements ActionBar.TabListener{

    private final long	FINSH_INTERVAL_TIME    = 2000;
    private long		backPressedTime        = 0;
    public static final int PHOTO_REQUEST = 0;
    public static final String ALBUMNUM_REQUEST = "req";

    private static String idcode;
    private int albumNum = 0;
    private final String DEBUG_TAG = "CusHome";
    private ArrayList<String> dateList;
    private BlockingQueue<Runnable> threadQueue;
    private ThreadPoolExecutor exec;
    private static ArrayList<ItemModel> als;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);
        dateList = new ArrayList<>();

        idcode = getIntent().getStringExtra(LoginAct.IDCODE);
        albumNum = getIntent().getIntExtra(LoginAct.ALBUMNUM, -1);

        threadQueue = new ArrayBlockingQueue<Runnable>(30, true);
        exec = new ThreadPoolExecutor(
                10,
                30,
                30000,
                TimeUnit.MILLISECONDS,
                threadQueue
        );

        File userDirectory = new File(Environment.getExternalStorageDirectory(), "GHSS/Users");
        if(!userDirectory.exists()){
            if(!userDirectory.mkdirs())
                Log.e(DEBUG_TAG, "can't create directory");
            else
                Log.i(DEBUG_TAG, "User directory created");
        }else
            Log.i(DEBUG_TAG, "User directory already exists!");
        File userLog = new File(userDirectory, idcode+".txt");
        if(!userLog.exists())
            try {
                if (!userLog.createNewFile()) {
                    albumNum = 0;
                    Log.e(DEBUG_TAG, "can't create user log file");
                }else
                    Log.i(DEBUG_TAG, "UserLog successfully created");
            }catch (IOException e){
                e.printStackTrace();
            }
        else{
            /* Check: read from log, array the dates*/
            Log.i(DEBUG_TAG, "checking & "+userLog.getAbsolutePath());
            try {
                BufferedReader br = new BufferedReader(new FileReader(userLog));
                String lineFeed = br.readLine();
                String date = null;
                while(lineFeed != null){
                    if(lineFeed.indexOf("/") != -1) {
                        date = lineFeed.split("/")[1];
                        dateList.add(date);
                    }
                    lineFeed = br.readLine();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            Log.i(DEBUG_TAG, "Date list length - " +dateList.size());
        }
        Log.i(DEBUG_TAG, albumNum+"");

        als = new ArrayList<>();

        int listNum = Math.min(dateList.size(), albumNum);
        for (int i = 1; i <= listNum; i++){
            ArrayList<String> param = loadImage(i);
            Log.i(DEBUG_TAG, dateList.get(i-1));
            String dateString = dateList.get(i-1);
            als.add(new ItemModel(param, dateString, false));
        }

        Collections.reverse(als);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            ActionBar.Tab t = actionBar.newTab();
            t.setIcon(mSectionsPagerAdapter.getPageIcon(i));
            actionBar.addTab(
                    t.setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer_home, menu);
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
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        final ActionBar actBar = getSupportActionBar();

        mViewPager.setCurrentItem(tab.getPosition());
        switch(tab.getPosition()){
            case 0:
                actBar.setTitle(getResources().getText(R.string.home_tab));
                break;
            case 1:
                actBar.setTitle(getResources().getText(R.string.rec_tab));
                break;
            case 2:
                actBar.setTitle(getResources().getText(R.string.settings_tab));
                break;
            default:
                break;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void fabClick(View v){
        Intent i = new Intent(this, TakePhoto.class);
        i.putExtra(LoginAct.IDCODE, idcode);
        i.putExtra(ALBUMNUM_REQUEST, albumNum);
        startActivityForResult(i, PHOTO_REQUEST);
    }

    @Override
    public void onBackPressed() {
        long tempTime        = System.currentTimeMillis();
        long intervalTime    = tempTime - backPressedTime;

        if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(),"'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PHOTO_REQUEST && resultCode == RESULT_OK && data != null && mViewPager.getCurrentItem() == 0) {
            albumNum++;
            ArrayList<String> als = data.getStringArrayListExtra(TakePhoto.FILE_PATHS);
            String date = data.getStringExtra(TakePhoto.DATE_STRING);
            PlaceholderFragment phf = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
            phf.addItemToRecycler(new ItemModel(als, date, false));
        }
    }

    private ArrayList<String> loadImage(int alNum){
        String[] fileName = {idcode+"_"+alNum+"_h.jpg", idcode+"_"+alNum+"_s1.jpg", idcode+"_"+alNum+"_s2.jpg" };
        File f = null;
        ArrayList<String> temp = new ArrayList<>();
        for(String s : fileName){
            f = new File(Environment.getExternalStorageDirectory(), "GHSS/Image/"+s);
            if(!f.exists()){
                Log.i(DEBUG_TAG, "networking " + s);
                try {
                    exec.execute(new Task(s));
                    temp.add(f.getAbsolutePath());
                }catch(RejectedExecutionException e){
                    e.printStackTrace();
                }
            }else{
                temp.add(f.getAbsolutePath());
                Log.i(DEBUG_TAG, "there exists an image of "+s);
            }
        }
        return temp;
    }

    @Override
    protected void onStop(){
        super.onStop();

        exec.shutdown();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }

        public int getPageIcon(int position){
            switch (position){
                case 0:
                    return R.drawable.home_50;
                case 1:
                    return R.drawable.popular_topic_50;
                case 2:
                    return R.drawable.settings_50;
                default:
                    return R.mipmap.ic_launcher;
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        int position;

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

                RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.album_list);
                //LayoutManager
                GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
                glm.setOrientation(GridLayoutManager.VERTICAL);
                rv.setLayoutManager(glm);

                als.add(new ItemModel(Arrays.asList("ddcut1","ddcut2","ddcut3"), "2015-07-22", true));
                als.add(new ItemModel(Arrays.asList("rgcut1","rgcut2","rgcut3"), "2015-05-02", true));
                als.add(new ItemModel(Arrays.asList("tblock1","tblock2","tblock3"), "2015-02-15", true));
                AlbumRecyclerAdapter ara = new AlbumRecyclerAdapter(als);
                rv.setAdapter(ara);
            }
            else if(position == 2) {
                rootView = inflater.inflate(R.layout.fragment_customer_rec, container, false);

                RecImageTask rit = new RecImageTask();
                rit.execute(idcode);
                LinearLayout cont = (LinearLayout) rootView.findViewById(R.id.rec_hair_container);
                ImageView imgV = new ImageView(getActivity());
                imgV.setImageBitmap(rit.result);
                imgV.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(140), dpToPx(140)));
                imgV.setOnLongClickListener(new LongClickDragListener());
                cont.addView(imgV);
                cont.setOnDragListener(new DragListener());

                FrameLayout bu = (FrameLayout) rootView.findViewById(R.id.bald_user);
                bu.setOnDragListener(new DragListener());

                ImageView userPhoto = (ImageView) rootView.findViewById(R.id.user_photo);
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
    }

    private class Task implements Runnable{

        private String fileName;

        Task(String fileName){
            super();
            this.fileName = fileName;
        }

        @Override
        public void run() {
            Log.i(DEBUG_TAG, "download thread started");
            InputStream is = null;
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "GHSS/Image/"+fileName;
            Bitmap bm = null;
            try {
                URL url = new URL("http://143.248.57.222:80/sendhome/" + fileName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(false);
                conn.setDoOutput(true);
                conn.setRequestMethod("GET");

                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                if(response == 200) {
                    is = conn.getInputStream();

                    bm = BitmapFactory.decodeStream(is);

                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(path);
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private static class RecImageTask extends AsyncTask<String, Void, Bitmap>{

        public String fileName;
        public Bitmap result;

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream is = null;
            Bitmap btm = null;
            try {
                URL url = new URL("http://143.248.57.222:80/sendrec/"+params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d("CusHome", "The response is: " + response);
                is = conn.getInputStream();
                String raw = conn.getHeaderField("Content-Disposition");
                if(raw != null && raw.indexOf('=') != -1)
                    fileName = raw.split("=")[1];
                else
                    Log.i("CusHome", "not found file name");
                btm = BitmapFactory.decodeStream(is);

                return btm;

            }catch(SocketTimeoutException e){
                Log.i("CusHome", "Socket Timeout Exception");
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
        }
    }
}
