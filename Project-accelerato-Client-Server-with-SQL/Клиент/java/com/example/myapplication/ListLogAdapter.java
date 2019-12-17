package com.example.myapplication;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;



import java.util.List;

public class ListLogAdapter extends BaseAdapter {

    private List<ListLogElement> list;
    private LayoutInflater layoutInflater;


    public ListLogAdapter(Context context, List<ListLogElement> list) {
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
        if (view == null) {
            view = layoutInflater.inflate(R.layout.element_log_item, parent, false);
        }

        ListLogElement listLogElement = (ListLogElement) getItem(position);

        TextView message = (TextView) view.findViewById(R.id.message_item);
        TextView date = (TextView) view.findViewById(R.id.date_item);
        ConstraintLayout constraintLayout = view.findViewById(R.id.container);

        ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);

        set.clear(R.id.message_item, ConstraintSet.LEFT);
        set.clear(R.id.message_item, ConstraintSet.RIGHT);

        if (listLogElement.getLogin().equals(listLogElement.getMylogin())){
            set.connect(R.id.message_item, ConstraintSet.RIGHT, R.id.container, ConstraintSet.RIGHT);
            message.setPadding(350,0,0,0);
            set.applyTo(constraintLayout);
        }else {
            set.connect(R.id.message_item, ConstraintSet.LEFT, R.id.container, ConstraintSet.LEFT);
            message.setPadding(0,0,350,0);
            set.applyTo(constraintLayout);
        }

        set.clone(constraintLayout);
        set.clear(R.id.message_item, ConstraintSet.TOP);

        if (position != 0 && ((ListLogElement) getItem(position-1)).getDate().substring(0,10).equals(listLogElement.getDate().substring(0,10))){
            set.connect(R.id.message_item, ConstraintSet.TOP, R.id.container, ConstraintSet.TOP);
            date.setText(null);
        }else{
            set.connect(R.id.message_item, ConstraintSet.TOP, R.id.date_item, ConstraintSet.BOTTOM);
            date.setText(listLogElement.getDate().substring(0,10));
        }

        set.applyTo(constraintLayout);

        String text = "<font color=#006400>" + listLogElement.getLogin() + " </font> " +
                "<font color=#000000>" + listLogElement.getMessage() + "</font> " +
                "  <small><small><i>" + listLogElement.getDate().substring(10,16) +"</i></small></small>";
        message.setText(Html.fromHtml(text));

        return view;
    }


}
