package Database.Queries;

import DataStructures.Coordinate;
import DataStructures.Stop;
import Database.GtfsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CoordinateQuery {
    public Coordinate getCoordinateByStopId(String stopId) {
        String sql = """
                SELECT stop_lat, stop_lon
                FROM stops
                WHERE stop_id = ?
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, stopId);
            var rs = statement.executeQuery();
            Coordinate coordinate;

            while (rs.next()) {
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                coordinate = new Coordinate(lat, lon);
                return coordinate;
            }


        } catch (SQLException e) {
            System.err.println("Error TripIdQuery: " + e.getMessage());
        }

        return null;
    }

    public String getStopIdByCoordinate(Coordinate coordinate) {
        //TODO:/more a QUESTION do i just need to give the closest back, that we didn't visit yet?
        //if not already in the open/closed list: then

        //source for the select statement: https://alikhallad.com/how-to-find-closest-locations-using-coordinates-with-sql/
        double lat = coordinate.getLatitude();
        double lon = coordinate.getLongitude();

        String sql = """
                SELECT stop_id (
                    6371 *
                    acos(cos(radians(?)) * 
                    cos(radians(stop_lat)) * 
                    cos(radians(stop_lon) - 
                    radians(?)) + 
                    sin(radians(?)) * 
                    sin(radians(stop_lat)))
                    ) AS distance 
                FROM stops
                HAVING distance
                LIMIT 20    
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, lat);
            statement.setDouble(2, lon);
            statement.setDouble(3, lat);
            var rs = statement.executeQuery();

            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                //TODO: add something that only returns a coordinate that's not already been visitedif()
                return stopId;
            }
        } catch (SQLException e) {
            System.err.println("Error getIdByCoordinate: " + e.getMessage());
        }
        return Stop.INVALID_STOP;
    }
}
