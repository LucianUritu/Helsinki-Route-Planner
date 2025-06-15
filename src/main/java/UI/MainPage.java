package UI;

import Backend.RoutingEngine;
import UI.Window.WindowManager;
import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainPage extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws CsvValidationException {
        RoutingEngine engine = new RoutingEngine();
        engine.loadDatabase("src/main/resources/hsl.zip");
        WindowManager windowManager = new WindowManager(primaryStage);
        windowManager.show();
    }
}
