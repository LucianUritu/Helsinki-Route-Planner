package UI.Components;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ResultsWindow {

    public VBox createResultsWindow() {
        DirectionsWindow directionsWindow = new DirectionsWindow();
        HBox answersHeader = directionsWindow.createHeader("YOUR DIRECTIONS");

        VBox directionsBox = new VBox(10);
        directionsBox.setStyle("-fx-background-color: white;");
        directionsBox.setPadding(new Insets(40, 20, 20, 20));
        directionsBox.setPrefWidth(250);

        VBox container = new VBox();
        container.getChildren().addAll(answersHeader, directionsBox);

        return container;
    }

}
