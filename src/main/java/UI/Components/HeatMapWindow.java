package UI.Components;

import Backend.Algorithms.AStarHeatmap;
import Backend.Algorithms.DijkstraHeatmap;
import DataStructures.Coordinate;
import DataStructures.Node;
import Helper.Polygon.GridPolygonGenerator;
import Helper.Polygon.HelsinkiPolygonMap;
import UI.Components.Helper.*;
import UI.Window.WindowManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeatMapWindow {
    private static final String WINDOW_TITLE = "HEATMAP";
    private static final String FONT_FAMILY = "Raleway";
    private static final int FONT_SIZE = 20;
    private static Coordinate coordinate;
    private TextField coordinateField;
    private VBox heatmapBox;
    private Button generateButton;
    private Button resetButton;
    private Button closeStop;
    private TextField closeStopField;
    private TextField activeField;
    private ComboBox<String> timeOptions;
    private ComboBox<Integer> hours;
    private ComboBox<Integer> minutes;
    private ComboBox<Integer> gridHeatBox;
    private Label colon;
    private Label gridHeatMap;
    private HBox gridSizeBox;
    private VBox legendBox;
    private WaypointManager waypointManager;
    private WindowManager windowManager;
    private HeatmapPainter heatmapPainter2;
    private CustomWaypointPainter heatmapPainter1;
    private Map<Coordinate, Integer> heatmapData;
    private String selectedAlgorithm;
    private List<CustomWaypoint> closedStopWaypoints = new ArrayList<>();
    private DijkstraHeatmap currenDijkstraHeatmap;
    private boolean restartLoop = false;
    private List<Coordinate> closedStops =  new ArrayList<>();

    public HeatMapWindow() {
        this.heatmapData = new HashMap<>();
    }

    public static Coordinate getCoordinate() {
        return coordinate;
    }

    public static GeoPosition convertToGeoPosition(Coordinate coordinate) {
        if (coordinate == null) return null;
        return new GeoPosition(coordinate.getLatitude(), coordinate.getLongitude());
    }

    public void setSelectedAlgorithm(String algorithm) {
        this.selectedAlgorithm = algorithm;
        if (gridSizeBox != null) {
            gridSizeBox.setVisible(algorithm.equals("AStar"));
        }
    }

    public void setWaypointManager(WaypointManager waypointManager) {
        this.waypointManager = waypointManager;
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public VBox createLeftHeatmapWindow() {
        HBox heatmapHeader = createHeader(WINDOW_TITLE);
        coordinateField = createTextField("Enter coordinate");

        timeOptions = new ComboBox<>();
        timeOptions.getItems().addAll("Leave now", "Leave at: ...");
        timeOptions.setValue("Leave now");
        timeOptions.setStyle(UI.Components.Styles.UIStyles.TIMEOPTIONSTYLE);
        timeOptions.setPrefWidth(120);

        hours = new ComboBox<>();
        for (int i = 00; i < 24; i++) {
            hours.getItems().add(i);
        }
        hours.setStyle(UI.Components.Styles.UIStyles.HOURMINUTESTYLE);
        hours.setPrefWidth(25);

        colon = new Label();
        colon.setText(":");

        minutes = new ComboBox<>();
        for (int i = 00; i < 60; i++) {
            minutes.getItems().add(i);
        }
        minutes.setStyle(UI.Components.Styles.UIStyles.HOURMINUTESTYLE);
        minutes.setPrefWidth(25);

        HBox hourMinBox = new HBox(3, timeOptions, hours, colon, minutes);
        hourMinBox.setAlignment(Pos.CENTER_LEFT);
        hourMinBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        hours.setVisible(false);
        minutes.setVisible(false);
        colon.setVisible(false);

        timeOptions.setOnAction(event -> {
            String selection = timeOptions.getValue();
            boolean show = selection.equals("Leave at: ...");
            hours.setVisible(show);
            minutes.setVisible(show);
            colon.setVisible(show);
        });

        gridHeatBox = new ComboBox<>();

        for (int i = 1; i <= 10; i++) {
            gridHeatBox.getItems().add(i);
        }

        gridHeatMap = new Label("Grid Size:");
        gridHeatMap.setStyle("-fx-text-fill: rgb(132, 132, 132);");
        gridHeatBox.setPrefWidth(100);
        gridHeatBox.setValue(4);
        gridHeatBox.setStyle(UI.Components.Styles.UIStyles.HOURMINUTESTYLE);
        gridHeatBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        gridHeatBox.setOnAction(event -> {
            getGridInput();
        });

        gridSizeBox = new HBox(3, gridHeatMap, gridHeatBox);
        gridSizeBox.setAlignment(Pos.CENTER_LEFT);
        gridSizeBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        gridSizeBox.setPadding(new Insets(5, 10, 5, 10));
        gridSizeBox.setVisible(false);


        generateButton = new Button("Generate Heatmap");
        resetButton = new Button("Reset");
        closeStop = new Button("Close Stop");
        generateButton.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        resetButton.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        closeStop.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        generateButton.setPrefWidth(200);
        resetButton.setPrefWidth(200);
        closeStop.setPrefWidth(200);

        closeStop.setVisible(false);

        setupButtonActions();

        closeStopField = createTextField("Select point to close stop");
        closeStopField.setVisible(false);

        heatmapBox = new VBox(10);
        heatmapBox.getChildren().addAll(coordinateField, generateButton, resetButton, closeStopField, closeStop);
        heatmapBox.setStyle("-fx-background-color: white;");
        heatmapBox.setPadding(new Insets(20));
        heatmapBox.setPrefWidth(250);
        heatmapBox.setAlignment(Pos.TOP_CENTER);

        HeatmapLegend heatmapLegend = new HeatmapLegend();
        legendBox = heatmapLegend.createLegend();

        Button returnButton = new Button("Return to Main Menu");
        returnButton.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        returnButton.setStyle("-fx-background-color: white;");
        returnButton.setPrefWidth(200);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);


        returnButton.setOnAction(e -> {
            resetMap();
            windowManager.returnToMainMenu();
        });


        VBox container = new VBox(0);
        container.getChildren().addAll(heatmapHeader, heatmapBox, hourMinBox, legendBox, gridSizeBox, spacer, new VBox(20, returnButton));

        return container;
    }

    private int getGridInput() {
        return gridHeatBox.getValue();
    }

    private void setupButtonActions() {
        generateButton.setOnAction(e -> generateHeatmap());
        resetButton.setOnAction(e -> resetMap());
        closeStop.setOnAction(e -> handleCloseStop());
    }

    private int getSelectedTime() {
        if (timeOptions.getValue().equals("Leave now")) {
            return 600;
        }

        if (hours.getValue() == null || minutes.getValue() == null) {
            DirectionsWindow.showError("Please select both hour and minutes", "Time Selection Error");
            return 600;
        }

        int hour = hours.getValue();
        int minute = minutes.getValue();

        return (hour * 60 + minute);
    }

    private void generateHeatmap() {
        if (coordinateField.getText().isEmpty()) {
            DirectionsWindow.showError("Please enter coordinates.", "Input Error");
            return;
        }


        if (selectedAlgorithm.equals("AStar")) {
            int gridSize = getGridInput();
            List<Coordinate> helsinkiSquare = HelsinkiPolygonMap.getSquare();
            List<Coordinate> gridCells = new GridPolygonGenerator().generateGridPoints(helsinkiSquare, gridSize);

            coordinate = DirectionsWindow.stringToCoordinate(coordinateField.getText());

            int time = getSelectedTime();
            Node originNode = new Node(coordinate, time);

            AStarHeatmap heatmap = new AStarHeatmap(gridCells, originNode);
            heatmapData = heatmap.calculateHeatmap();

            heatmapPainter2 = new HeatmapPainter(gridCells, heatmapData, helsinkiSquare.get(0), helsinkiSquare.get(1), gridSize);

            updateMapOverlay();
            closeStop.setVisible(false);
            closeStopField.setVisible(false);
        } else {
            coordinate = DirectionsWindow.stringToCoordinate(coordinateField.getText());

            int time = getSelectedTime();
            currenDijkstraHeatmap = new DijkstraHeatmap(coordinate, time);
            heatmapPainter1 = new CustomWaypointPainter(currenDijkstraHeatmap.coordinatesEarliestArrival);

            updateMapOverlay();
            closeStop.setVisible(true);
            closeStopField.setVisible(true);
        }

        generateButton.setVisible(false);

    }

    private void updateMapOverlay() {
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>();
        List<Painter<JXMapViewer>> painters = new ArrayList<>();

        if (heatmapPainter1 != null) {
            painters.add(heatmapPainter1);
        } else if (heatmapPainter2 != null) {
            painters.add(heatmapPainter2);
        }

        painters.add(waypointManager.getPainter());

        painter.setPainters(painters);
        waypointManager.getMapViewer().setOverlayPainter(painter);
        waypointManager.updateMap();
    }

    private TextField createTextField(String title) {
        TextField textField = new TextField();
        textField.setPromptText(title);
        textField.setFont(Font.font(FONT_FAMILY, 10));
        textField.setStyle("-fx-control-inner-background: rgb(255, 255, 255);" +
                "-fx-text-fill: rgb(132, 132, 132);" +
                "-fx-border-color: rgb(8, 185, 255);");
        textField.setOnMouseClicked(e -> activeField = textField);
        return textField;
    }

    private HBox createHeader(String header) {
        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(10));
        headerBox.setStyle("-fx-background-color:rgb(126, 137, 218);");
        Label title = new Label(header);
        title.setFont(new Font(FONT_FAMILY, FONT_SIZE));
        title.setTextFill(Color.WHITE);
        headerBox.getChildren().add(title);
        return headerBox;
    }

    private void resetMap() {
        if (waypointManager != null) {
            waypointManager.clearAllWaypoints();
            closedStopWaypoints.clear();
        }

        if (heatmapPainter1 != null) {
            heatmapPainter1.clearHeatmap();
            updateMapOverlay();
        }

        if (heatmapPainter2 != null) {
            heatmapPainter2.clearHeatmap();
            updateMapOverlay();
        }

        closedStops = new ArrayList<>();
        restartLoop = true;
        currenDijkstraHeatmap = null;
        generateButton.setVisible(true);
        coordinateField.clear();
        closeStopField.clear();
        closeStopField.setVisible(false);
        closeStop.setVisible(false);
        activeField = null;
    }

    public TextField getActiveField() {
        return activeField;
    }

    public void updateWaypoint(GeoPosition position) {
        if (position != null) {
            String coords = String.format("%.6f, %.6f", position.getLatitude(), position.getLongitude());
            coordinateField.setText(coords);
            activeField = coordinateField;
        }
    }

        private void handleCloseStop() {
        generateButton.setVisible(false);
        if (activeField == null && closeStopField.getText() != null && !closeStopField.getText().isEmpty()) {
            try {
                Coordinate selectedCoord = DirectionsWindow.stringToCoordinate(closeStopField.getText());

                if (currenDijkstraHeatmap != null) {
                    Waypoint closed = currenDijkstraHeatmap.closestStop(selectedCoord);
                    if (closed != null) {
                        GeoPosition closedStop = closed.getPosition();
                        CustomWaypoint closedStopWaypoint = new CustomWaypoint(closedStop, CustomWaypoint.WaypointType.ROUTE);
                        closedStopWaypoints.add(closedStopWaypoint);
                        waypointManager.addClosedStopWaypoint(closedStopWaypoint);  
                        
                        closedStops.add(selectedCoord);
                        
                        currenDijkstraHeatmap = new DijkstraHeatmap(coordinate, getSelectedTime());
                        for(Coordinate toClose : closedStops) {
                            currenDijkstraHeatmap.eliminateClosestStop(toClose);
                        }
                        
                        heatmapPainter1 = new CustomWaypointPainter(currenDijkstraHeatmap.coordinatesEarliestArrival);
                        updateMapOverlay();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Stop Closed");
                        alert.setHeaderText(null);
                        alert.setContentText("Stop removed successfully. Heatmap was updated.");
                        alert.showAndWait();
                    }
                }
            } catch (Exception e) {
                DirectionsWindow.showError("Error removing stop: " + e.getMessage(), "Error");
            }
        } else {
            activeField = closeStopField;
            DirectionsWindow.showError("Please click on the map to select a stop to close", "Select Stop");
        }
    }

    public void setCoordinates(GeoPosition position) {
        if (activeField == null) return;

        String coords = String.format("%.6f, %.6f", position.getLatitude(), position.getLongitude());

        if (activeField == closeStopField) {
            closeStopField.setText(coords);
            waypointManager.setEndWaypoint(position);
            updateMapOverlay();

        } else if (activeField == coordinateField) {
            coordinateField.setText(coords);
            waypointManager.clearWaypoints();
            waypointManager.setStartWaypoint(position);
            updateMapOverlay();
        }

        activeField = null;
    }

}