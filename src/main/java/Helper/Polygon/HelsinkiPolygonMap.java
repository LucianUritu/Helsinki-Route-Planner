package Helper.Polygon;

import DataStructures.Coordinate;

import java.util.Arrays;
import java.util.List;

public class HelsinkiPolygonMap {
    private static List<Coordinate> polygon = Arrays.asList( // used google to generate a map and extracted coordinates. Did this to draw a shape that does not include water/sea 
            new Coordinate(60.1535061, 24.9560814),
            new Coordinate(60.1568167, 24.9627293),
            new Coordinate(60.165785, 24.9534596),
            new Coordinate(60.1674075, 24.9567212),
            new Coordinate(60.1638207, 24.967965),
            new Coordinate(60.1669379, 24.9808396),
            new Coordinate(60.1701828, 24.9753464),
            new Coordinate(60.1697558, 24.9611844),
            new Coordinate(60.1757753, 24.9616135),
            new Coordinate(60.1835007, 24.9828995),
            new Coordinate(60.2158152, 24.9810687),
            new Coordinate(60.2633633, 25.0833811),
            new Coordinate(60.2914496, 24.9374689),
            new Coordinate(60.2989352, 24.8460527),
            new Coordinate(60.2565509, 24.7157007),
            new Coordinate(60.2195688, 24.6408563),
            new Coordinate(60.1546547, 24.6473795),
            new Coordinate(60.1471361, 24.750033),
            new Coordinate(60.1640506, 24.8077112),
            new Coordinate(60.1794198, 24.8368936),
            new Coordinate(60.1971709, 24.8169809),
            new Coordinate(60.2022896, 24.8478799),
            new Coordinate(60.1913687, 24.8736291),
            new Coordinate(60.1770295, 24.9113947),
            new Coordinate(60.1582425, 24.9034982),
            new Coordinate(60.1478197, 24.9268442)
    );

    public static List<Coordinate> getPolygon() {
        return polygon;
    }

    public static List<Coordinate> getSquare() {
        return Arrays.asList(
                new Coordinate(60.137974, 24.639004),
                new Coordinate(60.319390, 25.111751)
        );
    }
}
