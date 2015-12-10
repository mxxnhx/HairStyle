package com.example.badasaza.gohaesungsaview;

import android.content.ClipData;
import android.util.Log;
import android.view.View;

/**
 * Created by Badasaza on 2015-11-24.
 */
public class LongClickDragListener implements View.OnLongClickListener {

    @Override
    public boolean onLongClick(View v) {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder sb = new View.DragShadowBuilder(v);
        v.startDrag(data, sb, v, 0);
        v.setVisibility(View.INVISIBLE);
        Log.i("LongClick:", "Successful");
        return true;
    }
}
