package com.example.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class GroupChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    String groupName;
    int groupID;
    EditText message;
    ListView logLV;
    List<ListLogElement> list = new ArrayList<>();
    Boolean isBreak;

    ClientConnection clientConnection;
    boolean isLink;


    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("INTERNET","onServiceDisconnected");
            isLink = false;
            clientConnection = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("INTERNET","onServiceConnected");
            isLink = true;
            ClientConnection.LocalBinder mLocalBinder = (ClientConnection.LocalBinder) service;
            clientConnection = mLocalBinder.getService();

            groupName = getIntent().getStringExtra("Group_Name");
            groupID = getIntent().getIntExtra("Group_ID",-1);
            toolbar.setTitle(getIntent().getStringExtra("Group_Name"));
            new UpdateLog().execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_chat_layout);
        Log.d("INTERNET","onCreate");
        message = (EditText) findViewById(R.id.message_edit);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        logLV = findViewById(R.id.log_group_LV);
    }

    class UpdateLog extends AsyncTask<Void, String, Void> {
        @Override
        protected void onProgressUpdate(String... strings) {
            super.onProgressUpdate(strings);

            String allMessage[] = strings[0].split("-");

            list.add(new ListLogElement(allMessage[0],allMessage[1],allMessage[2], clientConnection.LOGIN));
            logLV.setAdapter(new ListLogAdapter(GroupChatActivity.this, list));

        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("INTERNET","doInBackground");
            clientConnection.uploadHistory();

            String lastMessage;
            int index = 0;
            for (int i = 0; i < clientConnection.historyChats.size(); i++) {
                if (clientConnection.historyChats.get(i).getNameChat().equals(groupName) &&
                        clientConnection.historyChats.get(i).getGroupID() == groupID &&
                        clientConnection.historyChats.get(i).getTypeChat().equals("public")){
                    index = i;
                    break;
                }
            }

            int size = clientConnection.historyChats.get(index).getHistory().size();

            for (int i = 0; i < size; i++) {
                lastMessage = clientConnection.historyChats.get(index).getHistory().get(i);

                publishProgress(lastMessage);
            }

            while (clientConnection.keyValueString.get(clientConnection.MESSAGE) != null)
                SystemClock.sleep(10);

            isBreak = true;
            while (isBreak) {
                lastMessage = null;

                while (lastMessage == null) {
                    if (!isBreak) return null;
                    lastMessage = clientConnection.keyValueString.get(clientConnection.MESSAGE);
                }

                String group = "";
                String ID = "";
                String value = "";
                boolean two = false;
                for (int i = 0; i != lastMessage.length(); i++) {
                    if (lastMessage.charAt(i) == ':') {
                        if (!two){
                            two = true;
                            continue;
                        }
                        group = lastMessage.substring(0, i);
                        value = lastMessage.substring(i + 1);
                        break;
                    }
                }
                for (int i = 0; i != group.length(); i++) {
                    if (group.charAt(i) == ':') {
                        ID = group.substring(i + 1);
                        group = group.substring(0, i);
                        break;
                    }
                }


                if (!(group.equals(groupName) && ID.equals(groupID + ""))) {
                    continue;
                }
                lastMessage = value;
                publishProgress(lastMessage);
            }

            return null;
        }

    }

    public void onInfo(View v){

        Intent intent = new Intent(this,InfoPublicActivity.class);
        intent.putExtra("Group_Name", groupName);
        intent.putExtra("Group_ID", groupID);
        startActivity(intent);

    }

    public void onSendGroup(View v) {
        final String msg = message.getText().toString();
        message.setText(null);
        if (!msg.equals(""))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    clientConnection.sendString(clientConnection.MESSAGE, clientConnection.LOGIN + ":" + groupName + ":" + msg + ":" + groupID);
                }
            }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("INTERNET","onResume");
        bindService(new Intent(this, ClientConnection.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isLink) {
            isBreak = false;
            unbindService(serviceConnection);
            isLink = false;
        }
    }
}
