package UI.Components.Helper;

import DataStructures.Coordinate;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

public class HeatmapPainter implements Painter<JXMapViewer> {
    private Map<Coordinate, Integer> gridData;
    private List<Coordinate> gridCells;
    private Coordinate bottomLeft;
    private Coordinate topRight;
    private int gridSize;
    private int max = Integer.MIN_VALUE;
    private int min = 0;

    public HeatmapPainter(List<Coordinate> gridCells, Map<Coordinate, Integer> tripTimes, Coordinate bottomLeft, Coordinate topRight, int gridSize) {
        this.gridData = tripTimes;
        this.gridCells = gridCells;
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.gridSize = gridSize;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
        g = (Graphics2D) g.create();
        Rectangle viewportBounds = map.getViewportBounds();
        g.translate(-viewportBounds.getX(), -viewportBounds.getY());

        for (Integer value : gridData.values()) {
            max = Math.max(max, value);
            if (max > 120) {
                max = 120;
                break;
            }
        }

        for (Coordinate cell : gridCells) {
            Integer value = gridData.get(cell);

            double latStep = (topRight.getLatitude() - bottomLeft.getLatitude()) / gridSize;
            double lonStep = (topRight.getLongitude() - bottomLeft.getLongitude()) / gridSize;

            double cellLat = cell.getLatitude();
            double cellLon = cell.getLongitude();

            GeoPosition topLeftCorner = new GeoPosition(cellLat + latStep / 2, cellLon - lonStep / 2);
            GeoPosition bottomRightCorner = new GeoPosition(cellLat - latStep / 2, cellLon + lonStep / 2);

            Point2D topLeftPoint = map.getTileFactory().geoToPixel(topLeftCorner, map.getZoom());
            Point2D bottomRightPoint = map.getTileFactory().geoToPixel(bottomRightCorner, map.getZoom());

            int cellWidth = (int) (bottomRightPoint.getX() - topLeftPoint.getX()) + 1;
            int cellHeight = (int) (bottomRightPoint.getY() - topLeftPoint.getY()) + 1;

            g.setColor(getColorForValue(value, min, max));
            g.fillRect((int) topLeftPoint.getX(), (int) topLeftPoint.getY(), Math.abs(cellWidth), Math.abs(cellHeight));
        }
        g.dispose();
    }

    private Color getColorForValue(double value, double min, double max) {
        if (value > max) value = max;

        float normalized = (float) ((value - min) / (max - min));

        float red = normalized;
        float green = 1 - normalized;
        float blue = 0;

        return new Color(red, green, blue, 0.6f);
    }

    public void clearHeatmap() {
        gridData.clear();
        gridCells.clear();
        max = Integer.MIN_VALUE;
        min = 0;
    }
}