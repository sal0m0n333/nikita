package com.example.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends Activity {

    EditText login;
    EditText password;
    Button sign_in;
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        login = (EditText)findViewById(R.id.login_edit);
        password = (EditText)findViewById(R.id.pass_edit);
        sign_in = (Button) findViewById(R.id.sign_in_btn);
        startService(new Intent(this, ClientConnection.class));
    }

    public void goToMainActivity(View view){
        if (login.getText().toString().equals("")){
            Toast.makeText(this,"Введите логин!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.getText().toString().equals("")){
            Toast.makeText(this,"Введите пароль!", Toast.LENGTH_SHORT).show();
            return;
        }
        sign_in.setEnabled(false);
        new Authorization().execute();
    }

    class Authorization extends AsyncTask<Void , Integer, Void> {
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values[0] == 1){
                Toast.makeText(LoginActivity.this,"Верно", Toast.LENGTH_SHORT).show();
                sign_in.setEnabled(true);
                Intent intent = new Intent(LoginActivity.this,MainScreenActivity.class);
                startActivity(intent);
            }

            else if (values[0] == 0) {
                sign_in.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Неверно", Toast.LENGTH_SHORT).show();
            }
            else{
                sign_in.setEnabled(true);
                Toast.makeText(LoginActivity.this,"Нет соединения с сервером", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        protected Void doInBackground(Void... param) {
            int isAuth;
            String lastMessage = null;
            if (!clientConnection.sendString(clientConnection.AUTHORIZATION, login.getText().toString() + ":" + password.getText().toString())){
                isAuth = -1;
                publishProgress(isAuth);
                return null;
            }
            while (lastMessage == null)
                lastMessage = clientConnection.keyValueString.get(clientConnection.AUTHORIZATION);

            if (lastMessage.equals("true")) {
                clientConnection.LOGIN = login.getText().toString();
                isAuth = 1;
            } else
                isAuth = 0;

            publishProgress(isAuth);

            return null;
        }
    }

    public void goToRegistrationActivity(View view){
        Intent intent = new Intent(this,RegistrationActivity.class);
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
        if(isLink) {
            unbindService(serviceConnection);
            isLink = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clientConnection.isRead = false;
        clientConnection.onDisconnect();
    }

}
