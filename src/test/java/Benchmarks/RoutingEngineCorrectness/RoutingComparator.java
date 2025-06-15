package Benchmarks.RoutingEngineCorrectness;

import Backend.RoutingEngine;
import DataStructures.Coordinate;
import DataStructures.Route;
import Helper.Calculator.WalkingCalculator;
import Helper.Polygon.HelsinkiPolygonMap;
import Helper.Polygon.RandomPolygonGenerator;
import com.opencsv.exceptions.CsvValidationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * This class compares the performance of different routing engines: Our routing engine (Dijkstra and A*), OpenTripPlanner (OTP), and Google Maps API.
 * It generates random trips within Helsinki's polygon, calculates trip times,
 * and outputs the results to a CSV file which can then be analyzed in the visualization.ipynb notebook.
 * Make sure to have the OTP server running locally according to the readme instructions and create a class that holds the Google Maps API key.
 */
public class RoutingComparator {
    private static final String OTP_API_URL = "http://localhost:8080/otp/routers/default/plan";
    private static final String GMAPS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";
    private static final String API_KEY = ApiKey.GOOGLE_MAPS_API_KEY;
    private final int MAX_TRIPS = 500;
    private RoutingEngine routingEngine = new RoutingEngine();
    private RandomPolygonGenerator randomPolygonGenerator = new RandomPolygonGenerator();
    private WalkingCalculator walkingCalculator = new WalkingCalculator();

    public static void main(String[] args) {
        RoutingComparator comparator = new RoutingComparator();
        comparator.run();
        System.err.println("Routing comparison completed.");
    }

    public void run() {
        List<TripResult> results = new ArrayList<>();
        try {
            routingEngine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
        Random random = new Random();
        int successfulTrips = 0;

        for (int i = 0; i < MAX_TRIPS; i++) {
            try {
                Coordinate startCoord = randomPolygonGenerator.generateRandomPointInPolygon(HelsinkiPolygonMap.getPolygon(), random);
                Coordinate endCoord = randomPolygonGenerator.generateRandomPointInPolygon(HelsinkiPolygonMap.getPolygon(), random);
                double distance = walkingCalculator.calculateDistance(startCoord, endCoord);

                int startTimeMin = random.nextInt(900) + 420;
                String startTime = intTimeToString(startTimeMin);

                int dijkstraDuration = calculateDijkstra(startCoord, endCoord, startTime);
                int aStarDuration = calculateAStar(startCoord, endCoord, startTime);
                int otpDuration = getOTPTripTime(startCoord.getLatitude(), startCoord.getLongitude(), endCoord.getLatitude(), endCoord.getLongitude(), startTimeMin);
                int googleMapsDuration = getGoogleMapsTripTime(startCoord.getLatitude(), startCoord.getLongitude(), endCoord.getLatitude(), endCoord.getLongitude(), startTimeMin);

                if ((otpDuration > 0 && googleMapsDuration > 0) &&
                        dijkstraDuration > 0 && aStarDuration > 0) {

                    results.add(new TripResult(
                            startCoord.getLatitude(), startCoord.getLongitude(),
                            endCoord.getLatitude(), endCoord.getLongitude(),
                            dijkstraDuration, aStarDuration, otpDuration, googleMapsDuration,
                            distance, startTime
                    ));
                    successfulTrips++;
                    System.err.println("Processed trip " + successfulTrips + "/" + MAX_TRIPS);
                }
            } catch (Exception e) {
                System.err.println("Error processing trip: " + e.getMessage());
            }
        }

        if (!results.isEmpty()) {
            writeResultsToCSV(results, "routing_comparison_" + MAX_TRIPS + "_trips.csv");
            System.err.println("Total trips compared: " + results.size());
        } else {
            System.err.println("No successful trips to compare");
        }
    }

    private int calculateDijkstra(Coordinate start, Coordinate end, String time) {
        try {
            Route route = routingEngine.calculateTripDijkstra(start, end, time);
            return route.getTripTime();
        } catch (Exception e) {
            System.err.println("Dijkstra failed: " + e.getMessage());
            return 0;
        }
    }

    private int calculateAStar(Coordinate start, Coordinate end, String time) {
        try {
            Route route = routingEngine.calculateTrip(start, end, time); // A* method
            return route.getTripTime();
        } catch (Exception e) {
            System.err.println("A* failed: " + e.getMessage());
            return 0;
        }
    }

    private int getOTPTripTime(double startLat, double startLon, double endLat, double endLon, int startTimeMin) {
        try {
            String url = String.format(Locale.US,
                    "%s?fromPlace=%.6f,%.6f&toPlace=%.6f,%.6f&time=%s&date=%s" +
                            "&mode=TRANSIT,WALK&maxWalkDistance=5000&arriveBy=false",
                    OTP_API_URL,
                    startLat, startLon,
                    endLat, endLon,
                    intTimeToString(startTimeMin),
                    "2025-06-02"
            );

            String response = sendRequest(url);
            JSONObject json = new JSONObject(response);
            JSONObject plan = json.getJSONObject("plan");
            JSONArray itineraries = plan.getJSONArray("itineraries");

            if (itineraries.length() > 0) {
                JSONObject itinerary = itineraries.getJSONObject(0);
                return itinerary.getInt("duration") / 60;
            }
        } catch (Exception e) {
            System.err.println("OTP error: " + e.getMessage());
        }
        return 0;
    }

    private int getGoogleMapsTripTime(double startLat, double startLon, double endLat, double endLon, int startTimeMin) {
        try {
            ZonedDateTime departureTime = ZonedDateTime.of(
                    LocalDate.of(2025, 6, 2),
                    LocalTime.MIDNIGHT.plusMinutes(startTimeMin),
                    ZoneId.of("Europe/Helsinki")
            );
            long timestamp = departureTime.toEpochSecond();

            String urlString = String.format(Locale.US,
                    "%s?origin=%.5f,%.5f&destination=%.5f,%.5f" +
                            "&mode=transit&departure_time=%d&key=%s",
                    GMAPS_API_URL,
                    startLat, startLon,
                    endLat, endLon,
                    timestamp,
                    API_KEY
            );

            String response = sendRequest(urlString);
            JSONObject json = new JSONObject(response);

            if (!json.getString("status").equals("OK")) {
                System.err.println("Google Maps error: " + json.optString("error_message", "Unknown error"));
                return 0;
            }

            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() == 0) return 0;

            JSONObject firstRoute = routes.getJSONObject(0);
            JSONArray legs = firstRoute.getJSONArray("legs");
            if (legs.length() == 0) return 0;

            JSONObject firstLeg = legs.getJSONObject(0);
            return firstLeg.getJSONObject("duration").getInt("value") / 60;

        } catch (Exception e) {
            System.err.println("Google Maps error: " + e.getMessage());
            return 0;
        }
    }

