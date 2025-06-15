package UI.Components;

import UI.Components.Helper.OSMTileFactoryInfo;
import UI.Components.Helper.PixelToCoordinate;
import UI.Components.Helper.RoutePainter;
import UI.Components.Helper.WaypointManager;
import UI.Config.MapConfig;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MapComponent {
    private final Pane mapPane;
    private final JXMapViewer mapViewer;
    private final MapConfig config;
    private final MapZoomHandler zoomHandler;
    private final WaypointManager waypointManager;
    private final CompoundPainter<JXMapViewer> painter;
    private final RoutePainter routePainter;
    private DirectionsWindow directionsWindow;
    private HeatMapWindow heatMapWindow;

    public MapComponent() {
        this.config = new MapConfig();
        this.mapPane = new Pane();
        this.mapViewer = createMapViewer();
        this.zoomHandler = new MapZoomHandler(mapViewer, config);
        this.waypointManager = new WaypointManager();
        this.painter = new CompoundPainter<>();
        this.routePainter = new RoutePainter();
        initializeMap();
    }

    public void setHeatMapWindow(HeatMapWindow heatMapWindow) {
        this.heatMapWindow = heatMapWindow;
    }

    public void setDirectionsWindow(DirectionsWindow directionsWindow) {
        this.directionsWindow = directionsWindow;
    }

    public WaypointManager getWaypointManager() {
        return waypointManager;
    }

    private void initializeMap() {
        waypointManager.setMapViewer(mapViewer);
        waypointManager.setRoutePainter(routePainter);
        SwingNode swingNode = new SwingNode();

        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(mapViewer);
            swingNode.setPickOnBounds(true);
        });

        StackPane swingNodeContainer = new StackPane();
        swingNodeContainer.getChildren().add(swingNode);

        // Basic container setup
        swingNodeContainer.setPickOnBounds(true);
        swingNodeContainer.setMouseTransparent(false);

        // Remove all existing event filters and handlers
        bindContainerSize(swingNodeContainer);

        // Clear and set up new event handlers
        setupEventHandlers(swingNodeContainer);

        setupDragFunctionality(mapViewer);

        // Set up the map pane
        mapPane.setPickOnBounds(true);
        mapPane.getChildren().clear();
        mapPane.getChildren().add(swingNodeContainer);
        mapPane.setMinSize(400, 300);
        mapPane.setPrefSize(800, 600);

        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(routePainter);
        painters.add(waypointManager.getPainter());
        painter.setPainters(painters);
        mapViewer.setOverlayPainter(painter);

        PixelToCoordinate converter = new PixelToCoordinate(mapViewer);
        swingNode.setOnMouseClicked(evt -> {
            Point2D fxPt = new Point2D(evt.getX(), evt.getY());
            GeoPosition geo = converter.pixelToGeo(fxPt);
            waypointManager.addWaypoint(geo);

            SwingUtilities.invokeLater(() -> {
                mapViewer.repaint();
            });

            if (waypointManager.hasRoute()) {
                List<GeoPosition> route = new ArrayList<>();
                route.add(waypointManager.getStartWaypoint().getPosition());
                route.add(waypointManager.getEndWaypoint().getPosition());
                routePainter.setTrack(route);
            }

            SwingUtilities.invokeLater(() -> {
                mapViewer.repaint();
            });

            System.out.println("clicked on geo-coord: " + geo.getLatitude() + " / " + geo.getLongitude());
        });

        PixelToCoordinate pixelToCoordinate = new PixelToCoordinate(mapViewer);
        swingNode.setOnMouseClicked(evt -> {
            Point2D fxPt = new Point2D(evt.getX(), evt.getY());
            GeoPosition geo = pixelToCoordinate.pixelToGeo(fxPt);

            if (directionsWindow != null && directionsWindow.getActiveField() != null) {
                directionsWindow.setCoordinates(geo);

                if (waypointManager.hasRoute()) {
                    List<GeoPosition> route = waypointManager.getRoutePositions();
                    routePainter.setTrack(route);
                }

                SwingUtilities.invokeLater(() -> {
                    mapViewer.repaint();
                });
            } else if (heatMapWindow != null && heatMapWindow.getActiveField() != null) {
                heatMapWindow.setCoordinates(geo);
                SwingUtilities.invokeLater(() -> {
                    mapViewer.repaint();
                });
            }
        });
    }

    private JXMapViewer createMapViewer() {
        JXMapViewer viewer = new JXMapViewer();
        configureTileFactory(viewer);
        viewer.setZoom(config.getDefaultZoom());
        viewer.setAddressLocation(config.getDefaultCenter());
        return viewer;
    }

    private void configureTileFactory(JXMapViewer viewer) {
        String tilePath = "src/main/resources/map/helsinki_small";
        File tileDir = new File(tilePath);
        TileFactoryInfo info = new OSMTileFactoryInfo("Helsinki Tiles", tileDir.toURI().toString());
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(config.getThreadPoolSize());
        viewer.setTileFactory(tileFactory);
    }

    private void setupDragFunctionality(JXMapViewer mapViewer) {
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);

    }

    private void setupEventHandlers(StackPane container) {
        // Scroll handler for zooming
        container.setOnScroll(event -> {
            zoomHandler.handleScroll(event);
        });
    }

    private void bindContainerSize(StackPane container) {
        container.prefWidthProperty().bind(mapPane.widthProperty());
        container.prefHeightProperty().bind(mapPane.heightProperty());
    }

    public Pane getMapPane() {
        return mapPane;
    }

    public boolean hasRoute() {
        return waypointManager.hasRoute();
    }

    public GeoPosition getStartWaypoint() {
        return waypointManager.getStartWaypoint().getPosition();
    }

    public GeoPosition getEndWaypoint() {
        return waypointManager.getEndWaypoint().getPosition();
    }
}
