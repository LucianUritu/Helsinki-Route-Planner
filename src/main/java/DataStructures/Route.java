package DataStructures;

import lombok.Getter;

import java.util.List;

@Getter
public class Route {
    private List<Node> path;
    private int tripTime;

    public Route(List<Node> path, int tripTime) {
        this.path = path;
        this.tripTime = tripTime;
    }
}
