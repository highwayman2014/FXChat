package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.*;


public class Controller implements Initializable {

    @FXML
    TextArea chatArea;
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
    @FXML
    ListView<String> clientList;

    private boolean darkTheme = false;
    private boolean isAuthorized = false;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public static final String ADDRESS = "localhost";
    public static final int PORT = 6001;

    private List<TextArea> textAreas;

    @FXML
    void sendMsg(){
        try {
            if (!textField.getText().isEmpty()) {
                out.writeUTF(textField.getText());
                //chatArea.appendText("Вы: \t" + textField.getText() + "\n");
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

            setAuthorized(false);

            new Thread(() -> {
                try{

                    while (true){
                        String str = in.readUTF();
                        if("/ auth-OK".equals(str)){
                            setAuthorized(true);
                            chatArea.clear();
                            break;
                        }else{
                            for(TextArea ta:textAreas){
                                chatArea.appendText(str + "\n");
                            }
                        }
                    }

                    while (true){
                        String str = in.readUTF();
                        if("/serverClosed".equals(str)){
                            break;
                        }
                        if(str.startsWith("/clientList")){
                            String[] tokens = str.split(" ");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        clientList.getItems().add(tokens[i]);
                                    }
                                }
                            });
                        } else {
                            chatArea.appendText(str + "\n");
                        }
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
            chatArea.appendText("В соединении отказано\n");
        }
    }

    public void setAuthorized(boolean authorized) {
        this.isAuthorized = authorized;
        if(!isAuthorized){
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);

            messageBox.setVisible(false);
            messageBox.setManaged(false);

            clientList.setVisible(false);
            clientList.setManaged(false);

        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);

            messageBox.setVisible(true);
            messageBox.setManaged(true);

            clientList.setVisible(true);
            clientList.setManaged(true);
        }

    }

    public void tryAuth(ActionEvent actionEvent) {
        if(socket == null || socket.isClosed()){
            connect();

        }
        try {
            String login = loginField.getText();
            if(login.contains(" ")){
                chatArea.appendText("Имя пользователя не может содержать пробелы\n");

            } else {
                out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
                loginField.clear();
                passwordField.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        if (socket != null) {
            if(!socket.isClosed()) {
                try {
                    out.writeUTF("/end");
                    //socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void logUp(ActionEvent actionEvent) {
        RegistrationStage rs = new RegistrationStage(out);
        rs.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
        textAreas = new ArrayList<>();
        textAreas.add(chatArea);
    }


    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            MiniStage ms = new MiniStage(clientList.getSelectionModel().getSelectedItem(), out, textAreas);
            ms.show();
        }
    }
}
