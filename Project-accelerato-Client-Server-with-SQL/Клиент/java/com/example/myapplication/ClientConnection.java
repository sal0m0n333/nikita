package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.util.ArraySet;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

public class ClientConnection extends Service {

    //final String LISTENING = "listening";
    final String MESSAGE = "message";
    final String MY_LOGIN = "myLogin";
    final String DELETE_USER = "deleteUser";
    final String GET_EXIST_GROUP = "getExistGroup";
    final String ADD_USERS_IN_GROUP = "addUsersInGroup";
    final String GET_INFO_GROUP = "getInfoGroup";
    final String MESSAGE_PRIVATE = "messagePrivate";
    final String AUTHORIZATION = "authorization";
    final String DISCONNECT = "disconnect";
    //final String STOPREADING = "stopReading";
    final String REGISTRATION = "registration";
    //final String USEREXIST = "userExist";
    final String LOADHISTORY = "loadHistory";
    final String LOAD_HISTORY_PRIVATE = "loadHistoryPrivate";
    final String CREATE_GROUP = "createGroup";
    final String CREATE_PRIVATE_CHAT = "createPrivateChat";
    final String GET_GROUP_LIST = "getGroupList";
    final String GET_PRIVATE_LIST = "getPrivateList";
    final String GET_LOGIN_LIST = "getLoginList";
    final String DELETE_SELF_GROUP = "deleteSelfGroup";
    final String LAST_ONLINE = "lastOnline";
    final String DELETE_PRIVATE_CHAT = "deletePrivateChat";
//    final String CREATE_CHAT = "createChat";
//    final String DELETE_CHAT = "deleteChat";
    //final String GET_LAST_GROUP = "getLastGroup";
    //final String GET_LAST_PRIVATE = "getLastPrivate";
    String LOGIN;
    private final String IP_ADDR = "212.16.19.18";
    private final int PORT = 8020;

    Socket socket;
    BufferedReader in;
    BufferedWriter out;
    boolean isRead = true;

    ArrayList<HistoryChat> historyChats;
    ArraySet<String> groupListID = new ArraySet<>();
    ArraySet<String> privateListID = new ArraySet<>();
    KeyValueString keyValueString = new KeyValueString();

    class LocalBinder extends Binder {
        ClientConnection getService() {
            return ClientConnection.this;
        }
    }

