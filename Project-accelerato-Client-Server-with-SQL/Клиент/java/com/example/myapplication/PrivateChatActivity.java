package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class PrivateChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    String myNickname;
    String toNickname;
    ListView logLV;
    List<ListLogElement> list = new ArrayList<>();
    EditText message;
    boolean isBreak;

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
            ClientConnection.LocalBinder mLocalBinder = (ClientConnection.LocalBinder)service;
            clientConnection = mLocalBinder.getService();
            myNickname = clientConnection.LOGIN;
            toNickname = getIntent().getStringExtra("Group_Name");
            toolbar.setTitle(getIntent().getStringExtra("Group_Name"));

            new UpdateLog().execute();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_chat_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        message = (EditText)findViewById(R.id.private_message_edit);
        logLV = findViewById(R.id.log_private_LV);
    }

    public void onSendPrivate(View v) {
        final String msg = message.getText().toString();
        message.setText(null);
        if (!msg.equals(""))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    clientConnection.sendString(clientConnection.MESSAGE_PRIVATE,  toNickname + ":" + msg);
                }
            }).start();

    }

    class UpdateLog extends AsyncTask<Void, String, Void> {
        @Override
        protected void onProgressUpdate(String... strings) {
            super.onProgressUpdate(strings);
            String allMessage[] = strings[0].split("-");

            list.add(new ListLogElement(allMessage[0],allMessage[1],allMessage[2], clientConnection.LOGIN));
            logLV.setAdapter(new ListLogAdapter(PrivateChatActivity.this, list));
        }

        @Override
        protected Void doInBackground(Void... voids) {

            clientConnection.uploadHistory();

            String lastMessage;
            int index = 0;
            for (int i = 0; i < clientConnection.historyChats.size(); i++) {
                if (clientConnection.historyChats.get(i).getNameChat().equals(toNickname) &&
                        clientConnection.historyChats.get(i).getTypeChat().equals("private")){
                    index = i;
                    break;
                }

            }

            int size = clientConnection.historyChats.get(index).getHistory().size();

            for (int i = 0; i < size; i++) {
                lastMessage = clientConnection.historyChats.get(index).getHistory().get(i);

                publishProgress(lastMessage);
            }

            while (clientConnection.keyValueString.get(clientConnection.MESSAGE_PRIVATE) != null) ;

            isBreak = true;
            while (isBreak) {
                lastMessage = null;

                while (lastMessage == null) {
                    if (!isBreak) return null;
                    lastMessage = clientConnection.keyValueString.get(clientConnection.MESSAGE_PRIVATE);
                }

                String group = "";
                String value = "";
                for (int i = 0; i != lastMessage.length(); i++) {
                    if (lastMessage.charAt(i) == ':') {
                        group = lastMessage.substring(0, i);
                        value = lastMessage.substring(i + 1);
                        break;
                    }
                }

                if (!(group.equals(toNickname) || group.equals(clientConnection.LOGIN))) {
                    continue;
                }
                lastMessage = value;
                publishProgress(lastMessage);
            }

            return null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, ClientConnection.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isLink) {
            isBreak = false;
            unbindService(serviceConnection);
            isLink = false;
        }
    }
}
