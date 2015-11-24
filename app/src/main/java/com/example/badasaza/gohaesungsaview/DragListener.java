package com.example.badasaza.gohaesungsaview;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.badasaza.gohaesungsacustomer.R;

/**
 * Created by Badasaza on 2015-11-24.
 */
public class DragListener implements View.OnDragListener {
    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch(event.getAction()){
            case DragEvent.ACTION_DRAG_ENTERED:
                if(v.getId() == R.id.bald_user)
                    Log.i("OnDrag: ", "Entered ImgView");
                break;
            case DragEvent.ACTION_DROP:
                if(v.getId() == R.id.bald_user) {
                    View target = (View) event.getLocalState();
                    ViewGroup ll = (ViewGroup) target.getParent();
                    ll.removeView(target);
                    ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) target.getLayoutParams();
                    vlp.setMargins(Math.round(event.getX())-target.getMeasuredWidth()/2, Math.round(event.getY())-target.getMeasuredHeight()/2, 0 ,0);
                    ((ViewGroup) v).addView(target);
                    Log.i("OnDrag: ", "Dropped on " + event.getX() + ", " + event.getY());
                    Log.i("OnDrag: ", "Dropped on round " + Math.round(event.getX()) + ", " + Math.round(event.getY()));
                    Log.i("OnDrag: ", "ImgView currently at " + target.getX() + ", " + target.getY());
                    target.setVisibility(View.VISIBLE);
                    target.invalidate();
                    v.requestLayout();
                    Log.i("OnDrag: ", "Dropped on ImgView");
                }else if (v.getId() == R.id.rec_hair_container) {
                    ((View) event.getLocalState()).setVisibility(View.VISIBLE);
                    Log.i("OnDrag: ", "Dropped on LinearLayout");
                }
                break;
            default:
                break;
        }
        return true;
    }
}
