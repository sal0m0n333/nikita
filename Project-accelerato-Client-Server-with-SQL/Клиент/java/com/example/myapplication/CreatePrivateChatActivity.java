package com.example.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CreatePrivateChatActivity extends Activity {

    private ListView listView;
    List<ListLoginElement> list;
    ClientConnection clientConnection;
    boolean isLink, isUpdate;

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
            new UpdateList().execute();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_private_chat_layout);

        listView = (ListView) findViewById(R.id.all_login_LV);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

                if (!clientConnection.isOnline(CreatePrivateChatActivity.this)){
                    Toast.makeText(CreatePrivateChatActivity.this, "Потеряно соединение с сервером", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(CreatePrivateChatActivity.this, PrivateChatActivity.class);
                intent.putExtra("Group_Name", list.get(position).getLogin());

                new CreatePrivateChat().execute(list.get(position).getLogin());

                CreatePrivateChatActivity.this.finish();
                startActivity(intent);
            }
        });
    }

    class CreatePrivateChat extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... voids) {
            clientConnection.sendString(clientConnection.CREATE_PRIVATE_CHAT, clientConnection.LOGIN + ":" + voids[0]);
            return null;
        }
    }

    class UpdateList extends AsyncTask<Void, String, Void> {

        @Override
        protected void onProgressUpdate(String... strings) {
            super.onProgressUpdate(strings);
            String logins[];
            logins = strings[0].split(":");
            list = new ArrayList<>();

            for (String str : logins){
                if (!(str.equals(clientConnection.LOGIN) || clientConnection.privateListID.contains(str)))
                    list.add(new ListLoginElement(str,0));
            }

            listView.setAdapter(new ListLoginAdapter(CreatePrivateChatActivity.this, list));

        }

        @Override
        protected Void doInBackground(Void... voids) {

            while (!clientConnection.isOnline(CreatePrivateChatActivity.this)){
                if (!isUpdate) {
                    return null;
                }
                SystemClock.sleep(100);
            }

            publishProgress(clientConnection.getLoginList());

            return null;
        }
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
