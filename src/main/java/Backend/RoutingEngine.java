package Backend;

import Backend.Algorithms.AStar;
import Backend.Algorithms.Dijkstra;
import DataStructures.Coordinate;
import DataStructures.DijkstraEdge;
import DataStructures.Node;
import DataStructures.Route;
import Database.GtfsLoader;
import Database.Queries.AgencyQuery;
import Database.Queries.BusDetailsQuery;
import Database.Queries.StopNameQuery;
import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONSyntaxError;
import com.leastfixedpoint.json.JSONWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.sql.*;
import java.util.*;

public class RoutingEngine {
    private JSONReader requestReader = new JSONReader(new InputStreamReader(System.in));
    private JSONWriter<OutputStreamWriter> responseWriter = new JSONWriter<>(new OutputStreamWriter(System.out));
    private GtfsLoader loader;
    private AgencyQuery agencyQuery = new AgencyQuery();
    private BusDetailsQuery busDetailsQuery = new BusDetailsQuery();
    private StopNameQuery stopNameQuery = new StopNameQuery();

    public static void main(String[] args) throws IOException, CsvValidationException {
        new RoutingEngine().run();
    }

    public void run() throws IOException, CsvValidationException {
        System.err.println("Starting");
        this.responseWriter.setSortKeys(false);
        while (true) {
            Object json;
            try {
                json = requestReader.read();
            } catch (JSONSyntaxError e) {
                sendError("Bad JSON input");
                break;
            } catch (EOFException e) {
                System.err.println("End of input detected");
                break;
            }

            if (json instanceof Map<?, ?>) {
                Map<?, ?> request = (Map<?, ?>) json;
                if (request.containsKey("ping")) {
                    sendOk(Map.of("pong", request.get("ping")));
                    continue;
                } else if (request.containsKey("load")) {
                    String loadPath = request.get("load").toString();
                    loadDatabase(loadPath);
                } else if (request.containsKey("routeFrom")) {
                    try {
                        Coordinate startingCoordinate = loadCoordinate(request.get("routeFrom").toString());
                        Coordinate destinationCoordinate = loadCoordinate(request.get("to").toString());

                        String startingTime = (request.get("startingAt").toString());

                        Route route = calculateTripDijkstra(startingCoordinate, destinationCoordinate, startingTime);
                        List<Node> path = route.getPath();

                        List<Map<String, Object>> steps = new ArrayList<>();

                        for (int i = 1; i < path.size(); i++) {
                            Map<String, Object> step = new LinkedHashMap<>();
                            Node currentNode = path.get(i);
                            Node previousNode = path.get(i - 1);

                            String mode = currentNode.getTransportType();
                            if (!mode.equals("walk")) mode = "ride";
                            step.put("mode", mode);

                            Coordinate coordinate = currentNode.getCoordinate();
                            double lat = coordinate.getLatitude();
                            double lon = coordinate.getLongitude();
                            Map<String, Object> toMap = Map.of("lat", lat, "lon", lon);
                            step.put("to", toMap);

                            int duration = currentNode.getTime() - previousNode.getTime();
                            step.put("duration", duration);
                            step.put("startTime", previousNode.getDateTime());

                            if (!mode.equals("walk")) {
                                String stopName = stopNameQuery.getStopName(currentNode.getStopId());
                                if (stopName != null) {
                                    step.put("stop", stopName);
                                }
                                String tripId = currentNode.getTripId();
                                if (tripId != null) {
                                    Map<String, String> routeInformation = getRouteInfo(tripId);
                                    step.put("route", routeInformation);
                                }
                            }
                            steps.add(step);
                        }
                        sendOk(steps);
                    } catch (Exception e) {
                        sendError("Invalid routeFrom request: " + e.getMessage());
                    }
                }
            } else sendError("Bad request");
        }
    }

    private Map<String, String> getRouteInfo(String tripId) {
        Map<String, String> routeInfo = new LinkedHashMap<>();
        routeInfo.put("operator", agencyQuery.getAgency(tripId));
        routeInfo.put("shortName", busDetailsQuery.getShortName(tripId));
        routeInfo.put("longName", busDetailsQuery.getLongName(tripId));
        routeInfo.put("headSign", busDetailsQuery.getHeadsign(tripId));
        return routeInfo;
    }

    private Coordinate loadCoordinate(String stringCoordinate) throws Exception {
        String[] split = stringCoordinate.split("=");
        String lon = split[1].split(",")[0];
        String lat = split[2].split("}")[0];
        double latitude = Double.parseDouble(lat);
        double longitude = Double.parseDouble(lon);
        return new Coordinate(latitude, longitude);
    }


    public void loadDatabase(String dbPath) throws CsvValidationException {
        loader = new GtfsLoader(dbPath);
        try {
            loader.loadGtfsFolder();
            try (Connection conn = GtfsLoader.getConnection()) {
                sendOk(("loaded"));
            } catch (SQLException ex) {
                sendError("File not found");
            }
        } catch (FileNotFoundException x) {
            sendError("File not found");
        }
    }

    public Route calculateTrip(Coordinate startingCoordinate, Coordinate destinationCoordinate, String startTime) {
        int startTimeMinutes = timeToMinutes(startTime);
        Node startingNode = new Node(startingCoordinate, startTimeMinutes);
        Node finalDestinationNode = new Node(destinationCoordinate, 0);
        AStar aStar = new AStar(startingNode, finalDestinationNode, startTimeMinutes);
        return aStar.getRoute();
    }

    public Route calculateTripDijkstra(Coordinate startingCoordinate, Coordinate destinationCoordinate, String startTime) {
        int startTimeMinutes = timeToMinutes(startTime);
        Dijkstra hm = new Dijkstra(startingCoordinate, destinationCoordinate, startTimeMinutes);
        int startT = timeToMinutes(startTime);
        String StopID = "Start";
        Map<String, Integer> tempMap = hm.computeEarliestArrivalTimes(StopID, startT);
        List<DijkstraEdge> dijkstraRoute = hm.retracePathDijkstra();
        int arrival_time = tempMap.get("Destination") - startT;
        return hm.getRoute(dijkstraRoute, arrival_time, startingCoordinate, destinationCoordinate, startTimeMinutes);
    }


    private void sendOk(Object value) {
        try {
            responseWriter.write(Map.of("ok", value));
            responseWriter.getWriter().write('\n');
            responseWriter.getWriter().flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to send Ok message", ex);
        }
    }

    private void sendError(String message) {
        try {
            responseWriter.write(Map.of("error", message));
            responseWriter.getWriter().write('\n');
            responseWriter.getWriter().flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to send error message", ex);
        }
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}

