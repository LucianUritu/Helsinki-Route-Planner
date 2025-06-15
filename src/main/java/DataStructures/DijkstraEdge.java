package DataStructures;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DijkstraEdge {
    String sourceId;
    String destId;
    int departureTime;
    int arrivalTime;
    String routeId;
    int stopSequenceSource;
    int stopSequenceTarget;
    int travelTime;
    boolean walk;


    public DijkstraEdge(String sourceId, String destId, int departureTime, int arrivalTime, String routeId, int stopSequenceSource, int stopSequenceTarget) {
        this.sourceId = sourceId;
        this.destId = destId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.routeId = routeId;
        this.stopSequenceSource = stopSequenceSource;
        this.stopSequenceTarget = stopSequenceTarget;
        this.walk = false;
    }

    public DijkstraEdge(String sourceId, String destId, int travelTime, int stopSequenceSource, int stopSequenceTarget) {
        this.sourceId = sourceId;
        this.destId = destId;
        this.travelTime = travelTime;
        this.stopSequenceSource = stopSequenceSource;
        this.stopSequenceTarget = stopSequenceTarget;
        this.walk = true;
    }
}
