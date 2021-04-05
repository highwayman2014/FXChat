package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Controller{

    @FXML
    TextArea textArea;
    @FXML
    TextField textField;
    @FXML
    VBox VBox;
    @FXML
    MenuItem menuChangeTheme;
    @FXML
    HBox upperPanel;
    @FXML
    HBox messageBox;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;

    private boolean darkTheme = false;
    private boolean isAuthorized = false;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public static final String ADDRESS = "localhost";
    public static final int PORT = 6001;

    @FXML
    void sendMsg(){
        try {
            if (!textField.getText().isEmpty()) {
                out.writeUTF(textField.getText());
                //textArea.appendText("Вы: \t" + textField.getText() + "\n");
                textField.clear();
            }
            textField.requestFocus();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    void changeTheme(){
        if (!darkTheme){
            VBox.getStylesheets().add("/styles/DarkTheme.css");
            darkTheme = true;
            menuChangeTheme.setText("Светлая");
        }else{
            VBox.getStylesheets().remove("/styles/DarkTheme.css");
            darkTheme = false;
            menuChangeTheme.setText("Темная");
        }
    }

    public void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try{

                    while (true){
                        String str = in.readUTF();
                        if("/ auth-OK".equals(str)){
                            setAuthorized(true);
                            textArea.clear();
                            break;
                        }else{
                            textArea.appendText(str + "\n");
                        }
                    }

                    while (true){
                        String str = in.readUTF();
                        if("/serverClosed".equals(str)){
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);
                };
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            textArea.appendText("В соединении отказано/n");
        }
    }

    public void setAuthorized(boolean authorized) {
        this.isAuthorized = authorized;
        if(!isAuthorized){
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            messageBox.setVisible(false);
            messageBox.setManaged(false);

        }else{
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            messageBox.setVisible(true);
            messageBox.setManaged(true);
        }

    }

    public void tryAuth(ActionEvent actionEvent) {
        if(socket == null || socket.isClosed()){
            connect();

        }
        try {
            String login = loginField.getText();
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            out.writeUTF("/end");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
