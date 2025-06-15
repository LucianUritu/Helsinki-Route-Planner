package UI.Components.Helper;

import UI.Components.Helper.CustomWaypoint.WaypointType;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.*;
import java.awt.geom.Point2D;

public class CustomWaypointRenderer implements WaypointRenderer<Waypoint> {
    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint waypoint) {
        if (!(waypoint instanceof CustomWaypoint)) return;

        CustomWaypoint custom = (CustomWaypoint) waypoint;
        Point2D point = map.getTileFactory().geoToPixel(
                waypoint.getPosition(), map.getZoom());

        int x = (int) point.getX();
        int y = (int) point.getY();

        WaypointType type = custom.getType();
        Color color;

        if (type == WaypointType.START || type == WaypointType.END) {
            color = Color.BLUE;
        } else {
            color = Color.RED;
        }

        g.setColor(color);
        g.fillOval(x - 5, y - 5, 10, 10);

        g.setColor(Color.BLACK);
        g.drawOval(x - 5, y - 5, 10, 10);
    }
}