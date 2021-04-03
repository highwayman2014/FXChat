package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Чат");
        primaryStage.setScene(new Scene(root, 415, 500));
        //primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();

        //Перемещение окна
        //com.sun.glass.ui.Window.getWindows().get(0).setUndecoratedMoveRectangle(22);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
