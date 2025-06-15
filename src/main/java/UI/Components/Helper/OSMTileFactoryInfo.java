package UI.Components.Helper;

import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 * Uses OpenStreetMap
 */
public class OSMTileFactoryInfo extends TileFactoryInfo {
    private static final int MAX_ZOOM = 19;

    /**
     * Default constructor
     */
    public OSMTileFactoryInfo() {
        this("OpenStreetMap", "http://tile.openstreetmap.org");
    }

    /**
     * @param name    the name of the factory
     * @param baseURL the base URL to load tiles from
     */
    public OSMTileFactoryInfo(String name, String baseURL) {
        super(name,
                0, MAX_ZOOM, MAX_ZOOM,
                256, true, true,
                baseURL,
                "x", "y", "z");
    }

    @Override
    public String getTileUrl(int x, int y, int zoom) {
        int invZoom = MAX_ZOOM - zoom;
        String url = this.baseURL + "/" + invZoom + "/" + x + "/" + y + ".png";
        return url;
    }
}