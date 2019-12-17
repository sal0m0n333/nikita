package com.example.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainScreenActivity extends Activity {

    private ListView listView;
    List<ListGroupElement> list;
    boolean isUpdate,isEndUpdate = false;
    ClientConnection clientConnection;
    boolean isLink;

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
            clientConnection.groupListID.clear();
            clientConnection.privateListID.clear();
            new UpdateGroupList().execute();
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_layout);

        listView = findViewById(R.id.viewAllChats_LV);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(list.get(position).getTypeChat().equals("public")) {
                    Intent intent = new Intent(MainScreenActivity.this, GroupChatActivity.class);
                    intent.putExtra("Group_Name", list.get(position).getGroupName());
                    intent.putExtra("Group_ID", list.get(position).getGroupID());
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(MainScreenActivity.this, PrivateChatActivity.class);
                    intent.putExtra("Group_Name", list.get(position).getGroupName());
                    startActivity(intent);
                }
            }
        });

    }

    public void onDelete(View view){
        final View tmpView = view;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (clientConnection.groupListID.contains(list.get(listView.getPositionForView(tmpView)).getGroupName()+ "-" + list.get(listView.getPositionForView(tmpView)).getGroupID()))
                    clientConnection.sendString(clientConnection.DELETE_SELF_GROUP, list.get(listView.getPositionForView(tmpView)).getGroupName() + ":" +
                            list.get(listView.getPositionForView(tmpView)).getGroupID());
                else
                    clientConnection.sendString(clientConnection.DELETE_PRIVATE_CHAT, list.get(listView.getPositionForView(tmpView)).getGroupName());
            }
        }).start();
    }


    class UpdateGroupList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            String[] strings;
            list = new ArrayList<>();
            ArrayList<String> arrayList;
            String str;
            int size;
            for (int i = 0; i < clientConnection.historyChats.size(); i++){
                arrayList = clientConnection.historyChats.get(i).getHistory();
                size = arrayList.size();
                if (size != 0){
                    str = arrayList.get(size-1);
                    strings = str.split("-");
                    str = strings[0] + "  " + strings[1] + " : " + strings[2];
                }
                else
                    str = "Список сообщений пуст";

                int resId;
                if (clientConnection.historyChats.get(i).getTypeChat().equals("public"))
                    resId = R.drawable.ic_group;
                else
                    resId = R.drawable.ic_user_add;
                list.add(new ListGroupElement(clientConnection.historyChats.get(i).getNameChat(),
                        clientConnection.historyChats.get(i).getGroupID(),
                        str, clientConnection.historyChats.get(i).getTypeChat(), resId));
            }
            listView.setAdapter(new ListGroupAdapter(MainScreenActivity.this, list));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String[] strings;
            isEndUpdate = false;
            ArrayList<String> arrayList;
            String str;
            int size;
            while (!clientConnection.isOnline(MainScreenActivity.this)){
                if (!isUpdate) {
                    isEndUpdate = true;
                    return null;
                }
                SystemClock.sleep(100);
            }
            clientConnection.uploadHistory();
            publishProgress();
            while (isUpdate) {
                while (!clientConnection.isOnline(MainScreenActivity.this)){
                    if (!isUpdate) {
                        isEndUpdate = true;
                        return null;
                    }
                    SystemClock.sleep(100);
                }
                Log.d("INTERNET", "doInBackground - uploadHistory - start");
                clientConnection.uploadHistory();
                Log.d("INTERNET", "doInBackground - uploadHistory - end");
                if ((clientConnection.groupListID.size() + clientConnection.privateListID.size()) != listView.getCount()){
                    publishProgress();
                }

                    for (int i = 0; i < clientConnection.historyChats.size(); i++){
                        arrayList = clientConnection.historyChats.get(i).getHistory();
                        size = arrayList.size();
                        if (size != 0) {
                            str = arrayList.get(size - 1);
                            strings = str.split("-");
                            str = strings[0] + "  " + strings[1] + " : " + strings[2];
                        }
                        else
                            str = "Список сообщений пуст";

                        if (clientConnection.historyChats.size() != list.size() ||
                                !list.get(i).getSubInfo().equals(str))
                            publishProgress();
                    }
                Log.d("INTERNET", "doInBackground - stop");
            }
            Log.d("INTERNET", "onPause - isEndUpdate = true");
            isEndUpdate = true;
            return null;
        }
    }

    public void onCreateGroupActivity(View v) {
        Intent intent = new Intent(this, CreateGroupChatActivity.class);
        startActivity(intent);
    }

    public void onCreatePrivateActivity(View v) {
        Intent intent = new Intent(this, CreatePrivateChatActivity.class);
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
        Log.d("INTERNET", "onPause - start");
        isUpdate = false;
        while (!isEndUpdate)
        {
            Log.d("INTERNET", "onPause - while (!isEndUpdate)");
            SystemClock.sleep(10);
        }


        if (isLink) {
            unbindService(serviceConnection);
            isLink = false;
        }
        Log.d("INTERNET", "onPause - stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(new Runnable() {
            @Override
            public void run() {
                clientConnection.sendString(clientConnection.LAST_ONLINE,"");
            }
        }).start();
    }
}
