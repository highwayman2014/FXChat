package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Чат");
        primaryStage.setScene(new Scene(root, 415, 500));
        //primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Controller controller = loader.getController();
                controller.disconnect();
            }
        });
        primaryStage.show();

        //Перемещение окна
        //com.sun.glass.ui.Window.getWindows().get(0).setUndecoratedMoveRectangle(22);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
