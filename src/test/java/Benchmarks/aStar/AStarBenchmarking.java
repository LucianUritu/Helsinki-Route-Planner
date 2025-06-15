package Benchmarks.aStar;

import Backend.Algorithms.AStar;
import DataStructures.Coordinate;
import DataStructures.Node;
import Helper.Calculator.WalkingCalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AStarBenchmarking {
    private static boolean isPointInPolygon(Coordinate point, List<Coordinate> polygon) {
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

    private static Coordinate generateRandomPointInPolygon(List<Coordinate> polygon, Random r) {
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

    public static void main(String[] args) {
        List<Test> tests = new ArrayList<>();

        Random r = new Random();

        List<Coordinate> polygon = Arrays.asList( // used google to generate a map and extracted coordinates. Did this to draw a shape that does not include water/sea
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

        for (int i = 0; i < 250; i++) {
            Coordinate startCoordinate = generateRandomPointInPolygon(polygon, r);
            Coordinate destinationCoordinate = generateRandomPointInPolygon(polygon, r);
            int startTime = r.nextInt(1000);

            tests.add(new Test(String.valueOf(i), startCoordinate, destinationCoordinate, startTime));
        }

        String benchmarkName = "results";
        File csvFile = new File("src/test/java/Benchmarks/aStar/" + benchmarkName + ".csv");

        try (PrintWriter out = new PrintWriter(csvFile)) {
            out.println("Test,Start_Lat,Start_Lon,Dest_Lat,Dest_Lon,Route_Time_minutes,Time_ms,Time_s,Path_Length,Distance_meters");
            for (Test t : tests) {
                Node sourceNode = new Node(t.source, t.startTime);
                Node destNode = new Node(t.dest, 0);

                long t0 = System.nanoTime();
                AStar astar = new AStar(sourceNode, destNode, t.startTime);
                List<Node> path = astar.getPath();
                int routeTimeMinutes = astar.getTripTime();
                long elapsedNs = System.nanoTime() - t0;

                long elapsedMs = TimeUnit.MILLISECONDS.convert(elapsedNs, TimeUnit.NANOSECONDS);
                double elapsedSec = TimeUnit.SECONDS.convert(elapsedNs, TimeUnit.NANOSECONDS);
                if (elapsedSec == 2) continue;

                int NodesNum;
                if (path != null && path.size() > 0) {
                    NodesNum = path.size() - 1;
                } else {
                    NodesNum = 0;
                }

                out.printf("%s,%.7f,%.7f,%.7f,%.7f,%d,%d,%.0f,%d,%d%n",
                        t.name,
                        t.source.getLatitude(),
                        t.source.getLongitude(),
                        t.dest.getLatitude(),
                        t.dest.getLongitude(),
                        routeTimeMinutes,
                        elapsedMs,
                        elapsedSec,
                        NodesNum,
                        t.distanceMeters);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: the CSV file created is not found: " + csvFile.getAbsolutePath());
            return;
        }
    }

    private static class Test {
        String name;
        Coordinate source;
        Coordinate dest;
        int startTime, distanceMeters;
        WalkingCalculator walkingCalculator = new WalkingCalculator();

        Test(String name, Coordinate source, Coordinate dest, int startTime) {
            this.name = name;
            this.source = source;
            this.dest = dest;
            this.startTime = startTime;
            this.distanceMeters = (int) walkingCalculator.calculateDistanceMeters(source, dest);
        }
    }
}