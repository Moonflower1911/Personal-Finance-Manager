package com.example.personal_finance_manager.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class IconGridAdapter extends BaseAdapter {
    private final Context context;
    private final int[] iconIds;

    public IconGridAdapter(Context context, int[] iconIds) {
        this.context = context;
        this.iconIds = iconIds;
    }

    @Override
    public int getCount() {
        return iconIds.length;
    }

    @Override
    public Object getItem(int position) {
        return iconIds[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconIds[position]);
        icon.setLayoutParams(new GridView.LayoutParams(96, 96));
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return icon;
    }
}

