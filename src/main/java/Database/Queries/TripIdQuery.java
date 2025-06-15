package Database.Queries;

import Database.GtfsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TripIdQuery {
    private List<String> trips;

    public List<String> getTripId(String stopId, int startTime, int maximumTime) {
        //calculate maximumtime: for now, we can pretend = 00:30:00
        //?= prepared statements= objects -> see video form nimute 4 if needed: https://www.youtube.com/watch?v=0beocykXUag&ab_channel=LogicLambda
        //checkout this: https://www.sqlitetutorial.net/sqlite-java/select/ to undretsand terminology used
        String sql = """
                SELECT trip_id,
                       departure_time
                FROM stop_times
                WHERE stop_id         = ?
                  AND departure_time >= ?
                  AND departure_time <= ?
                ORDER BY departure_time;
                """;
        trips = new ArrayList<>();

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String timeUntil = minuteToString(maximumTime + startTime);
            String startSearch = minuteToString(startTime);

            pstmt.setString(1, stopId);
            pstmt.setString(2, startSearch);
            pstmt.setString(3, timeUntil);
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                trips.add(rs.getString("trip_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error TripIdQuery: " + e.getMessage());
        }

        return trips;
    }

    private String minuteToString(int minutes) {
        String s = "";
        String hours = String.valueOf(minutes / 60);
        String newMinutes = String.valueOf(minutes % 60);
        if (Integer.valueOf(hours) < 10) hours = "0" + hours;
        if (Integer.valueOf(newMinutes) < 10) newMinutes = "0" + newMinutes;

        s = hours + ":" + newMinutes + ":" + "00";
        return s;
    }
}
