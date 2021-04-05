package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private ConsoleServer server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(ConsoleServer server, Socket socket) {

        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                try{
                    // auth - /auth login pass
                    while (true){
                        String str = in.readUTF();
                        if(str.startsWith("/auth ")){
                            String[]tokens = str.split(" ");
                            String nick = AuthService.getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                            if(nick != null){
                                sendMsg("/ auth-OK");
                                setNickname(nick);
                                server.subscribe(ClientHandler.this);
                                break;
                            }else{
                                sendMsg("Wrong auth data");
                            }
                        }
                    }

                    while (true){
                        String str = in.readUTF();
                        if("/end".equals(str)){
                            out.writeUTF("/serverClosed");
                            System.out.printf("Client [%s] disconnected\n", socket.getInetAddress());
                            break;
                        }
                        System.out.printf("Client [%s] was send '%s'\n", socket.getInetAddress(), str);
                        //out.writeUTF(str);
                        server.broadcastMsg(nickname + ": " + str);
                    }
                }catch (IOException e){
                    e.printStackTrace();;
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
}
