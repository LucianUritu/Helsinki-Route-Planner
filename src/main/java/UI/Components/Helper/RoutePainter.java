package UI.Components.Helper;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class RoutePainter implements Painter<JXMapViewer> {
    private static final float[] DASH_PATTERN = {10.0f, 10.0f};
    private final Color routeColor = Color.RED;
    private final float routeWidth = 3.0f;
    private List<GeoPosition> track;
    private List<String> transportTypes;

    public RoutePainter() {
        this.track = new ArrayList<>();
        this.transportTypes = new ArrayList<>();
    }

    public void setTrack(List<GeoPosition> track) {
        this.track = track;
    }

    public void setTransportTypes(List<String> transportTypes) {
        this.transportTypes = transportTypes;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(routeWidth));
        g.setColor(routeColor);

        // Draw the route
        drawRoute(g, map);

        g.dispose();
    }

    private void drawRoute(Graphics2D g, JXMapViewer map) {
        if (track.size() < 2) return;

        // Handle remaining segments
        for (int i = 0; i < track.size() - 1; i++) {
            Point2D pt1 = map.getTileFactory().geoToPixel(track.get(i), map.getZoom());
            Point2D pt2 = map.getTileFactory().geoToPixel(track.get(i + 1), map.getZoom());


            if (i == 1) {
                if ("walk".equals(transportTypes.get(i))) {
                    g.setStroke(new BasicStroke(routeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, DASH_PATTERN, 0.0f));
                    g.setColor(Color.BLUE);
                }
            } else {
                String currentType = i < transportTypes.size() ? transportTypes.get(i) : "ride";
                g.setStroke(new BasicStroke(routeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, DASH_PATTERN, 0.0f));
                g.setColor(Color.BLUE);

                if ("walk".equals(currentType)) {
                } else {
                    g.setStroke(new BasicStroke(routeWidth));
                    g.setColor(routeColor);
                }
            }

            g.drawLine((int) pt1.getX(), (int) pt1.getY(),
                    (int) pt2.getX(), (int) pt2.getY());
        }
    }
}