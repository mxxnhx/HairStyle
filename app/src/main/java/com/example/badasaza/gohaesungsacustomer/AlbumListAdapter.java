package com.example.badasaza.gohaesungsacustomer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import java.util.List;

/**
 * Created by Badasaza on 2015-10-30.
 */
public class AlbumListAdapter extends ArrayAdapter<String> {

    Context cxt;
    List<String> pics;

    public AlbumListAdapter(Context context, List<String> objects){
        super(context, R.layout.listview_albumshow, objects);
        cxt = context;
        pics = objects;
    }

    private class ViewHolder{
        ImageButton imgButton;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder vh;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.listview_albumshow, parent, false);

            vh = new ViewHolder();
            vh.imgButton = (ImageButton) convertView.findViewById(R.id.album_cover);

            convertView.setTag(vh);
        }else
            vh = (ViewHolder) convertView.getTag();
        int resId = cxt.getResources().getIdentifier("@drawable/"+pics.get(position), "drawable", cxt.getPackageName());

        vh.imgButton.setImageResource(resId);
        vh.imgButton.setAdjustViewBounds(true);
        vh.imgButton.setOnClickListener((View.OnClickListener) cxt);

        return convertView;
    }
}
