package Helper.Polygon;

import DataStructures.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a grid of coordinates within a given square area. This class will be used to help create the heatmap
 */
public class GridPolygonGenerator {

    public List<Coordinate> generateGridPoints(List<Coordinate> square, int gridSize) {
        Coordinate bottomLeft = square.get(0);
        Coordinate topRight = square.get(1);

        double minLat = bottomLeft.getLatitude();
        double maxLat = topRight.getLatitude();
        double minLon = bottomLeft.getLongitude();
        double maxLon = topRight.getLongitude();

        List<Coordinate> gridPoints = new ArrayList<>();

        double latStep = (maxLat - minLat) / gridSize;
        double lonStep = (maxLon - minLon) / gridSize;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double lat = minLat + (i * latStep) + (latStep / 2);
                double lon = minLon + (j * lonStep) + (lonStep / 2);
                gridPoints.add(new Coordinate(lat, lon));
            }
        }

        return gridPoints;
    }
}