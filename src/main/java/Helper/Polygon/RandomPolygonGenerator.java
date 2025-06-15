package Helper.Polygon;

import DataStructures.Coordinate;

import java.util.List;
import java.util.Random;


public class RandomPolygonGenerator {

    public Coordinate generateRandomPointInPolygon(List<Coordinate> polygon, Random r) {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (Coordinate coord : polygon) {
            if (coord.getLatitude() < minLat) minLat = coord.getLatitude();
            if (coord.getLatitude() > maxLat) maxLat = coord.getLatitude();
            if (coord.getLongitude() < minLon) minLon = coord.getLongitude();
            if (coord.getLongitude() > maxLon) maxLon = coord.getLongitude();
        }

        while (true) {
            double randomLat = minLat + (maxLat - minLat) * r.nextDouble();
            double randomLon = minLon + (maxLon - minLon) * r.nextDouble();
            Coordinate candidate = new Coordinate(randomLat, randomLon);
            if (isPointInPolygon(candidate, polygon)) {
                return candidate;
            }
        }
    }

    private boolean isPointInPolygon(Coordinate point, List<Coordinate> polygon) {
        int i, j;
        boolean result = false;
        for (i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if ((polygon.get(i).getLongitude() > point.getLongitude()) != (polygon.get(j).getLongitude() > point.getLongitude()) &&
                    (point.getLatitude() < (polygon.get(j).getLatitude() - polygon.get(i).getLatitude()) *
                            (point.getLongitude() - polygon.get(i).getLongitude()) /
                            (polygon.get(j).getLongitude() - polygon.get(i).getLongitude()) + polygon.get(i).getLatitude())) {
                result = !result;
            }
        }
        return result;
    }
}