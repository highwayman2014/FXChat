package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientHandler {
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    private List<String> blackList;

    public ClientHandler(ConsoleServer server, Socket socket) {

        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new ArrayList<>();
            // lalala

            new Thread(()->{
                boolean isExit = false;
                try{
                    socket.setSoTimeout(120_000);
                    // auth - /auth login pass
                    while (true){
                        String str = in.readUTF();
                        if(str.startsWith("/auth")){
                            String[]tokens = str.split(" ");
                            String nick = AuthService.getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                            if(nick == null){
                                sendMsg("Логин или пароль неверны\n");
                            }else{
                                setNickname(nick);
                                if(server.isUserLoggedIn(this)){
                                    sendMsg("Данный пользователь уже подключен\n");
                                }else {
                                    sendMsg("/ auth-OK");
                                    // заполнение черного списка из БД
                                    this.blackList = AuthService.getBlacklist(nick);
                                    socket.setSoTimeout(0);
                                    server.subscribe(ClientHandler.this);
                                    break;
                                }
                            }
                        }
                        // регистрация
                        if (str.startsWith("/signup ")) {
                            String[] tokens = str.split(" ");
                            int result = AuthService.addUser(tokens[1], tokens[2], tokens[3]);
                            if (result > 0) {
                                sendMsg("Регистрация прошла успешно");
                            } else {
                                sendMsg("При регистрации произошла ошибка");
                            }
                        }
                        if("/end".equals(str)){
                            isExit = true;
                        }
                    }

                    if(!isExit){
                        while (true){
                            String str = in.readUTF();
                            if(str.startsWith("/") || str.startsWith("@")){
                                if("/end".equals(str)){
                                    out.writeUTF("/serverClosed");
                                    System.out.printf("Client [%s] disconnected\n", socket.getInetAddress());
                                    break;
                                }else if (str.startsWith("@")){
                                    int firstSpaceIndex = str.indexOf(" ");
                                    if(firstSpaceIndex > 0){
                                        String targetNick = str.substring(1, firstSpaceIndex);
                                        server.sendMsgToUser(this, targetNick, nickname
                                                + ": [Отправлено для " + targetNick + "] "
                                                + str.substring(firstSpaceIndex + 1));
                                    }
                                } else if("/blacklist".equals(str)){
                                    String[] tokens = str.split(" ");
                                    if(blackList.contains(tokens[1])){
                                        if(AuthService.updateBlacklistInDB(nickname, tokens[1], false) == 1){
                                            blackList.remove(tokens[1]);
                                            sendMsg("Вы исключили пользователя " + tokens[1] + " из черного списка");
                                        } else {
                                            sendMsg("Произошла ошибка при операции с базой данных");
                                        }
                                    } else {
                                        if(AuthService.updateBlacklistInDB(nickname, tokens[1], true) == 1){
                                            blackList.add(tokens[1]);
                                            sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                                        } else {
                                            sendMsg("Произошла ошибка при операции с базой данных");
                                        }
                                    }
                                }
                            } else {
                                server.broadcastMsg(this, nickname + ": " + str);
                            }
                            System.out.printf("Client [%s] was send '%s' to %s\n", socket.getInetAddress(), str, "everybody");
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.unsubscribe(this);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setNickname(String nick) {
        this.nickname = nick;
    }

    public void sendMsg(String msg){
        try{
            out.writeUTF(msg);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientHandler that = (ClientHandler) o;
        return nickname.equals(that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname);
    }

    public String getNickname() {
        return nickname;
    }

    public boolean checkBlacklist(String nickname) {
        return !blackList.contains(nickname);
    }
}
