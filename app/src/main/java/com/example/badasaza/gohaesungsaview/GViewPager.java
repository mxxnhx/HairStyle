package com.example.badasaza.gohaesungsaview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Badasaza on 2015-12-10.
 */
public class GViewPager extends ViewPager {

    private boolean swipeEnabled = true;

    public GViewPager(Context context) {
        super(context);
    }

    public GViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(swipeEnabled)
            return super.onTouchEvent(event);
        else
            return true;
    }

    public void disableSwipe(){
        swipeEnabled = false;
    }

    public void enableSwipe(){
        swipeEnabled = true;
    }

    public boolean isEnabled(){
        return swipeEnabled;
    }
}
