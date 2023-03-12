package com.example.bluetoothuploader.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.bluetoothuploader.R;

import java.util.List;

public class ConnectionAdapter extends ArrayAdapter<Connection> {
    private int resourceId;

    public ConnectionAdapter(Context context, int resourceId,List<Connection> objects ) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Connection connection = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView nameText = (TextView)view.findViewById(R.id.name);
        TextView addsText = (TextView)view.findViewById(R.id.adds);
        nameText.setText(connection.getName());
        addsText.setText(connection.getAdds());
        return view;
    }
}
