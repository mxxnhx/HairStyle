package com.example.badasaza.gohaesungsaview;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.badasaza.gohaesungsacustomer.AlbumShow;
import com.example.badasaza.gohaesungsacustomer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Badasaza on 2015-10-30.
 */
public class AlbumListAdapter extends ArrayAdapter<String> {

    public static final String IMG_KEY = "image_key";
    public static final String DATE_KEY  = "date_key";
    private Context cxt;
    private List<String> pics;
    private String[] dateList;

    public AlbumListAdapter(Context context, List<String> objects){
        super(context, R.layout.listview_albumshow, objects);
        cxt = context;
        pics = objects;
        dateList = new String[3];
        dateList[0] = "2015-07-22";
        dateList[1] = "2015-05-02";
        dateList[2] = "2015-02-15";
    }

    private class ViewHolder{
        TextView date;
        TextView size;
        ImageView imgButton;
        CardView cv;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder vh;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.listview_albumshow, parent, false);

            vh = new ViewHolder();
            vh.date = (TextView) convertView.findViewById(R.id.date_text);
            vh.imgButton = (ImageView) convertView.findViewById(R.id.album_cover);
            vh.size = (TextView) convertView.findViewById(R.id.album_size);
            vh.cv = (CardView) convertView.findViewById(R.id.card_view);

            convertView.setTag(vh);
        }else
            vh = (ViewHolder) convertView.getTag();
        int resId = cxt.getResources().getIdentifier("@drawable/"+pics.get(position), "drawable", cxt.getPackageName());

        ArrayList<Integer> asd = getPhotoArray(position);
        vh.imgButton.setImageResource(resId);

        vh.cv.setOnClickListener(new AlbumListener(cxt, dateList[position], asd));

        vh.date.setText(dateList[position]);

        vh.size.setText(cxt.getResources().getText(R.string.num_of_photo).toString()+" " +asd.size()+cxt.getResources().getText(R.string.jang));

        return convertView;
    }

    private ArrayList<Integer> getPhotoArray(int pos){
        switch(pos){
            case 0:
                Integer[] temp1 = {R.drawable.ddcut1, R.drawable.ddcut2, R.drawable.ddcut3, R.drawable.ddcut4};
                return new ArrayList<Integer>(Arrays.asList(temp1));
            case 1:
                Integer[] temp2 = {R.drawable.rgcut1, R.drawable.rgcut2, R.drawable.rgcut3, R.drawable.rgcut4};
                return new ArrayList<Integer>(Arrays.asList(temp2));
            case 2:
                Integer[] temp3 = {R.drawable.tblock1, R.drawable.tblock2, R.drawable.tblock3, R.drawable.tblock4};
                return new ArrayList<Integer>(Arrays.asList(temp3));
            default:
                return null;
        }
    }

    public class AlbumListener implements View.OnClickListener{
        private Context cxt;
        private String date;
        private ArrayList<Integer> imgId;

        public AlbumListener(Context cont, String dt, ArrayList<Integer> ali){
            this.cxt = cont;
            this.date = dt;
            this.imgId = ali;
        }

        @Override
        public void onClick(View v){
            Intent i = new Intent(cxt, AlbumShow.class);
            i.putIntegerArrayListExtra(IMG_KEY, imgId);
            i.putExtra(DATE_KEY, date);
            cxt.startActivity(i);
        }
    }
}
