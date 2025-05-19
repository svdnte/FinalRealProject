package com.example.aaa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

// Кастомный адаптер для выпадающего списка
public class CustomSpinnerAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    String[] args;

    public CustomSpinnerAdapter(Context appContext, String[] args) {
        context = appContext;
        this.args = args;
        layoutInflater = (LayoutInflater.from(appContext));
    }

    @Override
    public int getCount() {
        return args.length;
    }

    @Override
    public Object getItem(int position) {
        return args[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.activity_spinner_view, null);
        }
        ((TextView)convertView.findViewById(R.id.spinner_text_view)).setText(args[position]);

        return convertView;
    }
}
