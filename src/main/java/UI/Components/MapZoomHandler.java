package UI.Components;

import UI.Config.MapConfig;
import javafx.scene.input.ScrollEvent;
import org.jxmapviewer.JXMapViewer;

public class MapZoomHandler {
    private final JXMapViewer mapViewer;
    private final MapConfig config;

    public MapZoomHandler(JXMapViewer mapViewer, MapConfig config) {
        this.mapViewer = mapViewer;
        this.config = config;
    }

    public void handleScroll(ScrollEvent event) {
        int currentZoom = mapViewer.getZoom();
        if (event.getDeltaY() > 0 && currentZoom > config.getMinZoom()) {
            mapViewer.setZoom(currentZoom - 1);
        } else if (event.getDeltaY() < 0 && currentZoom < config.getMaxZoom()) {
            mapViewer.setZoom(currentZoom + 1);
        }
        event.consume();
    }
}