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

    /** Обновляет таблицу blacklist в базе данных
     *
     * @param AddOrDetete - если Истина, то добавление, если ложь, то удаление
     */
    public static int updateBlacklistInDB(String nick, String blockedNick, Boolean AddOrDetete){
        PreparedStatement ps = null;
        String query;
        try{
            if(AddOrDetete){
                query = "INSERT INTO blacklist (user, blockedUser) VALUES (?, ?);";
            } else {
                query = "DELETE FROM blacklist WHERE user = ? AND blockedUser = ?;";
            }
            ps = connection.prepareStatement(query);
            ps.setString(1, nick);
            ps.setString(2, blockedNick);
            return ps.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            statementClose(ps);
        }
        return 0;
    }

    public static List<String> getBlacklist(String nick){
        List<String> blacklist = new ArrayList<>();
        String query = "SELECT blockedUser FROM blacklist WHERE user = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, nick);
            rs = ps.executeQuery();

            while(rs.next()){
                blacklist.add(rs.getString("blockedUser"));
            }
            return blacklist;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            resultSetClose(rs);
            statementClose(ps);
        }
        return blacklist;
    }

    /** Сохраняет сообщение в БД
     *
     * @param sender - никнейм отправителя
     * @param receiver - никнейм получателя
     * @param msg - сообщение
     */
    public static int saveMsgInDB(String sender, String receiver, String msg){
        String query = "INSERT INTO messageLog (sender, receiver, message) VALUES (?, ?, ?);";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setString(3, msg);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statementClose(ps);
        }
        return 0;
    }

    private static void resultSetClose(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void statementClose(PreparedStatement ps) {
        try {
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