    private String sendRequest(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Server returned HTTP: " + conn.getResponseCode());
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private void writeResultsToCSV(List<TripResult> results, String filename) {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            writer.println("start_lat,start_lon,end_lat,end_lon,dijkstra_min,a_star_min,otp_min,google_maps_min,distance_km,start_time");
            for (TripResult r : results) {
                writer.println(r.toCSV());
            }
            System.err.println("Results written to " + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String intTimeToString(int totalMinutes) {
        return String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60);
    }

    static class TripResult {
        double startLat, startLon, endLat, endLon;
        int dijkstraDuration;
        int aStarDuration;
        int otpDuration;
        int googleMapsDuration;
        double distance;
        String startTime;

        public TripResult(double startLat, double startLon, double endLat, double endLon, int dijkstraDuration, int aStarDuration, int otpDuration, int googleMapsDuration, double distance, String startTime) {
            this.startLat = startLat;
            this.startLon = startLon;
            this.endLat = endLat;
            this.endLon = endLon;
            this.dijkstraDuration = dijkstraDuration;
            this.aStarDuration = aStarDuration;
            this.otpDuration = otpDuration;
            this.googleMapsDuration = googleMapsDuration;
            this.distance = distance;
            this.startTime = startTime;
        }

        public String toCSV() {
            return String.format(Locale.US, "%.6f,%.6f,%.6f,%.6f,%d,%d,%d,%d,%.1f,%s",
                    startLat, startLon, endLat, endLon,
                    dijkstraDuration, aStarDuration, otpDuration, googleMapsDuration,
                    distance, startTime);
        }
    }
}