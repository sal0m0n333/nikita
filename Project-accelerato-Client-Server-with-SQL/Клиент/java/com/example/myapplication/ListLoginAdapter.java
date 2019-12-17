package com.example.myapplication;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListLoginAdapter extends BaseAdapter {

    private List<ListLoginElement> list;
    private LayoutInflater layoutInflater;

    public ListLoginAdapter(Context context, List<ListLoginElement> list) {
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
            view = layoutInflater.inflate(R.layout.element_login_item, parent, false);
        }

        ListLoginElement listLoginElement = (ListLoginElement) getItem(position);

        ImageView imageView = view.findViewById(R.id.login_img);
        imageView.setImageResource(listLoginElement.getResId());

        ConstraintLayout constraintLayout = view.findViewById(R.id.container_login);

        if (listLoginElement.getResId() == 0){

            ConstraintSet set = new ConstraintSet();
            set.clone(constraintLayout);
            set.clear(R.id.login_id_txt, ConstraintSet.END);
            set.connect(R.id.login_id_txt, ConstraintSet.END, R.id.container_login, ConstraintSet.END);
            set.applyTo(constraintLayout);
        }

        TextView elementTxt = (TextView) view.findViewById(R.id.login_id_txt);
        elementTxt.setText(listLoginElement.getLogin());

        return view;
    }


}
