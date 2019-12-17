import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class Server{

    private final ArrayList<Connection> connections = new ArrayList<>();
    private final SimpleDateFormat formatForDateNow;
    private Database_sql database_sql;

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        System.out.println("Server running...");
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        formatForDateNow = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        database_sql = new Database_sql();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    try {
//                        database_sql.uploadSQL();
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        System.out.println(e.toString());
//                    }
//
//                }
//            }
//        }).start();

        try (ServerSocket serverSocket = new ServerSocket(8020)) {
            while (true) {
                try {
                    new Connection(serverSocket.accept(), this);
                } catch (IOException e) {
                    System.out.println("Connection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void onConnectionReady(Connection connection) {
        connections.add(connection);
        ViewOnConsole("Client connected: " + connection);
    }

    public synchronized void onReceiveString(Connection connection, String keyValue) {
        if (keyValue == null) return;
        String key = "";
        String value = "";
        for (int i = 0; i != keyValue.length(); i++) {
            if (keyValue.charAt(i) == '-') {
                key = keyValue.substring(0, i);
                value = keyValue.substring(i + 1);
                break;
            }
        }

        if (key.equals("message")) {

            String[] responses = value.split(":");
            String nickname = responses[0];
            String groupName = responses[1];
            String message = responses[2];
            String groupID = responses[3];
            ViewOnConsole(nickname + " отправил сообщение в чат " + groupName + " : " + message);

            database_sql.save_messageToGlobal(message, formatForDateNow.format(new Date()), nickname, groupName, Integer.parseInt(groupID));

            for (Connection connection1 : connections) {
                if (database_sql.isLoginWithGroup(connection1.LOGIN, groupName, Integer.parseInt(groupID)))
                    connection1.sendString("message",groupName + ":" + groupID+ ":" + formatForDateNow.format(new Date()) + "-" + nickname + "-" + message);
            }

        }
        else if (key.equals("authorization")) {
            String[] responses = value.split(":");
            String login = responses[0];
            String password = responses[1];

            if (database_sql.isLoginWithPassword(login, password)){
                connection.sendString("authorization","true");
                connection.LOGIN = login;
                ViewOnConsole(connection + " - вход под логином - " + connection.LOGIN);
            }

            else
                connection.sendString("authorization","false");

        }
        else if (key.equals("listening")) {


        }
        else if (key.equals("disconnect")) {
            connection.disconnect();
            onDisconnect(connection);
        }
        else if (key.equals("stopReading")) {
            connection.sendString("stopReading","");
        }
        else if (key.equals("registration")) {
            String[] responses = value.split(":");
            String login = responses[0];
            String password = responses[1];

            if (!database_sql.isUniqueLogin(login)) {
                database_sql.create_account(login, password, formatForDateNow.format(new Date()));
                connection.sendString("registration","true");
                connection.LOGIN = login;
                ViewOnConsole(connection + " - Зарегестрировался новый пользователь - " + connection.LOGIN);
            } else {
                connection.sendString("registration","false");
            }
        }
        else if (key.equals("userExist")) {

            if (database_sql.isUniqueLogin(value)) {
                connection.sendString("userExist","true");
            } else {
                connection.sendString("userExist","false");
            }

        }
        else if (key.equals("loadHistory")) {

//            ViewOnConsole("Запрос на загрузку истории - " + value);

            String[] strings = value.split(":");

            database_sql.export_messageToGlobal(connection, strings[0],Integer.parseInt(strings[1]));
            connection.sendString("loadHistory","");

        }
        else if (key.equals("createGroup")) {

            String[] loginArr = value.split(":");
            ArrayList<String> logins = new ArrayList<>();
            Collections.addAll(logins, loginArr);

            connection.sendString("createGroup", database_sql.createLoginWithGroup(logins, loginArr[loginArr.length - 1], loginArr[0], formatForDateNow.format(new Date())) + "");


        }
        else if (key.equals("getGroupList")) {

            connection.sendString("getGroupList",database_sql.getGroupList(value));

        }
        else if (key.equals("deleteSelfGroup")) {

            String[] strings = value.split(":");

            database_sql.deleteSelfGroup(connection.LOGIN,strings[0],Integer.parseInt(strings[1]));
        }
        else if (key.equals("getLoginList")) {

            connection.sendString("getLoginList", database_sql.getLoginList());

        }
        else if (key.equals("createPrivateChat")) {

            String[] loginArr = value.split(":");
            database_sql.createLoginWithLogin(loginArr[0],loginArr[1]);

        }
        else if (key.equals("getPrivateList")) {
            connection.sendString("getPrivateList",database_sql.getPrivateList(value));
        }
        else if (key.equals("messagePrivate")) {
            String[] responses = value.split(":");
            String toLogin = responses[0];
            String message = responses[1];
            ViewOnConsole(connection.LOGIN + " отправил сообщение пользователю " + toLogin + " : " + message);

            database_sql.save_messageToPrivate(message, formatForDateNow.format(new Date()),connection.LOGIN,toLogin);

            for (Connection connection1 : connections) {
                if (connection1.LOGIN != null &&(connection1.LOGIN.equals(toLogin) || connection1.LOGIN.equals(connection.LOGIN)))
                    connection1.sendString("messagePrivate",connection.LOGIN + ":" + formatForDateNow.format(new Date()) + "-" + connection.LOGIN + "-" + message);
            }
        }
        else if (key.equals("loadHistoryPrivate")) {

            database_sql.loadHistoryPrivate(connection, value,connection.LOGIN);
            connection.sendString("loadHistoryPrivate","");

        }
        else if (key.equals("deletePrivateChat")) {

            database_sql.deletePrivateChat(connection.LOGIN,value);

        }
        else if (key.equals("getLastGroup")) {
            String str = database_sql.getLastGroup(value);
            if (str == null)
                str = " ";
            connection.sendString("getLastGroup", str);
        }
        else if (key.equals("getLastPrivate")) {
            String str = database_sql.getLastPrivate(value,connection.LOGIN);
            if (str == null)
                str = " ";
            connection.sendString("getLastPrivate", str);
        }
        else if (key.equals("myLogin")) {
            connection.LOGIN = value;
        }
        else if (key.equals("getExistGroup")) {
            String[] strings = value.split(":");
            connection.sendString("getExistGroup",database_sql.getExistGroup(strings[0],Integer.parseInt(strings[1])));
        }
        else if (key.equals("getInfoGroup")) {
            String[] strings = value.split(":");
            connection.sendString("getInfoGroup",database_sql.getInfoGroup(strings[0],Integer.parseInt(strings[1])));
        }
        else if (key.equals("addUsersInGroup")) {

            String strings[] = value.split(":");
            int size = strings.length;
            ArrayList logins = new ArrayList();
            Collections.addAll(logins, strings);

            database_sql.addUsersInGroup(logins,strings[size-2],Integer.parseInt(strings[size-1]),formatForDateNow.format(new Date()));
        }
        else if (key.equals("deleteUser")) {

            String[] strings = value.split(":");

            database_sql.deleteSelfGroup(strings[0],strings[1],Integer.parseInt(strings[2]));
        }
        else if (key.equals("lastOnline")) {
            exitLogin(connection);
        }
        else if (key.equals("")) {

        }
    }

    public synchronized void exitLogin(Connection connection){
        database_sql.lastOnline(connection.LOGIN, formatForDateNow.format(new Date()));
    }

    public synchronized void onDisconnect(Connection connection) {
        exitLogin(connection);
        connections.remove(connection);
        System.out.println(formatForDateNow.format(new Date()) + "  " + "Client disconnected: " + connection);
    }


    private void ViewOnConsole(String value) {
        if (value != null) {
            System.out.println(formatForDateNow.format(new Date()) + "  " + value);
        }
    }

}
