package DataStructures;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Edge {
    private Node source;
    private Node dest;
    private double weight;
    private String type;

    public Edge(Node source, Node dest, double weight, String type) {
        this.source = source;
        this.dest = dest;
        this.weight = weight;
        this.type = type;
    }

    //        if (!this.transportType.equals("w")) {
    //            Node walkingAlt = this;
    //            walkingAlt.setTransportType("w");
    //            neighbors.add(walkingAlt);
    //        }
    //        if(this.transportType.equals("w")){
    //
    //        }
}
