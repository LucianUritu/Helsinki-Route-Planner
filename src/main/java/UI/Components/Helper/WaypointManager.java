package UI.Components.Helper;

import lombok.Getter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class WaypointManager {
    private final List<CustomWaypoint> routeWaypoints;
    private final Set<CustomWaypoint> allWaypoints;
    private final WaypointPainter<Waypoint> painter;
    private CustomWaypoint startWaypoint;
    private CustomWaypoint endWaypoint;
    private JXMapViewer mapViewer;
    private RoutePainter routePainter;


    public WaypointManager() {
        this.routeWaypoints = new ArrayList<>();
        this.allWaypoints = new HashSet<>();
        this.painter = new WaypointPainter<>();
        ((WaypointPainter<Waypoint>) painter).setRenderer(new CustomWaypointRenderer());
    }

    public void setMapViewer(JXMapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

    public void setRoutePainter(RoutePainter routePainter) {
        this.routePainter = routePainter;
    }


    public void addWaypoint(GeoPosition position) {
        CustomWaypoint waypoint = new CustomWaypoint(
                position,
                CustomWaypoint.WaypointType.ROUTE
        );
        routeWaypoints.add(waypoint);
        allWaypoints.add(waypoint);
        updatePainter();
    }

    public void clearWaypoints() {
        routeWaypoints.clear();
        allWaypoints.clear();

        if (startWaypoint != null) {
            allWaypoints.add(startWaypoint);
        }
        if (endWaypoint != null) {
            allWaypoints.add(endWaypoint);
        }
        updatePainter();
    }

    public void clearAllWaypoints() {
        routeWaypoints.clear();
        allWaypoints.clear();
        startWaypoint = null;
        endWaypoint = null;
        updatePainter();
    }

    private void updatePainter() {
        painter.setWaypoints(allWaypoints);

        if (mapViewer != null) {
            SwingUtilities.invokeLater(() -> {
                mapViewer.repaint();
            });
        }
    }

    public WaypointPainter<Waypoint> getPainter() {
        return painter;
    }

    public boolean hasRoute() {
        return routeWaypoints.size() >= 2;
    }

    public List<GeoPosition> getRoutePositions() {
        List<GeoPosition> positions = new ArrayList<>();
        for (CustomWaypoint waypoint : routeWaypoints) {
            positions.add(waypoint.getPosition());
        }
        return positions;
    }

    public List<CustomWaypoint> getWaypoints() {
        return new ArrayList<>(routeWaypoints);
    }

    public void setStartWaypoint(GeoPosition position) {
        if (startWaypoint != null) {
            allWaypoints.remove(startWaypoint);
        }

        startWaypoint = new CustomWaypoint(position, CustomWaypoint.WaypointType.START);
        allWaypoints.add(startWaypoint);
        updatePainter();
    }

    public void setEndWaypoint(GeoPosition position) {
        if (endWaypoint != null) {
            allWaypoints.remove(endWaypoint);
        }

        endWaypoint = new CustomWaypoint(position, CustomWaypoint.WaypointType.END);
        allWaypoints.add(endWaypoint);
        updatePainter();
    }

    public void clearRoute() {
        if (routePainter != null) {
            routePainter.setTrack(new ArrayList<>());
        }
        updatePainter();
    }

    public void addClosedStopWaypoint(CustomWaypoint waypoint) {
        allWaypoints.add(waypoint);
        updatePainter();
    }

    public void updateMap() {
        mapViewer.repaint();
    }
}
