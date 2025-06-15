package UI.Config;

import org.jxmapviewer.viewer.GeoPosition;

public class MapConfig {
    private static final double HELSINKI_LAT = 60.1699;
    private static final double HELSINKI_LON = 24.9384;
    private static final int MIN_ZOOM = 4;
    private static final int MAX_ZOOM = 8;
    private static final int DEFAULT_ZOOM = 6;
    private static final int THREAD_POOL_SIZE = 8;

    public GeoPosition getDefaultCenter() {
        return new GeoPosition(HELSINKI_LAT, HELSINKI_LON);
    }

    public int getMinZoom() {
        return MIN_ZOOM;
    }

    public int getMaxZoom() {
        return MAX_ZOOM;
    }

    public int getDefaultZoom() {
        return DEFAULT_ZOOM;
    }

    public int getThreadPoolSize() {
        return THREAD_POOL_SIZE;
    }
}