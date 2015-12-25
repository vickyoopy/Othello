package com.example.vicky.othello;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by vicky on 1/11/15.
 */


public class ImageAdapter extends BaseAdapter {
    private Battle battle_context;

    public ImageAdapter(Battle battle_context) {
        this.battle_context = battle_context;
    }

    public int getCount() {
        return 64;
    }

    public Object getItem(int position) {
        return battle_context.mThumbIds[position%8][position/8];
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(battle_context);
            imageView.setLayoutParams(new GridView.LayoutParams(50, 50));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(android.graphics.Color.parseColor("white"));
            imageView.getBackground().setAlpha(100);
            imageView.setPadding(3, 3, 3, 3);
        } else {
            imageView = (ImageView) convertView;
        }
        int x=position%8;
        int y=position/8;
        imageView.setImageResource(battle_context.mThumbIds[x][y]);
        return imageView;
    }
}

