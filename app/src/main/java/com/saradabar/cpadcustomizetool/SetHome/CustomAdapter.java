package com.saradabar.cpadcustomizetool.SetHome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.saradabar.cpadcustomizetool.R;

import java.util.List;
import java.util.Objects;

public class CustomAdapter extends ArrayAdapter<CustomData> {
    private final LayoutInflater layoutInflater_;


    CustomAdapter(Context context, int textViewResourceId, List<CustomData> objects) {
        super(context, textViewResourceId, objects);
        layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomData item = getItem(position);
        if (null == convertView) {
            convertView = layoutInflater_.inflate(R.layout.launcher_item, null);
        }
        ImageView imageView;
        imageView = convertView.findViewById(R.id.image);
        imageView.setImageDrawable(Objects.requireNonNull(item).getImageData());
        TextView textView;
        textView = convertView.findViewById(R.id.text);
        textView.setText(item.getTextData());
        return convertView;
    }
}