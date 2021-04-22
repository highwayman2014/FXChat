package server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileLog {

    File msgLog;

    public FileLog(ClientHandler client){

        this.msgLog = new File("src/message_log/history_" + client.getNickname() + ".txt");
        if(!this.msgLog.exists()){
            try {
                this.msgLog.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        printLastLines(client);
    }

    /** Выводит в окно клиента последние 100 сообщений
     *
     * @param client - текущий клиент
     */
    private void printLastLines(ClientHandler client) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(msgLog))){
            String line;
            while((line = in.readLine()) != null){
                lines.add(line);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        for (int i = lines.size() - 1; (i >= 0) && (i >= lines.size() - 100); i--) {
            client.sendMsg(lines.get(i));
        }
    }

    /** Записывает сообщение в лог-файл пользователя
     *
     * @param msg - строка сообщения
     */
    public void writeMessageInLog(String msg){
        try (BufferedWriter out = new BufferedWriter(new FileWriter(msgLog, true))){
            out.write(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
