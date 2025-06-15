package UI.Components.Helper;

import lombok.Getter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

@Getter
public class CustomWaypoint extends DefaultWaypoint {
    private final WaypointType type;

    public CustomWaypoint(GeoPosition coord, WaypointType type) {
        super(coord);
        this.type = type;
    }

    public WaypointType getType() {
        return type;
    }

    public enum WaypointType {
        ROUTE,
        START,
        END
    }
}
