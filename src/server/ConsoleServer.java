package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ConsoleServer {
    private Vector<ClientHandler> users;

    public ConsoleServer() {
        users = new Vector<>();
        ServerSocket server = null; // сервер
        Socket socket = null; // хост

        try {
            AuthService.connect();
            server = new ServerSocket(6001);
            System.out.println("Server started");

            while (true){
                socket = server.accept();
                System.out.printf("Client [%s] try to connect\n", socket.getInetAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.printf("Client [%s] disconnected\n", socket.getInetAddress());
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void subscribe(ClientHandler client){
        users.add(client);
        System.out.printf("User [%s] connected\n", client.getNickname());
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client){
        users.remove(client);
        System.out.printf("User [%s] disconnected\n", client.getNickname());
        broadcastClientsList();
    }

    public boolean isUserLoggedIn(ClientHandler client){
        return users.contains(client);
    }

    public void broadcastMsg(ClientHandler from, String msg){
        for(ClientHandler client:users){
            if(!client.checkBlacklist(from.getNickname())){
                client.sendMsg(msg);
            }
        }
        AuthService.saveMsgInDB(from.getNickname(), "@everybody@", msg);
    }

    public void sendMsgToUser(ClientHandler sender, String targetNick, String msg) {
        for(ClientHandler client:users){
            if(targetNick.equals(client.getNickname())){
                if(!client.checkBlacklist(sender.getNickname())){
                    client.sendMsg(msg);
                    sender.sendMsg(msg);
                    AuthService.saveMsgInDB(sender.getNickname(), client.getNickname(), msg);
                }
                break;
            }
        }
    }

    private void broadcastClientsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientList ");
        for(ClientHandler c : users){
            sb.append(c.getNickname() + " ");
        }

        String out = sb.toString();
        for(ClientHandler c : users){
            c.sendMsg(out);
        }
    }
}
