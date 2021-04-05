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
                System.out.printf("Client [%s] connected\n", socket.getInetAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
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
    }

    public void unsubscribe(ClientHandler client){
        users.remove(client);
    }

    public void broadcastMsg(String msg){
        for(ClientHandler client:users){
            client.sendMsg(msg);
        }

    }

}
