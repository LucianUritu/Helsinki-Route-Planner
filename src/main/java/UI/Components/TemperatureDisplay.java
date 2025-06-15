package UI.Components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TemperatureDisplay {
    private static final String FONT_FAMILY = "Raleway";
    private static final int FONT_SIZE = 70;
    private final HBox container;
    private final WeatherIcon weatherIcon;
    private double temperature;

    public TemperatureDisplay(double temperature) {
        this.temperature = temperature;
        this.weatherIcon = new WeatherIcon();
        this.container = createTemperatureDisplay();
    }

    private HBox createTemperatureDisplay() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        Label tempLabel = new Label(String.format("%.0f°", temperature));
        tempLabel.setFont(new Font(FONT_FAMILY, FONT_SIZE));
        tempLabel.setTextFill(Color.WHITE);

        box.getChildren().addAll(tempLabel, weatherIcon.getDisplay());
        box.setStyle("-fx-background-color: transparent;");

        return box;
    }

    public HBox getDisplay() {
        return container;
    }

    public void updateTemperature(double newTemp) {
        this.temperature = newTemp;
        Label tempLabel = (Label) container.getChildren().get(0);
        tempLabel.setText(String.format("%.0f°", temperature));
    }
}