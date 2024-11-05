package com.openocr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class IDDataAdapter extends ArrayAdapter<IDDataAdapter.IDData> {

    static class IDData {
        public String mLabel;
        public String mData;
        public IDData(String label,String data) {
            mLabel = label;
            mData = data;
        }
    }

    private LayoutInflater mInflater;

    IDDataAdapter(Context context, ArrayList<IDData> results) {
        super(context,0,results);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // It needs inflating if it's not recycled
            convertView = mInflater.inflate(R.layout.item_ez_result, parent, false);
            // Make scan items not be clickable
            convertView.setEnabled(false);
            convertView.setOnClickListener(null);
        }

        IDData currentNumberPosition = getItem(position);

        TextView txtLabel = (TextView) convertView.findViewById(R.id.id_label);
        TextView txtData = (TextView) convertView.findViewById(R.id.id_data);
        txtLabel.setText(currentNumberPosition.mLabel);
        txtData.setText(currentNumberPosition.mData);

        return convertView;
    }

}
