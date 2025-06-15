package Database;

import DataStructures.Coordinate;
import DataStructures.DijkstraEdge;
import DataStructures.Stop;
import Helper.Calculator.WalkingCalculator;

import java.sql.*;
import java.util.*;

public class DijkstraLoader {

    public Map<String, List<DijkstraEdge>> DKSloader() {
        // Map to hold the edges. key= source stop id
        //now this method preloads all connections before the user even inserts the data
        Map<String, List<DijkstraEdge>> scheduleMap = new HashMap<>();
        WalkingCalculator calculator = new WalkingCalculator();
        String queryCoordinates = "SELECT stop_id, stop_lat, stop_lon FROM stops";
        List<Stop> stops = new ArrayList<>();

        try (Connection connection = GtfsLoader.getConnection();
             Statement stmt = connection.createStatement();) {
            ResultSet rs = stmt.executeQuery(queryCoordinates);

            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                Coordinate coordinate = new Coordinate(lat, lon);
                stops.add(new Stop(stopId, "", coordinate)); //for pre-Loading
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < stops.size(); i++) {
            Stop stop1 = stops.get(i);
            for (int j = i + 1; j < stops.size(); j++) {
                Stop stop2 = stops.get(j);

                int travelTime = calculator.calculateTime(stop1.getStopCoordinate(), stop2.getStopCoordinate());

                if (travelTime <= 60 && stop1.getStopId() != stop2.getStopId()) {
                    if (travelTime == 0) travelTime = 1;

                    DijkstraEdge edge1 = new DijkstraEdge(stop1.getStopId(), stop2.getStopId(), travelTime, 0, 1);
                    if (!scheduleMap.containsKey(stop1.getStopId())) {
                        scheduleMap.put((stop1.getStopId()), new ArrayList<>());
                    }
                    scheduleMap.get(stop1.getStopId()).add(edge1);
                    DijkstraEdge edge2 = new DijkstraEdge(stop2.getStopId(), stop1.getStopId(), travelTime, 0, 1);
                    if (!scheduleMap.containsKey(stop2.getStopId())) {
                        scheduleMap.put((stop2.getStopId()), new ArrayList<>());
                    }
                    scheduleMap.get(stop2.getStopId()).add(edge2);
                }
            }
        }

        String sql = "SELECT trip_id, arrival_time, departure_time, stop_id, stop_sequence " +
                "FROM stop_times " +
                "ORDER BY trip_id";
        try (Connection connection = GtfsLoader.getConnection();
             Statement stmt = connection.createStatement();) {
            ResultSet rs = stmt.executeQuery(sql);
            String currentTrip = null;
            List<Stop> tripStops = new ArrayList<>();

            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String arrivalTime = rs.getString("arrival_time");
                String departureTime = rs.getString("departure_time");
                String stopId = rs.getString("stop_id");
                int stop_sequence = rs.getInt("stop_sequence");
                // when we finish each trip:
                if (currentTrip != null && !tripId.equals(currentTrip)) {
                    createConnections(scheduleMap, tripStops, currentTrip);
                    tripStops.clear();
                }

                currentTrip = tripId;
                tripStops.add(new Stop(stopId, arrivalTime, departureTime, stop_sequence));
            }

            if (!tripStops.isEmpty() && currentTrip != null) {
                createConnections(scheduleMap, tripStops, currentTrip);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return scheduleMap;
    }

    public Map<String, List<DijkstraEdge>> addStartStop(Map<String, List<DijkstraEdge>> scheduleMap, Coordinate firstCoord) {
        WalkingCalculator calculator = new WalkingCalculator();
        String queryCoordinates = "SELECT stop_id, stop_lat, stop_lon FROM stops";
        String firstStopId = "Start";
        try (Connection connection = GtfsLoader.getConnection();
             Statement stmt = connection.createStatement();) {
            ResultSet rs = stmt.executeQuery(queryCoordinates);

            scheduleMap.put(firstStopId, new ArrayList<>());

            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                Coordinate coordinate = new Coordinate(lat, lon);
                int travelTime = (int) Math.round((calculator.calculateTime(firstCoord, coordinate)));

                DijkstraEdge edge = new DijkstraEdge(firstStopId, stopId, travelTime, 0, 1);
                scheduleMap.get(firstStopId).add(edge);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scheduleMap;
    }

    private void createConnections(Map<String, List<DijkstraEdge>> scheduleMap, List<Stop> tripStops, String tripId) {
        for (int i = 0; i < tripStops.size() - 1; i++) {
            Stop sourceStop = tripStops.get(i);
            Stop targetStop = tripStops.get(i + 1);

            // Only creates a connection if the stop_sequence is consecutive(=> one stop is
            // after another)
            if (targetStop.getStop_sequence() == sourceStop.getStop_sequence() + 1) {
                String temptimeDeparture = sourceStop.getDepartureTime();
                String temptimeArrival = targetStop.getArrivalTime();

                int arrivalTime = timeToMinutes(temptimeArrival);
                int departureTime = timeToMinutes(temptimeDeparture);
                DijkstraEdge edge = new DijkstraEdge(
                        sourceStop.getStopId(),
                        targetStop.getStopId(),
                        departureTime,
                        arrivalTime,
                        tripId,
                        sourceStop.getStop_sequence(),
                        targetStop.getStop_sequence());
                if (!scheduleMap.containsKey(sourceStop.getStopId())) {
                    scheduleMap.put((sourceStop.getStopId()), new ArrayList<>());
                }
                scheduleMap.get(sourceStop.getStopId()).add(edge);

            }
        }
    }

    public Map<String, List<DijkstraEdge>> addDestinationStop(Map<String, List<DijkstraEdge>> scheduleMap, Coordinate destiantionCoord) {
        WalkingCalculator calculator = new WalkingCalculator();
        String queryEndCoordinates = "SELECT stop_id, stop_lat, stop_lon FROM stops";
        String destinationId = "Destination";

        try (Connection connection = GtfsLoader.getConnection();
             Statement stmt = connection.createStatement();) {
            ResultSet rs = stmt.executeQuery(queryEndCoordinates);

            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                Coordinate coordinate = new Coordinate(lat, lon);
                int travelTime = (int) Math.round((calculator.calculateTime(destiantionCoord, coordinate)));
                DijkstraEdge edge = new DijkstraEdge(stopId, destinationId, travelTime, 0, 1);
                if (!scheduleMap.containsKey(stopId)) {
                    scheduleMap.put((stopId), new ArrayList<>());
                }
                scheduleMap.get(stopId).add(edge);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scheduleMap;
    }

    public int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}

