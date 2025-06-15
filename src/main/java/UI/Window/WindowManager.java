package UI.Window;

import UI.Components.*;
import UI.Config.UIConfig;
import UI.Style.BackgroundFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jxmapviewer.viewer.GeoPosition;

public class WindowManager {
    private final Stage stage;
    private final BorderPane root;
    private final CityDisplay cityDisplay;
    private final TemperatureDisplay temperatureDisplay;
    private final DirectionsWindow directionsWindow;
    private final MapComponent mapComponent;
    private final HeatMapWindow heatMapWindow;
    private Scene scene;
    private AlgorithmSelectionWindow algorithmSelectionWindow;

    private Button directionsButton;
    private Button heatmap;
    private VBox leftSide;
    private VBox heatmapSide;

    public WindowManager(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.cityDisplay = new CityDisplay("Helsinki");
        this.temperatureDisplay = new TemperatureDisplay(9.0);
        this.directionsWindow = new DirectionsWindow();
        directionsWindow.setWindowManager(this);
        this.mapComponent = new MapComponent();
        this.heatMapWindow = new HeatMapWindow();
        initializeWindow();
    }

    private void initializeWindow() {
        configureRoot();
        createScene();
        configureStage();
        algorithmSelectionWindow = new AlgorithmSelectionWindow(this, directionsWindow, heatMapWindow);
    }


    private void configureRoot() {
        root.setBackground(BackgroundFactory.createGradientBackground());
        root.setPadding(new Insets(13));
        root.setPrefSize(UIConfig.WINDOW_WIDTH, UIConfig.WINDOW_HEIGHT);

        HBox topBar = createTopBar();
        root.setTop(topBar);

        VBox mainMenu = createMainMenu();
        root.setLeft(mainMenu);

        setupMap();

        leftSide = directionsWindow.createLeftDirectionsWindow();
        heatmapSide = heatMapWindow.createLeftHeatmapWindow();
        BorderPane.setMargin(leftSide, new Insets(0, 20, 0, 0));
        leftSide.setVisible(false);
        heatmapSide.setVisible(false);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.setPadding(new Insets(30, 40, 0, 40));
        topBar.getChildren().addAll(cityDisplay.getDisplay(), spacer, temperatureDisplay.getDisplay());
        return topBar;
    }

    private VBox createMainMenu() {
        VBox mainMenu = new VBox(20);
        mainMenu.setPadding(new Insets(20));

        directionsButton = new Button("Plan a journey");
        heatmap = new Button("Heatmap");

        String buttonStyle = UI.Components.Styles.UIStyles.BUTTONSTYLE;
        directionsButton.setStyle(buttonStyle);
        heatmap.setStyle(buttonStyle);
        heatmap.setStyle("-fx-color: white;");

        directionsButton.setPrefWidth(200);
        heatmap.setPrefWidth(200);

        directionsButton.setOnAction(event -> showDirectionAlgorithmSelection());
        heatmap.setOnAction(event -> showHeatmapAlgorithmSelection());
        directionsButton.setStyle("-fx-color: white;");

        mainMenu.getChildren().addAll(directionsButton, heatmap);
        mainMenu.setAlignment(Pos.CENTER);
        return mainMenu;
    }

    public void showDirectionsWindow(DirectionsWindow directionsWindow) {
        if (algorithmSelectionWindow == null) {
            algorithmSelectionWindow = new AlgorithmSelectionWindow(this, directionsWindow, heatMapWindow);
        }
        algorithmSelectionWindow.setIsHeatMap(false);
        clearRoot();
        root.setLeft(algorithmSelectionWindow.createAlgorithmSelectionWindow());
    }

    private void clearRoot() {
        root.setLeft(null);
        if (leftSide != null) {
            leftSide.setVisible(false);
        }
        if (heatmapSide != null) {
            heatmapSide.setVisible(false);
        }
    }

    public void showDirectionsWindow() {
        VBox mainMenu = (VBox) root.getLeft();
        mainMenu.setVisible(false);
        mapComponent.setDirectionsWindow(directionsWindow);
        directionsWindow.setWaypointManager(mapComponent.getWaypointManager());
        directionsWindow.setWindowManager(this);
        root.setLeft(leftSide);
        leftSide.setVisible(true);
    }

    public void showHeatmapAlgorithmSelection() {
        if (algorithmSelectionWindow == null) {
            algorithmSelectionWindow = new AlgorithmSelectionWindow(this, directionsWindow, heatMapWindow);
        }
        algorithmSelectionWindow.setIsHeatMap(true);
        clearRoot();
        root.setLeft(algorithmSelectionWindow.createAlgorithmSelectionWindow());
    }

    public void showDirectionAlgorithmSelection() {
        if (algorithmSelectionWindow == null) {
            algorithmSelectionWindow = new AlgorithmSelectionWindow(this, directionsWindow, heatMapWindow);
        }
        algorithmSelectionWindow.setIsHeatMap(false);
        clearRoot();
        root.setLeft(algorithmSelectionWindow.createAlgorithmSelectionWindow());
    }

    public void showHeatmapWindow() {
        VBox mainMenu = (VBox) root.getLeft();
        mainMenu.setVisible(false);
        mapComponent.setHeatMapWindow(heatMapWindow);
        heatMapWindow.setWaypointManager(mapComponent.getWaypointManager());
        heatMapWindow.setWindowManager(this);
        root.setLeft(heatmapSide);
        heatmapSide.setVisible(true);
    }

    private void setupMap() {
        directionsWindow.setWaypointManager(mapComponent.getWaypointManager());
        mapComponent.setDirectionsWindow(directionsWindow);

        Pane mapContainer = mapComponent.getMapPane();
        mapContainer.setPadding(new Insets(25, 25, 25, 25));
        root.setCenter(mapContainer);

        mapComponent.getMapPane().setOnMouseClicked(event -> {
            if (leftSide.isVisible()) {
                directionsWindow.updateWaypoints(mapComponent.getStartWaypoint(), mapComponent.getEndWaypoint());
            } else if (heatmapSide.isVisible() && heatMapWindow.getActiveField() != null) {
                GeoPosition position = mapComponent.getStartWaypoint();
                if (position != null) {
                    heatMapWindow.updateWaypoint(position);
                }
            }
        });
    }

    public void showAlgorithmSelectionWindow() {
        if (algorithmSelectionWindow == null) {
            algorithmSelectionWindow = new AlgorithmSelectionWindow(this, directionsWindow, heatMapWindow);
        }
        clearRoot();
        root.setLeft(algorithmSelectionWindow.createAlgorithmSelectionWindow());
    }

    private void createScene() {
        scene = new Scene(root);
    }


    private void showMainMenu() {
        leftSide.setVisible(false);
        VBox mainMenu = createMainMenu();
        root.setLeft(mainMenu);
        mainMenu.setVisible(true);
    }

    public void returnToMainMenu() {
        showMainMenu();
    }

    private void configureStage() {
        stage.setTitle(UIConfig.WINDOW_TITLE);
        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }
}