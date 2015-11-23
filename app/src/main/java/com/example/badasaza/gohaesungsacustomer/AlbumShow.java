package com.example.badasaza.gohaesungsacustomer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.badasaza.gohaesungsaview.AlbumListAdapter;

import java.util.ArrayList;

public class AlbumShow extends AppCompatActivity {

    private ViewPager vp;
    public ArrayList<Integer> abl;
    public String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_show);
        date = getIntent().getStringExtra(AlbumListAdapter.DATE_KEY);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);
        a.setTitle(date);

        AlbumPager pa = new AlbumPager(getSupportFragmentManager());
        vp = (ViewPager) findViewById(R.id.album_show);
        vp.setAdapter(pa);
        abl = getIntent().getIntegerArrayListExtra(AlbumListAdapter.IMG_KEY);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album_show, menu);
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
        }else if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    public class AlbumPager extends FragmentPagerAdapter {

        public AlbumPager(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int i){
            Fragment fg = new PhotoFrag();
            Bundle args = new Bundle();
            args.putInt(PhotoFrag.STRING_KEY, abl.get(i).intValue());
            fg.setArguments(args);
            return fg;
        }

        @Override
        public int getCount(){
            return 4;
        }
    }

    public static class PhotoFrag extends Fragment{
        public static final String STRING_KEY = "asdfss";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            View rootView = inflater.inflate(
                    R.layout.fragment_photo, container, false);
            Bundle args = getArguments();
            int imgId = -1;
            if(args != null)
                imgId = args.getInt(STRING_KEY);
            if(imgId != -1)
                ((ImageView) rootView.findViewById(R.id.photo_slot)).setImageResource(imgId);
            return rootView;
        }
    }
}
