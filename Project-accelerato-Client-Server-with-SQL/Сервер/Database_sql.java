import java.sql.*;
import java.util.ArrayList;

public class Database_sql {

    private String username = "root";
    private String password = "1q1q1q";
    private String url = "jdbc:mysql://localhost:3306/congo"+
            "?verifyServerCertificate=false"+
            "&useSSL=false"+
            "&requireSSL=false"+
            "&useLegacyDatetimeCode=false"+
            "&amp"+
            "&serverTimezone=UTC";

    public void save_messageToGlobal(String msg, String time, String nickname, String groupName, int groupID) {
        try {
            if (!msg.equals("null")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                     Statement statement = conn.createStatement()) {

                    statement.executeUpdate("insert into global_chat (author_id, text, time, group_name, group_id) values ('"+ nickname + "','" + msg + "','" + time + "','" + groupName + "', '"+ groupID +"')");

                }
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }

    }

    public void save_messageToPrivate(String msg, String time, String myLogin, String toLogin) {
        try {
            if (!msg.equals("null")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                     Statement statement = conn.createStatement()) {

                    statement.executeUpdate("insert into private_chat (author_id, message_to_id, text, time) values ('"+ myLogin + "','" + toLogin + "','" + msg + "','" + time + "')");

                    ResultSet resultSet = statement.executeQuery("select * from login_with_login where login = '"+ toLogin +"' and to_login = '"+ myLogin +"'");
                    if (!resultSet.next())
                        statement.executeUpdate("insert into login_with_login (login, to_login) values ('"+ toLogin  +"','"+ myLogin +"')");

                }
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }

    public void lastOnline(String login, String date){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                statement.executeUpdate("update accounts set last_online = '"+ date +"' where login = '"+ login +"'");

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }

    public void create_account(String login, String pass, String date){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                statement.executeUpdate("insert into accounts (login, password, data_reg) values ('" + login + "','" + pass + "','" + date + "')");

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }

    public void export_messageToGlobal(Connection connection, String groupName, int groupID){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url,username,password);
                 Statement statement = conn.createStatement()){

                ResultSet resultSet = statement.executeQuery("select * from global_chat where group_name = '"+ groupName +"' and group_id = '"+ groupID +"'");
                while (resultSet.next()){
                    connection.sendString("loadHistory",resultSet.getString("time") + "-" + resultSet.getString("author_id") + "-" + resultSet.getString("text"));
                }

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }

    }

    public void loadHistoryPrivate(Connection connection, String toLogin,String myLogin){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url,username,password);
                 Statement statement = conn.createStatement()){

                ResultSet resultSet = statement.executeQuery("select * from private_chat where (message_to_id = '"+toLogin+"' and author_id = '"+myLogin+"') or (message_to_id = '"+myLogin+"' and author_id = '"+toLogin+"')");
                while (resultSet.next()){
                    connection.sendString("loadHistoryPrivate",resultSet.getString("time") + "-" + resultSet.getString("author_id") + "-" + resultSet.getString("text"));
                }

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }

    }

    public boolean isUniqueLogin(String login){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url,username,password);
                 Statement statement = conn.createStatement()){

                ResultSet resultSet = statement.executeQuery("SELECT login FROM accounts WHERE login = '" + login +"'");

                return resultSet.next();

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLoginWithGroup(String login, String groupName, int groupID){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url,username,password);
                 Statement statement = conn.createStatement()){

                ResultSet resultSet = statement.executeQuery("SELECT * FROM login_with_group WHERE login = '" + login +"' and group_name = '"+ groupName +"' and groupID = '"+ groupID +"'");

                if (resultSet.next())
                    return true;

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLoginWithLogin(String login, String toLogin){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url,username,password);
                 Statement statement = conn.createStatement()){

                ResultSet resultSet = statement.executeQuery("SELECT * FROM private_chat WHERE message_to_id = '" + login +"' and author_id = '"+ toLogin +"'");

                return resultSet.next();

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLoginWithPassword(String login, String pass){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url,username,password);
                 Statement statement = conn.createStatement()){

                ResultSet resultSet = statement.executeQuery("SELECT login, password FROM accounts WHERE login = '" + login + "' and password = '" + pass + "'");
                if (resultSet.next()) return true;
                return false;
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return false;
        }

    }

    public void deleteSelfGroup(String login, String groupName, int groupID){

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                statement.executeUpdate("delete from login_with_group where login = '"+ login +"' and group_name = '"+ groupName +"' and groupID = '"+ groupID +"'");

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }

    }

    public void deletePrivateChat(String login, String toLogin){

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                statement.executeUpdate("delete from login_with_login where login = '"+ login +"' and to_login = '"+ toLogin +"'");
//                statement.executeUpdate("delete from login_with_login where login = '"+ toLogin +"' and to_login = '"+ login +"';");

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }

    }

    public void addUsersInGroup(ArrayList<String> logins, String groupName, int groupID, String date){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                logins.remove(logins.size() - 1);
                logins.remove(logins.size() - 1);

                String creator = null;

                ResultSet resultSet = statement.executeQuery("select creator from login_with_group where group_name = '"+ groupName +"' and groupID = '"+ groupID +"'");
                if (resultSet.next())
                    creator = resultSet.getString("creator");

                for (int i = 0; i < logins.size(); i++){
                    statement.executeUpdate("insert into login_with_group (login, group_name,groupID,date_create,creator) values ('" + logins.get(i) + "','" + groupName + "','" + groupID + "','"+ date +"','"+ creator +"')");
                }


            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }

    public int createLoginWithGroup(ArrayList<String> logins, String groupName, String creator,String date){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                logins.remove(logins.size() - 1);

                int groupID;
                ResultSet resultSet = statement.executeQuery("select group_name from login_with_group where group_name = '"+ groupName +"'");
                if (resultSet.next()) {
                    resultSet = statement.executeQuery("select max(groupID) from login_with_group where group_name = '"+ groupName +"'");
                    resultSet.next();
                    groupID = resultSet.getInt("max(groupID)") + 1;
                }else {
                    groupID = 0;
                }


                for (int i = 0; i < logins.size(); i++){
                    statement.executeUpdate("insert into login_with_group (login, group_name,groupID,date_create,creator) values ('" + logins.get(i) + "','" + groupName + "','" + groupID + "','"+ date +"','"+ creator +"')");
                }
                return groupID;
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    public String getGroupList(String login){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                String value = "";
                ResultSet resultSet = statement.executeQuery("select group_name,groupID from login_with_group where login = '" + login + "'");

                while (resultSet.next())
                    value = value + resultSet.getString("group_name") + "-" + resultSet.getInt("groupID") + ":";

                return value;
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return null;
        }

    }

    public String getPrivateList(String login){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                String value = "";
                ResultSet resultSet = statement.executeQuery("select to_login from login_with_login where login = '" + login + "'");

                while (resultSet.next())
                    value = value + resultSet.getString("to_login") + ":";

                return value;
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return null;
        }

    }

    public String getLoginList(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                String value = "";
                ResultSet resultSet = statement.executeQuery("select login from accounts");

                while (resultSet.next())
                    value = value + resultSet.getString("login") + ":";

                return value;
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return null;
        }

    }

    public void createLoginWithLogin(String login, String toLogin){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                ResultSet resultSet = statement.executeQuery("select * from login_with_login where login = '"+ login +"' and to_login = '"+ toLogin +"'");
                if (!resultSet.next())
                    statement.executeUpdate("insert into login_with_login (login, to_login) values ('"+ login  +"','"+ toLogin +"')");

                resultSet = statement.executeQuery("select * from login_with_login where login = '"+ toLogin +"' and to_login = '"+ login +"'");
                if (!resultSet.next())
                    statement.executeUpdate("insert into login_with_login (login, to_login) values ('"+ toLogin  +"','"+ login +"')");

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }

    }

    public String getLastGroup(String groupName){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                ResultSet resultSet = statement.executeQuery("select max(id) from global_chat where group_name = '"+ groupName +"'");
                int id = 0;
                if (resultSet.next())
                    id = resultSet.getInt("max(id)");
                else
                    return null;

                resultSet = statement.executeQuery("select text from global_chat where id = '"+ id +"'");

                if (resultSet.next())
                    return resultSet.getString("text");
                else
                    return null;

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public String getLastPrivate(String toLogin, String myLogin){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                ResultSet resultSet = statement.executeQuery("select max(id) from private_chat where  (author_id = '"+ toLogin +"' and message_to_id = '"+ myLogin+"') or (author_id = '"+ myLogin +"' and message_to_id = '"+ toLogin+"')");
                int id = 0;
                if (resultSet.next())
                    id = resultSet.getInt("max(id)");
                else
                    return null;

                resultSet = statement.executeQuery("select text from private_chat where id = '"+ id +"'");

                if (resultSet.next())
                    return resultSet.getString("text");
                else
                    return null;

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return null;

    }

    public String getExistGroup(String groupName, int groupID){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                String value = "";
                ResultSet resultSet = statement.executeQuery("select login from login_with_group where group_name = '"+ groupName +"' and groupID = '"+ groupID +"'");

                while (resultSet.next())
                    value = value + resultSet.getString("login") + ":";

                return value.substring(0,value.length()-1);
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public String getInfoGroup(String groupName, int groupID){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                String value = "";
                ResultSet resultSet = statement.executeQuery("select * from login_with_group where group_name = '"+ groupName +"' and groupID = '"+ groupID +"'");

                while (resultSet.next())
                    value = resultSet.getString("date_create") + "-" + resultSet.getString("creator") + "-" + resultSet.getString("groupID");

                return value.substring(0,value.length()-1);
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public synchronized String uploadSQL(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                ResultSet resultSet = statement.executeQuery("select DISTINCT group_name FROM login_with_group");

                ArrayList<String> existChats = new ArrayList<>();
                while (resultSet.next()){
                    existChats.add(resultSet.getString("group_name"));
                }

                resultSet = statement.executeQuery("select DISTINCT group_name FROM global_chat");

                ArrayList<String> allChats = new ArrayList<>();
                while (resultSet.next()){
                    allChats.add(resultSet.getString("group_name"));
                }

                for (int i = 0; i < allChats.size(); i++){
                    if (!existChats.contains(allChats.get(i))){
                        statement.executeUpdate("delete from global_chat where group_name = '"+ allChats.get(i) +"'");
                    }
                }

            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return null;
    }

}
