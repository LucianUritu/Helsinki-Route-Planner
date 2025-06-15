package Helper.Calculator;


import DataStructures.Coordinate;

import static java.lang.Math.toRadians;

/**
 * WalkingCalculator is using the haversine formula -- used for latitude and lonigtude distances.
 * It then takes the predetermined speed of 5 km/h from the project manual to calculate an "as the crow flies" distance
 * This will give you time to get from coord A to coord B in minutes
 */
public class WalkingCalculator {
    private double walkingSpeed = 5; // km/h

    public int calculateTime(Coordinate start, Coordinate end) {
        double distance = calculateDistance(start, end);
        int time = (int) Math.ceil((distance / walkingSpeed) * 60);
        return time;
    }

    public double calculateDistance(Coordinate start, Coordinate end) {
        final double R = 6371; // radius of the earth in KM

        double lat1 = toRadians(start.getLatitude());
        double lat2 = toRadians(end.getLatitude());
        double lon1 = toRadians(start.getLongitude());
        double lon2 = toRadians(end.getLongitude());
        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        /**
         * ~~ HAVERSINE FORMULA ~~
         * a = sin²((lat2 - lat1)/2) + cos(lat1) × cos(lat2) × sin²((lon2 - lon1)/2)
         * c = 2 × atan2(√a, √(1−a))
         * distance = R × c
         */

        double deltaLatSin2 = Math.sin(deltaLat / 2);
        double deltaLonSin2 = Math.sin(deltaLon / 2);
        double a = Math.pow(deltaLatSin2, 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(deltaLonSin2, 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public int calculateDistanceMeters(Coordinate start, Coordinate end) {
        double km = calculateDistance(start, end);
        return (int) (km * 1000);
    }
}
