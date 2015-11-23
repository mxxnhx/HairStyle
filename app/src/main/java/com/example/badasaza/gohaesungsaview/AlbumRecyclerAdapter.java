package com.example.badasaza.gohaesungsaview;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.badasaza.gohaesungsacustomer.AlbumShow;
import com.example.badasaza.gohaesungsacustomer.R;
import com.example.badasaza.gohaesungsamodel.ItemModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Badasaza on 2015-11-23.
 */
public class AlbumRecyclerAdapter extends RecyclerView.Adapter {

    public List<ItemModel> items;
    private Context cxt;

    public static final String ITEM_MODEL = "image_key";

    public AlbumRecyclerAdapter(List<ItemModel> items){
        this.items = items;
    }

    @Override
    public AlbumRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        cxt = parent.getContext();
        View itemView = LayoutInflater.from(cxt).inflate(R.layout.recyclerview_albumshow, parent, false);
        return new AlbumRecyclerViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemModel i = items.get(position);
        AlbumRecyclerViewHolder arvh = (AlbumRecyclerViewHolder) holder;
        Iterator<ImageView> iter = arvh.imgViews.iterator();
        if(i.inApp){
            for(String s : i.imgFiles) {
                int resId = cxt.getResources().getIdentifier("@drawable/" + s, "drawable", cxt.getPackageName());
                if(iter.hasNext())
                    iter.next().setImageResource(resId);
            }
            arvh.date.setText(i.dateTime);
            arvh.card.setOnClickListener(new AlbumRecyclerListener(cxt, i));
        }else{
            /* Load bitmap from file directory and put it in the list */
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public final static class AlbumRecyclerViewHolder extends RecyclerView.ViewHolder{

        public List<ImageView> imgViews;
        public CardView card;
        public TextView date;

        public AlbumRecyclerViewHolder(View v, int viewType){
            super(v);
            imgViews = new ArrayList<>();
            imgViews.add((ImageView) v.findViewById(R.id.main_image));
            imgViews.add((ImageView) v.findViewById(R.id.sub1_image));
            imgViews.add((ImageView) v.findViewById(R.id.sub2_image));
            date = (TextView)v.findViewById(R.id.date);
            card = (CardView) v.findViewById(R.id.card_view);
        }
    }

    public class AlbumRecyclerListener implements View.OnClickListener{

        private Context cxt;
        private ItemModel i;

        public AlbumRecyclerListener(Context context, ItemModel i){
            this.cxt = context;
            this.i = i;
        }

        @Override
        public void onClick(View v){
            Intent in = new Intent(cxt, AlbumShow.class);
            in.putExtra(ITEM_MODEL, i);
            cxt.startActivity(in);
        }
    }
}
