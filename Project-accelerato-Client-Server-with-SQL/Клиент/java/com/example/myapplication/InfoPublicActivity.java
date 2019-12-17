package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class InfoPublicActivity extends AppCompatActivity {

    private ListView listView;
    List<ListLoginElement> list;
    ClientConnection clientConnection;
    boolean isLink, isUpdate;
    String groupName;
    int groupID;
    TextView info;

    private ListView listViewD;
    List<ListLoginElement> listD;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isLink = false;
            clientConnection = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isLink = true;
            ClientConnection.LocalBinder mLocalBinder = (ClientConnection.LocalBinder) service;
            clientConnection = mLocalBinder.getService();
            groupName = getIntent().getStringExtra("Group_Name");
            groupID = getIntent().getIntExtra("Group_ID",-1);
            isUpdate = true;
            updateList();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_public_activity);
        listView = (ListView) findViewById(R.id.info_public_LV);
        info = (TextView) findViewById(R.id.info_about_group);
    }

    public void updateList() {
        while (!clientConnection.isOnline(InfoPublicActivity.this)){
            if (!isUpdate) {
                return;
            }
            SystemClock.sleep(100);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                clientConnection.sendString(clientConnection.GET_EXIST_GROUP, groupName + ":" + groupID);
                clientConnection.sendString(clientConnection.GET_INFO_GROUP, groupName + ":" + groupID);
            }
        }).start();

        String lastMessage= null;
        while (lastMessage == null){
            lastMessage = clientConnection.keyValueString.get(clientConnection.GET_EXIST_GROUP);
        }

        String logins[] = lastMessage.split(":");
        list = new ArrayList<>();

        for (String str : logins){
            if (!clientConnection.LOGIN.equals(str))
                list.add(new ListLoginElement(str,R.drawable.ic_delete));
            else
                list.add(new ListLoginElement(str,0));
        }

        listView.setAdapter(new ListLoginAdapter(InfoPublicActivity.this, list));

        lastMessage= null;
        while (lastMessage == null){
            lastMessage = clientConnection.keyValueString.get(clientConnection.GET_INFO_GROUP);
        }

        logins = lastMessage.split("-");

        String string = "Название чата: <i><big><b>" + groupName +
                "</b></big></i><br>Создатель чата: <i><b><big>" + logins[1] +
                "</big></b></i><br>Количество участников: <i><b><big>" + list.size() + "</big></b></i>" +
                "<br>Дата создания чата: <i><b><big>" + logins[0] + "</big></b></i>";
        info.setText(Html.fromHtml(string));
    }

    public void onAddOrDelete(View view){
        final View tmpView = view;
        final String login = list.get(listView.getPositionForView(tmpView)).getLogin();

        if (clientConnection.LOGIN.equals(login))
            return;
        list.remove(list.get(listView.getPositionForView(tmpView)));
        listView.setAdapter(new ListLoginAdapter(this, list));

        new Thread(new Runnable() {
            @Override
            public void run() {
                clientConnection.sendString(clientConnection.DELETE_USER,login + ":" + groupName + ":" + groupID);
            }
        }).start();
    }



    public void addUserOnInfo(View v){

        Intent intent = new Intent(this, AddUsersActivity.class);
        intent.putExtra("Group_Name", groupName);
        intent.putExtra("Group_ID", groupID);
        this.finish();
        startActivity(intent);

    }



    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, ClientConnection.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isUpdate = false;
        if (isLink) {
            unbindService(serviceConnection);
            isLink = false;
        }
    }

}
