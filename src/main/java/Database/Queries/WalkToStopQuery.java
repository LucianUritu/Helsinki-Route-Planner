package Database.Queries;

import DataStructures.Coordinate;
import DataStructures.Node;
import Database.GtfsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WalkToStopQuery {
    private int rad = 100;
    private ResultSet resultSet;

    public List<Node> queryWalkStops(Node node) {
        List<Node> nearbyNodes = new ArrayList<>();
        Coordinate coord = node.getCoordinate();
        double lat = coord.getLatitude();
        double lon = coord.getLongitude();

        int departMinutes = node.getTime();
        int timePlus30 = departMinutes + 10;

        LocalTime departTime = LocalTime.of(departMinutes / 60, departMinutes % 60);
        LocalTime endTime = LocalTime.of(timePlus30 / 60 % 24, timePlus30 % 60);

        String sql = """
                SELECT
                    s.stop_id,
                    st.trip_id,
                    st.departure_time
                FROM stops s
                JOIN stop_times st ON s.stop_id = st.stop_id
                WHERE (
                    6371000 * acos(
                        cos(radians(?)) * cos(radians(s.stop_lat)) *
                        cos(radians(s.stop_lon) - radians(?)) +
                        sin(radians(?)) * sin(radians(s.stop_lat))
                    )
                ) <= ?
                AND time(st.departure_time) BETWEEN time(?) AND time(?)
                ORDER BY st.departure_time;
                """;
        while (nearbyNodes.size() < 1) {
            // System.err.println("rad is " + rad);
            try (Connection connection = GtfsLoader.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setDouble(1, lat);
                pstmt.setDouble(2, lon);
                pstmt.setDouble(3, lat);
                pstmt.setInt(4, rad);
                pstmt.setString(5, departTime.toString());
                pstmt.setString(6, endTime.toString());

                resultSet = pstmt.executeQuery();

                while (resultSet.next()) {
                    String stopId = resultSet.getString("stop_id");
                    String tripId = resultSet.getString("trip_id");
                    LocalTime stopTime = LocalTime.parse(resultSet.getString("departure_time"));
                    int stopTimeMinutes = stopTime.getHour() * 60 + stopTime.getMinute();

                    Node nearbyNode = new Node(stopId, tripId, stopTimeMinutes);

                    nearbyNodes.add(nearbyNode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (rad < 0 || rad > 20000) {
                rad = 75;
                break;
            }
            if (rad < 400) rad += 5;
            else rad *= 2;
        }

        return nearbyNodes;
    }
}

