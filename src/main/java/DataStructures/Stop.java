package DataStructures;

import lombok.Getter;

@Getter
public class Stop implements Comparable<Stop> {
    public static final String VIRTUAL_STOP = "VIRTUAL_STOP";
    public static final String INVALID_STOP = "INVALID_STOP";
    private String stopId;
    private String stopName;
    private Coordinate stopCoordinate;
    private String arrivalTime;
    private String departureTime;
    private int stop_sequence;

    public Stop(String stopId, String stopName, Coordinate stopCoordinate) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopCoordinate = stopCoordinate;
    }

    public Stop(String stopId, String arrivalTime) {
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
    }

    public Stop(String stopId, String departureTime, String arrivalTime, int stop_sequence) {
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stop_sequence = stop_sequence;
    }

    @Override
    public int compareTo(Stop other) {
        int arrivalTimeT = Integer.valueOf(this.arrivalTime);
        int arrivalTimeO = Integer.valueOf(other.arrivalTime);
        return Integer.compare(arrivalTimeT, arrivalTimeO);
    }
}