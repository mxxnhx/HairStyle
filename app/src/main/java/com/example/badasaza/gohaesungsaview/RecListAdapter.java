package com.example.badasaza.gohaesungsaview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.badasaza.gohaesungsacustomer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Badasaza on 2015-11-09.
 */
public class RecListAdapter extends ArrayAdapter<Integer> {

    private Context cxt;
    private List<Integer> items;
    private String[] names;

    public RecListAdapter(Context context, List<Integer> objects){
        super(context, R.layout.listview_reclist, objects);
        this.cxt = context;
        this.items = objects;
        this.names = new String[3];
        names[0] = "리젠트컷: ";
        names[1] = "투블럭: ";
        names[2] = "다운펌: ";
    }

    private class ViewHolder{
        TextView recName;
        ImageView recPic;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder vh;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.listview_reclist, parent, false);

            vh = new ViewHolder();
            vh.recName = (TextView) convertView.findViewById(R.id.rec_name);
            vh.recPic = (ImageView) convertView.findViewById(R.id.rec_pic);

            convertView.setTag(vh);
        }else
            vh = (ViewHolder) convertView.getTag();

        vh.recName.setText(names[position]);
        vh.recPic.setImageResource(items.get(position).intValue());
        return convertView;
    }

}
