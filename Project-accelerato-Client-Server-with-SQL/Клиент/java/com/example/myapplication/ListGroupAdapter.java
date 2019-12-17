package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListGroupAdapter extends BaseAdapter {

    private List<ListGroupElement> list;
    private LayoutInflater layoutInflater;

    public ListGroupAdapter(Context context, List<ListGroupElement> list) {
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = layoutInflater.inflate(R.layout.element_group_item, parent, false);
        }

        ListGroupElement listGroupElement = (ListGroupElement) getItem(position);

        ImageView imageView = view.findViewById(R.id.group_img);
        imageView.setImageResource(listGroupElement.getResId());

        TextView groupID = (TextView) view.findViewById(R.id.group_id_txt);
        groupID.setText(listGroupElement.getGroupName());

        TextView subInfo = (TextView) view.findViewById(R.id.sub_info_txt);
        subInfo.setText(listGroupElement.getSubInfo());

        return view;
    }
}
