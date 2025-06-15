package Benchmarks.Query;

import Backend.Algorithms.AStar;
import DataStructures.Coordinate;
import DataStructures.Node;
import DataStructures.Stop;
import Database.Queries.CoordinateQuery;
import Database.Queries.SequenceQuery;
import Database.Queries.TripIdQuery;
import Database.Queries.WalkToStopQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class QueryBenchmarking {
    public static void main(String[] args) {
        List<QueryTest> tests = new ArrayList<>();
        List<Node[]> testCases = generatePathBasedTestCases();
        benchmarkAllQueries(tests, testCases);
        exportResultsToCSV(tests);
    }

    private static List<Node[]> generatePathBasedTestCases() {
        List<Node[]> testCases = new ArrayList<>();

        Coordinate[] origins = {
                new Coordinate(60.194560, 25.030990),
                new Coordinate(60.221420, 24.805880),
                new Coordinate(60.184810, 24.952340),
                new Coordinate(60.159280, 24.940994),
                new Coordinate(60.759280, 24.14994)
        };

        Coordinate[] destinations = {
                new Coordinate(60.156807, 24.911678),
                new Coordinate(60.178850, 24.828740),
                new Coordinate(60.471160, 24.541580),
                new Coordinate(60.071160, 25.141580),
                new Coordinate(60.181160, 24.441580)
        };

        int[] startTimes = {660, 718, 1025, 400, 540};

        for (int i = 0; i < origins.length; i++) {
            Node startNode = new Node(origins[i], startTimes[i]);
            Node destNode = new Node(destinations[i], 0);

            AStar astar = new AStar(startNode, destNode, startTimes[i]);
            List<Node> path = astar.getPath();

            if (path != null && path.size() > 1) {
                for (int j = 0; j < path.size() - 1; j++) {
                    testCases.add(new Node[]{path.get(j), path.get(j + 1)});
                }

                for (Node node : path) {
                    if (!node.getStopId().equals(Stop.VIRTUAL_STOP)) {
                        testCases.add(new Node[]{node, null});
                    }
                }
            }
        }

        return testCases;
    }

    private static void benchmarkAllQueries(List<QueryTest> tests, List<Node[]> testCases) {
        benchmarkCoordinateQuery(tests, testCases);
        benchmarkSequenceQuery(tests, testCases);
        benchmarkTripIdQuery(tests, testCases);
        benchmarkWalkToStopQuery(tests, testCases);
    }

    private static void benchmarkCoordinateQuery(List<QueryTest> tests, List<Node[]> testCases) {
        CoordinateQuery query = new CoordinateQuery();

        for (Node[] pair : testCases) {
            Node node = pair[0];
            if (node.getStopId().equals(Stop.VIRTUAL_STOP)) continue;

            QueryTest test = new QueryTest("CoordinateQuery.getCoordinateByStopId");

            long start = System.nanoTime();
            try {
                Coordinate coord = query.getCoordinateByStopId(node.getStopId());
            } catch (Exception e) {
                continue;
            }
            test.executionTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            tests.add(test);
        }
    }

    private static void benchmarkSequenceQuery(List<QueryTest> tests, List<Node[]> testCases) {
        SequenceQuery query = new SequenceQuery();

        for (Node[] pair : testCases) {
            if (pair[1] == null || pair[0].getTripId() == null) continue;

            QueryTest test = new QueryTest("SequenceQuery.getNextImmediateNeighbor");

            long start = System.nanoTime();
            try {
                Stop neighbor = query.getNextImmediateNeighbor(pair[0].getStopId(), pair[0].getTripId());
            } catch (Exception e) {
                continue;
            }
            test.executionTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            tests.add(test);
        }
    }

    private static void benchmarkTripIdQuery(List<QueryTest> tests, List<Node[]> testCases) {
        TripIdQuery query = new TripIdQuery();

        for (Node[] pair : testCases) {
            Node node = pair[0];
            if (node.getStopId().equals(Stop.VIRTUAL_STOP)) continue;

            QueryTest test = new QueryTest("TripIdQuery.getTripId");

            long start = System.nanoTime();
            try {
                List<String> trips = query.getTripId(node.getStopId(), node.getTime(), node.getTime() + 60);
            } catch (Exception e) {
                continue;
            }
            test.executionTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            tests.add(test);
        }
    }

    private static void benchmarkWalkToStopQuery(List<QueryTest> tests, List<Node[]> testCases) {
        WalkToStopQuery query = new WalkToStopQuery();

        for (Node[] pair : testCases) {
            Node node = pair[0];

            QueryTest test = new QueryTest("WalkToStopQuery.queryWalkStops");

            long start = System.nanoTime();
            try {
                List<Node> walkStops = query.queryWalkStops(node);
            } catch (Exception e) {
                continue;
            }
            test.executionTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            tests.add(test);
        }
    }

    private static void exportResultsToCSV(List<QueryTest> tests) {
        File csvFile = new File("src/test/java/Benchmarks/Query/query_benchmark.csv");
        try (PrintWriter out = new PrintWriter(csvFile)) {
            out.println("QueryName,ExecutionTimeMs");
            for (QueryTest test : tests) {
                out.printf("%s,%d%n", test.queryName, test.executionTimeMs);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error creating CSV: " + e.getMessage());
        }
    }

    private static class QueryTest {
        String queryName;
        long executionTimeMs;

        QueryTest(String queryName) {
            this.queryName = queryName;
        }
    }
}