    public void uploadHistory(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("INTERNET", "uploadHistory() - getGroupList - start");
                if (!getGroupList())
                    return;
                Log.d("INTERNET", "uploadHistory() - getGroupList - stop");
                ArrayList<HistoryChat> tmpHistoryChats = new ArrayList<>();
                String string;
                int j = 0;

                Log.d("INTERNET", "uploadHistory() - groupListID - start");
                for (int i = 0; i < groupListID.size();i++,j++) {
                    tmpHistoryChats.add(new HistoryChat(groupListID.valueAt(i), "public"));
                    if(!sendString(LOADHISTORY, tmpHistoryChats.get(i).getNameChat() + ":" + tmpHistoryChats.get(i).getGroupID())){
                        return;
                    }

                    while (true) {
                        Log.d("INTERNET", "uploadHistory() - groupListID - while (true)");
                        string = null;
                        while (string == null){
                            Log.d("INTERNET", "uploadHistory() - groupListID - while (string == null)");
                            string = keyValueString.get(LOADHISTORY);
                            if (string == null && (!isOnline(ClientConnection.this))){
                                return;
                            }
                        }

                        if (string.equals(""))
                            break;
                        tmpHistoryChats.get(j).addHistory(string);
                    }
                }
                Log.d("INTERNET", "uploadHistory() - groupListID - stop");
                Log.d("INTERNET", "uploadHistory() - privateListID - start");
                for (int i = 0; i < privateListID.size();i++,j++) {
                    tmpHistoryChats.add(new HistoryChat(privateListID.valueAt(i), "private"));
                    if (!sendString(LOAD_HISTORY_PRIVATE, privateListID.valueAt(i))){
                        return;
                    }
                    while (true) {
                        Log.d("INTERNET", "uploadHistory() - privateListID - while (true)");
                        string = null;
                        while (string == null)
                        {
                            string = keyValueString.get(LOAD_HISTORY_PRIVATE);
                            if (string == null && (!isOnline(ClientConnection.this))){
                                return;
                            }
                        }

                        if (string.equals(""))
                            break;
                        tmpHistoryChats.get(j).addHistory(string);
                    }
                }
                Log.d("INTERNET", "uploadHistory() - privateListID - stop");

                if(isOnline(ClientConnection.this)){
                    historyChats = tmpHistoryChats;
                }

            }
        });
        t.start();

        while (t.isAlive())
            SystemClock.sleep(10);
    }

    public static boolean isOnline(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createClient();
    }


    void createClient(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        socket = new Socket(IP_ADDR, PORT);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
                        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
                        readString();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void onDisconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendString(DISCONNECT, " ");
                try {
                    socket.close();
                    stopSelf();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public synchronized boolean sendString(final String KEY, final String value) {
            try {
                out.write(KEY + "-" + value + "\r\n");
                out.flush();
                return true;
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        return false;
    }

    public void readString() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRead) {

                    String keyValue;
                    try {
                        keyValue = in.readLine();

                        if (keyValue == null){

                            try {
                                if (isRead == false) return;
                                socket = new Socket(IP_ADDR, PORT);
                                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
                                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
                                sendString(MY_LOGIN,LOGIN);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                            continue;
                        }


                        String key = "";
                        String value = "";
                        for (int i = 0; i != keyValue.length(); i++) {
                            if (keyValue.charAt(i) == '-') {
                                key = keyValue.substring(0, i);
                                value = keyValue.substring(i + 1);
                                break;
                            }
                        }
                        keyValueString.add(key,value);
                    } catch (IOException e) {
                        try {
                            if (isRead == false) return;
                            socket = new Socket(IP_ADDR, PORT);
                            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
                            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
                            sendString(MY_LOGIN,LOGIN);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public boolean getGroupList() {
        final boolean[] isGet = {true};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String lastMessage;
                lastMessage = null;
                if (!sendString(GET_GROUP_LIST, LOGIN)){
                    isGet[0] = false;
                    return;
                }

                while (lastMessage == null){
                    Log.d("INTERNET", "getGroupList() - lastMessage == null");
                    lastMessage = keyValueString.get(GET_GROUP_LIST);

                    if (lastMessage == null && (!isOnline(ClientConnection.this))){
                        return;
                    }
                }


                if (lastMessage.length() != 0) {
                    lastMessage = lastMessage.substring(0, lastMessage.length() - 1);

                    String groups[];
                    groups = lastMessage.split(":");

                    if (groups.length != groupListID.size())
                        groupListID.clear();
                    Collections.addAll(groupListID, groups);
                }
                else
                    groupListID.clear();
                lastMessage = null;
                if (!sendString(GET_PRIVATE_LIST, LOGIN)){
                    isGet[0] = false;
                    return;
                }
                while (lastMessage == null) {
                    lastMessage = keyValueString.get(GET_PRIVATE_LIST);
                    if (lastMessage == null && (!isOnline(ClientConnection.this))){
                        return;
                    }
                }

                if (lastMessage.length() != 0) {
                    lastMessage = lastMessage.substring(0, lastMessage.length() - 1);

                    String privates[];
                    privates = lastMessage.split(":");

                    if (privates.length != privateListID.size())
                        privateListID.clear();
                    Collections.addAll(privateListID, privates);
                }else
                    privateListID.clear();
            }
        });
        thread.start();

        while (thread.isAlive())
            SystemClock.sleep(10);

        return isGet[0];

    }

    public String getLoginList() {
        final String[] logins = {null};
        final String[] lastMessage = new String[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                lastMessage[0] = null;
                if (!sendString(GET_LOGIN_LIST, ""))
                    return;
                while (lastMessage[0] == null)
                    lastMessage[0] = keyValueString.get(GET_LOGIN_LIST);
                if (lastMessage[0].equals("")) return;

                lastMessage[0] = lastMessage[0].substring(0, lastMessage[0].length() - 1);
                logins[0] = lastMessage[0];

            }
        });
        thread.start();


        while (thread.isAlive())
            SystemClock.sleep(10);

        return logins[0];
    }

}
