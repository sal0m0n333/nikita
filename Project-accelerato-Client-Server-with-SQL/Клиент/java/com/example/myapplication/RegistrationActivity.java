package com.example.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistrationActivity extends Activity {

    EditText nickname;
    EditText pass;
    EditText passAgain;
    Button sign_up;

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
        setContentView(R.layout.registration_layout);

        nickname = (EditText)findViewById(R.id.login_edit);
        pass = (EditText)findViewById(R.id.pass_edit);
        passAgain = (EditText)findViewById(R.id.pass_again_edit);
        sign_up = (Button)findViewById(R.id.sign_up_btn);
    }

    public void onClickSignUp(View view) {

        if (nickname.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Введите логин!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.getText().toString().length() >= 6) {
            if (!(pass.getText().toString().contains(":") || pass.getText().toString().contains("-"))) {
                if (pass.getText().toString().equals(passAgain.getText().toString())) {
                    if (!(nickname.getText().toString().contains(":") || nickname.getText().toString().contains("-"))) {

                        sign_up.setEnabled(false);
                        new Registration().execute();

                    }else
                        Toast.makeText(getApplicationContext(), "Логин не должен содержать символы ':' или '-'", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), "Пароль не должен содержать символы ':' или '-'", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Пароль должен состоять минимум из 6 символов", Toast.LENGTH_SHORT).show();
    }

    class Registration extends AsyncTask<Void , Integer, Void> {
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values[0] == 1) {
                Intent intent = new Intent(RegistrationActivity.this, MainScreenActivity.class);
                RegistrationActivity.this.finish();
                startActivity(intent);
            } else if (values[0] == 0) {
                sign_up.setEnabled(true);
                Toast.makeText(RegistrationActivity.this, "Логин занят", Toast.LENGTH_SHORT).show();
            } else {
                sign_up.setEnabled(true);
                Toast.makeText(RegistrationActivity.this, "Нет соединения с сервером", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... param) {
            int isReg;
            String lastMessage = null;
            if (!clientConnection.sendString(clientConnection.REGISTRATION, nickname.getText().toString() + ":" + pass.getText().toString())){
                isReg = -1;
                publishProgress(isReg);
                return null;
            }
            while (lastMessage == null)
                lastMessage = clientConnection.keyValueString.get(clientConnection.REGISTRATION);

            if (lastMessage.equals("true")) {
                clientConnection.LOGIN = nickname.getText().toString();
                isReg = 1;
            } else
                isReg = 0;

            publishProgress(isReg);

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
        if(isLink) {
            unbindService(serviceConnection);
            isLink = false;
        }
    }

}
