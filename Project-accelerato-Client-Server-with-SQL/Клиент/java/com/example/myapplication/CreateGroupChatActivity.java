package com.example.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupChatActivity extends Activity {

    EditText nameGroup;
    EditText nameUser;
    Button addUser;
    Button createGroup;
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
            new UpdateUserList().execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_chat_layout);

        nameGroup = findViewById(R.id.name_group_edit);
        nameUser = findViewById(R.id.name_user_edit);
        userList = findViewById(R.id.user_LV);
        addUser = findViewById(R.id.add_user_btn);
        createGroup = findViewById(R.id.create_group_btn);
        userListArr = new ArrayList<>();
    }

    public void onCreateGroup(View view) {

        if (!clientConnection.isOnline(this)){
            Toast.makeText(this, "Потеряно соединение с сервером", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nameGroup.getText().toString().equals("")) {
            Toast.makeText(this, "Введите название чата", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nameGroup.getText().toString().contains(":") || nameGroup.getText().toString().contains("-")) {
            Toast.makeText(this, "Название чата не должно содержать символы ':' или '-'", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isGroupExist = false;
        for (int i = 0; i < clientConnection.groupListID.size(); i++) {
            if (clientConnection.groupListID.valueAt(i).equals(nameGroup.getText().toString())) {
                isGroupExist = true;
                break;
            }
        }
        if (!isGroupExist) {

            boolean isNotEmpty = false;
            for (int i = 0; i < userListArr.size(); i++){
                if (userListArr.get(i).getResId() == R.drawable.ic_delete){
                    isNotEmpty = true;
                    break;
                }
            }
            if (isNotEmpty) {
                new CreateGroup().execute();
            } else
                Toast.makeText(this, "Добавьте хотя бы одного пользователя", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Такая группа уже существует", Toast.LENGTH_SHORT).show();
    }

    public void onAddUser(View view) {
        boolean userExist = false;
        for (int i = 0; i < userListArr.size(); i++){
            if (nameUser.getText().toString().equals(userListArr.get(i).getLogin())){
                userExist = true;
                userListArr.get(i).setResId(R.drawable.ic_delete);
                userList.setAdapter(new ListLoginAdapter(this, userListArr));
                break;
            }
        }
        if (!userExist){
            Toast.makeText(this, "Такого пользователя не существует", Toast.LENGTH_SHORT).show();
        }

    }

    class CreateGroup extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            Intent intent = new Intent(CreateGroupChatActivity.this, GroupChatActivity.class);
            intent.putExtra("Group_Name", nameGroup.getText().toString());
            intent.putExtra("Group_ID", groupID);
            CreateGroupChatActivity.this.finish();
            startActivity(intent);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            String request = clientConnection.LOGIN + ":";
            for (int i = 0; i < userListArr.size(); i++) {
                if (userListArr.get(i).getResId() == R.drawable.ic_delete)
                    request = request + userListArr.get(i).getLogin() + ":";
            }
            request = request + nameGroup.getText().toString();
            clientConnection.sendString(clientConnection.CREATE_GROUP, request);

            String lastMessage = null;
            while (lastMessage == null){
                lastMessage = clientConnection.keyValueString.get(clientConnection.CREATE_GROUP);
            }

            groupID = Integer.parseInt(lastMessage);

            clientConnection.uploadHistory();
            publishProgress();
            return null;
        }
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

    class UpdateUserList extends AsyncTask<Void, String, Void> {

        @Override
        protected void onProgressUpdate(String... strings) {
            super.onProgressUpdate(strings);
            String logins[];
            logins = strings[0].split(":");
            userListArr = new ArrayList<>();

            for (String str : logins){
                if (!(str.equals(clientConnection.LOGIN)))
                    userListArr.add(new ListLoginElement(str,R.drawable.ic_add));
            }

            userList.setAdapter(new ListLoginAdapter(CreateGroupChatActivity.this, userListArr));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!clientConnection.isOnline(CreateGroupChatActivity.this)){
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
