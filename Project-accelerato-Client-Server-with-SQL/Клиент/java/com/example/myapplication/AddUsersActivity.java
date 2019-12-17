package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AddUsersActivity extends AppCompatActivity {

    String groupName;
    int groupID;

    ListView userList;
    List<ListLoginElement> userListArr;

    ClientConnection clientConnection;
    boolean isLink;
    boolean isUpdate;
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
            isUpdate = true;
            groupName = getIntent().getStringExtra("Group_Name");
            groupID = getIntent().getIntExtra("Group_ID",-1);
            updateUserList();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_users_activity);

        userList = findViewById(R.id.user_add_LV);
    }

    public void onAddOrDelete(View view){
        final View tmpView = view;
        int resId;
        if (userListArr.get(userList.getPositionForView(tmpView)).getResId() == R.drawable.ic_add)
            resId = R.drawable.ic_delete;
        else
            resId = R.drawable.ic_add;
        userListArr.get(userList.getPositionForView(tmpView)).setResId(resId);
        userList.setAdapter(new ListLoginAdapter(this, userListArr));

    }

    public void updateUserList() {

        while (!clientConnection.isOnline(AddUsersActivity.this)) {
            if (!isUpdate) {
                return;
            }
            SystemClock.sleep(100);
        }
        userListArr = new ArrayList<>();
        String allLogins[] = clientConnection.getLoginList().split(":");

        String lastMessage = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                clientConnection.sendString(clientConnection.GET_EXIST_GROUP, groupName + ":" + groupID);
            }
        }).start();


        while (lastMessage == null) {
            lastMessage = clientConnection.keyValueString.get(clientConnection.GET_EXIST_GROUP);
        }

        String existLogins[] = lastMessage.split(":");


        boolean b;
        for (int i = 0; i < allLogins.length; i++) {
            b = true;
            for (int j = 0; j < existLogins.length; j++) {
                if (allLogins[i].equals(existLogins[j])) {
                    b = false;
                    break;
                }
            }
            if (b)
                userListArr.add(new ListLoginElement(allLogins[i], R.drawable.ic_add));
        }

        userList.setAdapter(new ListLoginAdapter(AddUsersActivity.this, userListArr));

    }

    public void addUsersAndOnInfo(View view){
        String request = "";

        for (int i = 0; i < userListArr.size(); i++){
            if (userListArr.get(i).getResId() == R.drawable.ic_delete) {
                request = request + userListArr.get(i).getLogin() + ":";
            }
        }

        request = request + groupName + ":" + groupID;

        final String finalRequest = request;
        new Thread(new Runnable() {
            @Override
            public void run() {
                clientConnection.sendString(clientConnection.ADD_USERS_IN_GROUP, finalRequest);
            }
        }).start();

        Intent intent = new Intent(this, InfoPublicActivity.class);
        intent.putExtra("Group_Name", groupName);
        intent.putExtra("Group_ID", groupID);
        this.finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, InfoPublicActivity.class);
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
