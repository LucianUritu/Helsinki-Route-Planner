package UI.Components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WeatherIcon {
    private static final Path ICON_PATH = Paths.get("src", "main", "java", "UI", "Images", "weather.png");
    private static final double ICON_SIZE = 90;
    private final ImageView iconView;

    public WeatherIcon() {
        this.iconView = createWeatherIcon();
    }

    private ImageView createWeatherIcon() {
        try {
            Image icon = new Image(new FileInputStream(ICON_PATH.toAbsolutePath().toFile()));
            ImageView view = new ImageView(icon);
            view.setFitHeight(ICON_SIZE);  // Match the temperature font size
            view.setFitWidth(ICON_SIZE);
            view.setPreserveRatio(true);
            view.setStyle("-fx-background-color: transparent;");  // Ensure transparent background
            return view;
        } catch (IOException e) {
            e.printStackTrace();
            return new ImageView();
        }
    }

    public ImageView getDisplay() {
        return iconView;
    }
}