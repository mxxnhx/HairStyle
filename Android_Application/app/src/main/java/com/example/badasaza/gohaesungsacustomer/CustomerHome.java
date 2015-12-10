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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.badasaza.gohaesungsamodel.ItemModel;
import com.example.badasaza.gohaesungsaview.GViewPager;
import com.example.badasaza.gohaesungsaview.PlaceholderFragment;

public class CustomerHome extends AppCompatActivity implements ActionBar.TabListener{

    private final long	FINSH_INTERVAL_TIME    = 2000;
    private long		backPressedTime        = 0;
    public static final int PHOTO_REQUEST = 0;
    public static final String ALBUMNUM_REQUEST = "req";

    private String idcode;
    private int albumNum = 0;
    private final String DEBUG_TAG = "CusHome";
    private ArrayList<String> dateList;
    private BlockingQueue<Runnable> threadQueue;
    private ThreadPoolExecutor exec;
    private ArrayList<ItemModel> als;
    private GViewPager gvp;

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
    public GViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        gvp = (GViewPager) findViewById(R.id.pager);
        gvp.disableSwipe();
        dateList = new ArrayList<>();

        idcode = getIntent().getStringExtra(LoginAct.IDCODE);
        albumNum = getIntent().getIntExtra(LoginAct.ALBUMNUM, -1);

        threadQueue = new ArrayBlockingQueue<>(30, true);
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

        String faceName = idcode+"_face.jpg";

        File userFace = new File(userDirectory, faceName);
        FaceTask ft = new FaceTask();
        if(!userFace.exists())
            ft.execute(faceName);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (GViewPager) findViewById(R.id.pager);
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

    public ArrayList<ItemModel> getAls(){
        return als;
    }

    public String getIdcode(){
        Log.i(DEBUG_TAG, idcode);
        return idcode;
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

    private class FaceTask extends AsyncTask<String, Void, Bitmap>{

        private String fileName;

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream is = null;
            Bitmap btm = null;
            fileName = params[0];
            try {
                URL url = new URL("http://143.248.57.222:80/sendface/" + idcode);
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
            FileOutputStream out = null;
            File file = new File(Environment.getExternalStorageDirectory() ,"GHSS/Image/"+fileName);
            Log.i(DEBUG_TAG, file.getAbsolutePath());
            try {
                out = new FileOutputStream(file);
                result.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Log.i(DEBUG_TAG, "face file created");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(DEBUG_TAG, "face file not created");
            }
        }
    }

}
