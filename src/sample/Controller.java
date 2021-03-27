package sample;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class Controller {

    boolean darkTheme = false;

    @FXML
    TextArea textArea;
    @FXML
    TextField textField;
    @FXML
    VBox VBox;
    @FXML
    MenuItem menuChangeTheme;

    @FXML
    void sendMsg(){
        if (!textField.getText().isEmpty()){
            textArea.appendText("Вы: \t" + textField.getText() + "\n");
            textField.clear();
        }
        textField.requestFocus();
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

}
