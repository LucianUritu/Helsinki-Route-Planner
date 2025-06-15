package UI.Components.Helper;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

public class PixelToCoordinate {
    private final JXMapViewer mapViewer;

    public PixelToCoordinate(JXMapViewer mapViewer) {
        this.mapViewer = mapViewer;

    }

    public GeoPosition pixelToGeo(javafx.geometry.Point2D pixelCoordinate) {
        int zoom = mapViewer.getZoom();

        java.awt.geom.Point2D centerWorld = mapViewer.getTileFactory().geoToPixel(mapViewer.getCenterPosition(), zoom);

        double dx = pixelCoordinate.getX() - mapViewer.getWidth() / 2.0;
        double dy = pixelCoordinate.getY() - mapViewer.getHeight() / 2.0;

        java.awt.geom.Point2D clickWorld = new java.awt.geom.Point2D.Double(
                centerWorld.getX() + dx,
                centerWorld.getY() + dy
        );

        return mapViewer.getTileFactory().pixelToGeo(clickWorld, zoom);
    }

}
