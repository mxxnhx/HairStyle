package com.example.badasaza.gohaesungsacustomer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import android.content.Intent;
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

    private String idcode;
    private int albumNum;

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

        idcode = getIntent().getStringExtra(LoginAct.IDCODE);
        albumNum = getIntent().getIntExtra(LoginAct.ALBUMNUM, -1);

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
            ArrayList<String> als = data.getStringArrayListExtra(TakePhoto.FILE_PATHS);
            String date = data.getStringExtra(TakePhoto.DATE_STRING);
            PlaceholderFragment phf = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
            phf.addItemToRecycler(new ItemModel(als, date, false));
        }
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

                /* ToDo: receive img from server */
                ArrayList<ItemModel> als = new ArrayList<>();
                als.add(new ItemModel(Arrays.asList("ddcut1","ddcut2","ddcut3"), "2015-07-22", true));
                als.add(new ItemModel(Arrays.asList("rgcut1","rgcut2","rgcut3"), "2015-05-02", true));
                als.add(new ItemModel(Arrays.asList("tblock1","tblock2","tblock3"), "2015-02-15", true));
                AlbumRecyclerAdapter ara = new AlbumRecyclerAdapter(als);
                rv.setAdapter(ara);
            }
            else if(position == 2) {
                rootView = inflater.inflate(R.layout.fragment_customer_rec, container, false);

                /*ListView lis = (ListView) rootView.findViewById(R.id.rec_list);
                Integer[] temp = {R.drawable.rec1, R.drawable.rec2, R.drawable.rec3};
                RecListAdapter rla = new RecListAdapter(getActivity(), Arrays.asList(temp));
                lis.setAdapter(rla);*/

                /* ToDo: get server working here! (Get thumb image and recimage)*/
                LinearLayout cont = (LinearLayout) rootView.findViewById(R.id.rec_hair_container);
                ImageView imgV = new ImageView(getActivity());
                imgV.setImageResource(R.drawable.wig);
                imgV.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(140), dpToPx(140)));
                imgV.setOnLongClickListener(new LongClickDragListener());
                cont.addView(imgV);
                cont.setOnDragListener(new DragListener());

                FrameLayout bu = (FrameLayout) rootView.findViewById(R.id.bald_user);
                bu.setOnDragListener(new DragListener());
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
}
