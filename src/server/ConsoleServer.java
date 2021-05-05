package server;

import com.sun.xml.internal.ws.api.ha.StickyFeature;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ConsoleServer {
    private Vector<ClientHandler> users;
    private static final Logger LOGGER = Logger.getLogger(ConsoleServer.class);

    public ConsoleServer() {
        users = new Vector<>();
        ServerSocket server = null; // сервер
        Socket socket = null; // хост

        try {
            AuthService.connect();
            server = new ServerSocket(6001);
            //System.out.println("Server started\n");
            LOGGER.info("Server started");

            while (true){
                socket = server.accept();
                //System.out.printf();
                LOGGER.info(String.format("Client [%s] try to connect", socket.getInetAddress()));
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //System.out.printf("Client [%s] disconnected\n", socket.getInetAddress());
                LOGGER.info(String.format("Client [%s] disconnected", socket.getInetAddress()));
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
        //System.out.printf("User [%s] connected\n", client.getNickname());
        LOGGER.info(String.format("User [%s] connected", client.getNickname()));
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client){
        users.remove(client);
        //System.out.printf("User [%s] disconnected\n", client.getNickname());
        LOGGER.info(String.format("User [%s] disconnected", client.getNickname()));
        broadcastClientsList();
    }

    public boolean isUserLoggedIn(ClientHandler client){
        return users.contains(client);
    }

    public void broadcastMsg(ClientHandler from, String msg){
        for(ClientHandler client:users){
            if(client.checkBlacklist(from.getNickname())){
                client.sendMsg(msg);
                client.logMessage(msg);
            }
        }
        LOGGER.info(String.format("User [%s] send to everyone: [%s]",
                from.getNickname(),
                msg));
        // Сохраним общее сообщение в виде одной записи
        if (AuthService.saveMsgInDB(from.getNickname(), "@everyone@", msg) == 0){
            //System.out.println("Error writing message in DB\n");
            LOGGER.error("Error writing message in DB");
        }
    }

    public void sendMsgToUser(ClientHandler sender, String targetNick, String msg) {
        for(ClientHandler client:users){
            if(targetNick.equals(client.getNickname())){
                // Проверим черный список перед отправкой сообщения
                if(client.checkBlacklist(sender.getNickname())){
                    client.sendMsg(msg);
                    client.logMessage(msg);
                    sender.sendMsg(msg);
                    sender.logMessage(msg);
                    LOGGER.info(String.format("User [%s] send to [%s]: [%s]",
                            sender.getNickname(),
                            client.getNickname(),
                            msg));
                    if (AuthService.saveMsgInDB(sender.getNickname(), client.getNickname(), msg) == 0){
                        //System.out.println("Error writing message in DB\n");
                        LOGGER.error("Error writing message in DB");
                    }
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
