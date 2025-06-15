package UI.Components.Helper;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Map;

// this is purley for changing the colour of the waypoints, had to do this because waypoint painter didn't
// have colour changing points

public class CustomWaypointPainter extends WaypointPainter<Waypoint> {
    private final Map<Waypoint, Integer> waypoints;

    public CustomWaypointPainter(Map<Waypoint, Integer> waypoints) {
        this.waypoints = waypoints;
    }

    private static Color getColor(int value) {
        int MAX_VALUE = 90;
        int v = Math.max(0, Math.min(value, MAX_VALUE));
        double ratio = v / (double) MAX_VALUE; //will give mixture between 0(green) and 1 (red)

        int red = (int) (ratio * 255);
        int green = (int) ((1.0 - ratio) * 255);
        int blue = 0;

        //opacity
        int alpha = 60;
        return new Color(red, green, blue, alpha);
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
        Color color = Color.blue;

        for (Map.Entry<Waypoint, Integer> elem : waypoints.entrySet()) {
            Point2D wpPoint = map.convertGeoPositionToPoint(elem.getKey().getPosition());

            // Convert Point2D to Point
            Point point = new Point((int) wpPoint.getX(), (int) wpPoint.getY());

            color = getColor(elem.getValue());

            g.setColor(color);
            g.fill(new Ellipse2D.Double(point.x - 5, point.y - 5, 25, 25));
        }
    }

    public void clearHeatmap() {
        waypoints.clear();
    }

}