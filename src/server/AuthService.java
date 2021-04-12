package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private static Connection connection;
    private static Statement statement;

    public static void connect(){

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public static int addUser(String login, String pass, String nickname) {
        try {
            String query = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nickname);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getNicknameByLoginAndPassword(String login, String password){
        String query = String.format("select nickname, password from users where login = '%s'", login);
        try {
            ResultSet rs = statement.executeQuery(query);
            int myHash = password.hashCode();

            if(rs.next()){
                String nick = rs.getString(1);
                int dbHash = rs.getInt(2);
                if(myHash == dbHash){
                    return nick;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getBlacklist(String nick){
        List<String> blacklist = new ArrayList<>();
        String query = String.format("select blockedUser from blacklist where user = '%s'", nick);
        try {
            ResultSet rs = statement.executeQuery(query);

            while(rs.next()){
                blacklist.add(rs.getString(1));
            }
            return blacklist;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blacklist;
    }

    public static void updateBlacklistInDB(String nick, String blockedNick){
        try {
            String query = "INSERT INTO blacklist (user, blockedUser) VALUES (?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nick);
            ps.setString(2, blockedNick);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveMsgInDB(String sender, String receiver, String msg){
        try {
            String query = "INSERT INTO messageLog (sender, receiver, message) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setString(3, msg);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
