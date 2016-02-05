package com.example.stream_provider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends ArrayAdapter<Triple> {

    public ListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapter(Context context, int resource, List<Triple> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitem, null);
        }

        Triple t = getItem(position);

        if (t != null) {
            TextView tv = (TextView) v.findViewById(R.id.listtextview);

            if (tv != null) {
                tv.setText(t.name + " (" + t.ip + ") - " + t.status);
            }
        }

        return v;
    }

}
