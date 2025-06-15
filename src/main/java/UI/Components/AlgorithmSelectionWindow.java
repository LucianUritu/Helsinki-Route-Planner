package UI.Components;

import UI.Window.WindowManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class AlgorithmSelectionWindow {
    private static final String FONT_FAMILY = "Raleway";
    private static final int FONT_SIZE = 20;
    private WindowManager windowManager;
    private DirectionsWindow directionsWindow;
    private HeatMapWindow heatMapWindow;
    private boolean isHeatMap;

    public AlgorithmSelectionWindow(WindowManager windowManager, DirectionsWindow directionsWindow, HeatMapWindow heatMapWindow) {
        this.windowManager = windowManager;
        this.directionsWindow = directionsWindow;
        this.directionsWindow.setWindowManager(windowManager);
        this.heatMapWindow = heatMapWindow;
        this.isHeatMap = true;
    }

    public void setIsHeatMap(boolean isHeatMap) {
        this.isHeatMap = isHeatMap;
    }

    public VBox createAlgorithmSelectionWindow() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));

        String headerText = isHeatMap ? "Select Heatmap Algorithm" : "Select Routing Algorithm";
        HBox headerBox = createHeader(headerText);

        Button aStarButton = createAlgorithmButton("A*");
        Button dijkstraButton = createAlgorithmButton("Dijkstra's");

        VBox buttonBox = new VBox(20, aStarButton, dijkstraButton);
        buttonBox.setAlignment(Pos.CENTER);

        container.getChildren().addAll(headerBox, buttonBox);

        aStarButton.setOnAction(e -> handleAlgorithmSelection("AStar"));
        dijkstraButton.setOnAction(e -> handleAlgorithmSelection("Dijkstra"));


        return container;
    }

    private void handleAlgorithmSelection(String algorithm) {
        if (isHeatMap) {
            heatMapWindow.setSelectedAlgorithm(algorithm);
            windowManager.showHeatmapWindow();
        } else {
            directionsWindow.setSelectedAlgorithm(algorithm);
            windowManager.showDirectionsWindow();
        }
    }

    private Button createAlgorithmButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(150);
        button.setPrefHeight(30);
        button.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        button.setStyle("-fx-control-inner-background: #f5f5f5;" +
                "-fx-font-size: 14px;");
        return button;
    }

    private HBox createHeader(String header) {
        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(10));
        headerBox.setStyle("-fx-background-color:rgb(126, 137, 218);");
        headerBox.setAlignment(Pos.CENTER);

        Label title = new Label(header);
        title.setFont(new Font(FONT_FAMILY, FONT_SIZE));
        title.setTextFill(Color.WHITE);
        headerBox.getChildren().add(title);

        return headerBox;
    }
}