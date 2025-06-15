package UI.Components;

import Backend.RoutingEngine;
import DataStructures.Coordinate;
import DataStructures.Node;
import DataStructures.Route;
import Database.GtfsLoader;
import UI.Components.Helper.RoutePainter;
import UI.Components.Helper.WaypointManager;
import UI.Window.WindowManager;
import com.opencsv.exceptions.CsvValidationException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class DirectionsWindow {
    private static final String FONT_FAMILY = "Raleway";
    private static final int FONT_SIZE = 20;
    private String directionsTitle = "GET DIRECTIONS";
    private TextField startField;
    private TextField endField;
    private ComboBox<String> timeOptions;
    private ComboBox<Integer> hours;
    private ComboBox<Integer> minutes;
    private Label colon;
    private VBox directionsBox;
    private Button goButton;
    private Button resetButton;
    private Button displayStopsButton;
    private String selectedAlgorithm;

    private RoutePainter routePainter;
    private TextField activeField = null; //Track which field is active
    private WaypointManager waypointManager;
    private TextArea routeInformation;
    private WindowManager windowManager;


    public DirectionsWindow() {
    }

    public static Coordinate stringToCoordinate(String coordinateString) {
        String[] split = coordinateString.trim().split(",");
        if (split.length == 2) {
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            return new Coordinate(x, y);
        } else if (split.length == 4) {
            String xCord = String.join(".", List.of(split[0], split[1]));
            String yCord = String.join(".", List.of(split[2], split[3]));
            double x = Double.parseDouble(xCord);
            double y = Double.parseDouble(yCord);
            return new Coordinate(x, y);
        }
        throw new IllegalArgumentException("Inconsistent coordinate format!");
    }

    public static void showError(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setWaypointManager(WaypointManager waypointManager) {
        this.waypointManager = waypointManager;
        this.routePainter = new RoutePainter();
        waypointManager.setRoutePainter(routePainter);
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public VBox createLeftDirectionsWindow() {
        HBox directionsHeader = createHeader(directionsTitle);

        startField = createTextField("Start");
        endField = createTextField("End");

        timeOptions = new ComboBox<>();
        timeOptions.getItems().addAll("Leave now", "Leave at ...");
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
        for (int j = 0; j < 60; j++) {
            minutes.getItems().add(j);
        }
        minutes.setStyle(UI.Components.Styles.UIStyles.HOURMINUTESTYLE);
        minutes.setPrefWidth(25);

        HBox hourMinBox = new HBox(3, timeOptions, hours, colon, minutes);
        hourMinBox.setAlignment(Pos.CENTER_LEFT);
        hours.setVisible(false);
        minutes.setVisible(false);
        colon.setVisible(false);

        timeOptions.setOnAction(e -> {
            String selection = timeOptions.getValue();
            boolean show = selection.equals("Leave at ...");
            hours.setVisible(show);
            minutes.setVisible(show);
            colon.setVisible(show);
        });


        goButton = new Button("Go");
        resetButton = new Button("Reset");
        displayStopsButton = new Button("Display all Stops");

        goButton.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        String buttonStyle = "-fx-background-color: rgb(173, 173, 173);" +
                "-fx-text-fill: rgb(9, 9, 9);";
        goButton.setStyle(buttonStyle);
        goButton.setPrefWidth(80);
        resetButton.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        resetButton.setPrefWidth(80);

        displayStopsButton.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        displayStopsButton.setPrefWidth(130);
        VBox.setMargin(displayStopsButton, new Insets(20, 0, 0, 0));


        directionsBox = new VBox(10, startField, endField, hourMinBox, goButton, resetButton, displayStopsButton);
        directionsBox.setStyle("-fx-background-color: white;");
        directionsBox.setPadding(new Insets(20, 20, 20, 20));
        directionsBox.setPrefWidth(250);


        resetButton.setOnAction(e -> {
            resetMap();
        });

        displayStopsButton.setOnAction(e -> {
            try {
                drawAllStopsToTheMap();
            } catch (Exception ex) {
                showError("Error with loading all Stops" + ex.getMessage(), "Database loading error");
            }
        });


        routeInformation = new TextArea();
        routeInformation.setEditable(false);
        routeInformation.setWrapText(true);
        routeInformation.setPrefRowCount(20);
        routeInformation.setPrefWidth(250);
        routeInformation.setStyle(UI.Components.Styles.UIStyles.ROUTEINFORMATIONSTYLE);
        routeInformation.setVisible(false);

        goButton.setOnAction(event -> {

            if (startField.getText().isEmpty() || endField.getText().isEmpty()) {
                showError("Please fill in both start and end coordinates.", "Input Error");
                return;
            }

            removeComponents();

            Coordinate startCoordinate = stringToCoordinate(startField.getText());
            Coordinate endCoordinate = stringToCoordinate(endField.getText());

            LocalTime selectedTime = LocalTime.now();

            String selectedOption = timeOptions.getValue();
            if (selectedOption.equals("Leave at ...")) {
                int selectedHour = hours.getValue();
                int selectedMinute = minutes.getValue();
                selectedTime = LocalTime.of(selectedHour, selectedMinute);
            }

            Route route;
            RoutingEngine engine = new RoutingEngine();
            try {
                engine.loadDatabase("src/main/resources/hsl.zip");
            } catch (CsvValidationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            if (selectedAlgorithm.equals("AStar")) {
                route = engine.calculateTrip(startCoordinate, endCoordinate, selectedTime.toString());
            } else {
                route = engine.calculateTripDijkstra(startCoordinate, endCoordinate, selectedTime.toString());
            }

            StringBuilder routeText = new StringBuilder();
            routeText.append("🕒 Total journey time: ").append(route.getTripTime()).append(" minutes\n\n");

            List<Node> path = route.getPath();
            Node firstNode = path.get(0);
            routeText.append("🚩 Start at ").append(firstNode.getDateTime()).append("\n");
            routeText.append("   ").append(formatCoordinate(firstNode.getCoordinate())).append("\n\n");

            for (int i = 0; i < path.size(); i++) {
                Node node = path.get(i);
                String transportType = node.getTransportType();

                String icon;
                String displayType;

                if (transportType == null || transportType.isEmpty() || transportType.equals("walk")) {
                    icon = "🚶";
                    displayType = "Walk";
                } else {
                    switch (transportType.toLowerCase()) {
                        case "tram":
                            icon = "🚋";
                            break;
                        case "metro":
                            icon = "🚇";
                            break;
                        case "rail":
                            icon = "🚆";
                            break;
                        case "train":
                            icon = "🚂";
                            break;
                        case "bus":
                            icon = "🚌";
                            break;
                        case "ferry":
                            icon = "⛴️";
                            break;
                        case "cable tram":
                            icon = "🚟";
                            break;
                        case "aerial lift":
                            icon = "🚡";
                            break;
                        case "funicular":
                            icon = "🚞";
                            break;
                        case "trolleybus":
                            icon = "🚎";
                            break;
                        case "monorail":
                            icon = "🚝";
                            break;
                        case "on-demand bus":
                            icon = "🚐";
                            break;
                        case "city bike":
                            icon = "🚲";
                            break;
                        case "scooter":
                            icon = "🛴";
                            break;
                        case "taxi":
                            icon = "🚖";
                            break;
                        default:
                            icon = "🚌";
                            break;
                    }

                    String routeNumber = node.getRouteNumber();
                    displayType = "Take " + transportType + " " + (routeNumber != null ? routeNumber : "");
                }

                routeText.append(icon).append(" ").append(displayType)
                        .append(" to next stop\n");
                routeText.append("   Arrive at: ").append(node.getDateTime()).append("\n");
                routeText.append("   ").append(formatCoordinate(node.getCoordinate())).append("\n\n");
            }

            Node lastNode = path.get(path.size() - 1);
            routeText.append("🏁 Arrive at ").append(lastNode.getDateTime()).append("\n");
            routeText.append("   ").append(formatCoordinate(lastNode.getCoordinate())).append("\n");
            routeInformation.setText(routeText.toString());

            System.err.println(routeText.toString());
            routeInformation.setText(routeText.toString());
            routeInformation.setVisible(true);

            if (waypointManager != null) {
                List<GeoPosition> routePoints = new ArrayList<>();
                List<String> transportTypes = new ArrayList<>();

                GeoPosition startGeo = new GeoPosition(startCoordinate.getLatitude(), startCoordinate.getLongitude());
                routePoints.add(startGeo);
                waypointManager.setStartWaypoint(startGeo);

                waypointManager.clearWaypoints();

                for (Node node : route.getPath()) {
                    Coordinate coord = node.getCoordinate();
                    if (coord != null) {
                        GeoPosition geo = new GeoPosition(coord.getLatitude(), coord.getLongitude());
                        routePoints.add(geo);
                        transportTypes.add(node.getTransportType());
                        if (node != route.getPath().get(0) && node != route.getPath().get(route.getPath().size() - 1)) {
                            waypointManager.addWaypoint(geo);
                        }
                    }
                }

                GeoPosition endGeo = new GeoPosition(endCoordinate.getLatitude(), endCoordinate.getLongitude());
                routePoints.add(endGeo);
                waypointManager.setEndWaypoint(endGeo);

                if (!routePoints.isEmpty()) {
                    routePainter.setTrack(routePoints);
                    routePainter.setTransportTypes(transportTypes);

                    CompoundPainter<JXMapViewer> painter = new CompoundPainter<>();
                    List<Painter<JXMapViewer>> painters = new ArrayList<>();
                    painters.add(routePainter);
                    painters.add(waypointManager.getPainter());
                    painter.setPainters(painters);

                    waypointManager.getMapViewer().setOverlayPainter(painter);
                    waypointManager.updateMap();
                }
            }
        });

        Button returnButton = new Button("Return to Main Menu");
        returnButton.setStyle(UI.Components.Styles.UIStyles.BUTTONSTYLE);
        returnButton.setStyle("-fx-background-color: white;");
        returnButton.setPrefWidth(200);
        returnButton.setOnAction(e -> {
            resetMap();
            windowManager.returnToMainMenu();
        });


        VBox container = new VBox(0);
        container.getChildren().addAll(directionsHeader, directionsBox, routeInformation, new VBox(20, returnButton));

        return container;

    }

    private TextField createTextField(String title) {
        TextField textField = new TextField();
        textField.setPromptText(title);
        textField.setFont(Font.font("Raleway", 10));
        textField.setStyle("-fx-control-inner-background: rgb(255, 255, 255);" +
                "-fx-text-fill: rgb(132, 132, 132);" +
                "-fx-border-color: rgb(8, 185, 255);"
        );
        textField.setOnMouseClicked(e -> {
            activeField = textField;
            e.consume();
        });

        return textField;
    }

    public HBox createHeader(String header) {
        HBox headerBox = new HBox();

        headerBox.setPadding(new Insets(10));
        headerBox.setStyle("-fx-background-color:rgb(126, 137, 218);");
        Label getDirectionsTitle = new Label(directionsTitle);
        getDirectionsTitle.setFont(new Font(FONT_FAMILY, FONT_SIZE));
        getDirectionsTitle.setTextFill(Color.WHITE);
        headerBox.getChildren().add(getDirectionsTitle);

        return headerBox;
    }

    private void removeComponents() {
        timeOptions.setVisible(false);
        colon.setVisible(false);
        goButton.setVisible(false);
    }

    private String formatCoordinate(Coordinate coord) {
        return String.format("%.6f, %.6f", coord.getLatitude(), coord.getLongitude());
    }

    public void updateWaypoints(GeoPosition start, GeoPosition end) {
        VBox content = (VBox) directionsBox.getChildren().get(0);
        content.getChildren().clear();

        if (start != null) {
            Label startLabel = new Label(String.format("Start: %.6f, %.6f",
                    start.getLatitude(), start.getLongitude()));
            content.getChildren().add(startLabel);
        }

        if (end != null) {
            Label endLabel = new Label(String.format("End: %.6f, %.6f",
                    end.getLatitude(), end.getLongitude()));
            content.getChildren().add(endLabel);
        }
    }

    public TextField getActiveField() {
        return activeField;
    }

    public void setCoordinates(GeoPosition position) {
        if (activeField == null) return;

        String coords = String.format("%.6f, %.6f", position.getLatitude(), position.getLongitude());

        if (activeField == startField) {
            startField.setText(coords);
            if (waypointManager != null) {
                waypointManager.clearWaypoints();
                waypointManager.setStartWaypoint(position);
                updateMapOverlay();
            }
        } else if (activeField == endField) {
            endField.setText(coords);
            if (waypointManager != null) {
                waypointManager.setEndWaypoint(position);
                updateMapOverlay();
            }
        }

        activeField = null;
    }

    private void updateMapOverlay() {
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>();
        List<Painter<JXMapViewer>> painters = new ArrayList<>();

        if (routePainter != null) {
            painters.add(routePainter);
        }

        painters.add(waypointManager.getPainter());
        painter.setPainters(painters);

        waypointManager.getMapViewer().setOverlayPainter(painter);
        waypointManager.updateMap();
    }

    private void resetMap() {
        if (waypointManager != null) {
            waypointManager.clearAllWaypoints();
            waypointManager.clearRoute();
        }

        startField.clear();
        endField.clear();

        activeField = null;

        startField.setVisible(true);
        endField.setVisible(true);
        timeOptions.setVisible(true);
        goButton.setVisible(true);
        directionsBox.setPadding(new Insets(40, 20, 20, 20));
        routeInformation.clear();
        routeInformation.setVisible(false);
    }

    private void drawAllStopsToTheMap() throws SQLException {
        if (waypointManager == null) {
            showError("WaypointManager not working", "Intern Error");
            return;
        }
        waypointManager.clearAllWaypoints();
        waypointManager.clearRoute();
        String sql = "SELECT stop_lat, stop_lon FROM stops";
        try (Connection conn = GtfsLoader.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                GeoPosition gp = new GeoPosition(lat, lon);
                waypointManager.addWaypoint(gp);
            }
        }
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>();
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(waypointManager.getPainter());
        painter.setPainters(painters);

        waypointManager.getMapViewer().setOverlayPainter(painter);
        waypointManager.updateMap();

    }

    public void setSelectedAlgorithm(String algorithm) {
        this.selectedAlgorithm = algorithm;
    }
}   
