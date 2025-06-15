package UI.Components;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CityDisplay {
    private static final String FONT_FAMILY = "Raleway";
    private static final int FONT_SIZE = 70;
    private final Label cityLabel;
    private String cityName;

    public CityDisplay(String cityName) {
        this.cityName = cityName;
        this.cityLabel = createCityLabel();
    }

    private Label createCityLabel() {
        Label label = new Label(cityName);
        label.setFont(new Font(FONT_FAMILY, FONT_SIZE));
        label.setTextFill(Color.WHITE);
        return label;
    }

    public Label getDisplay() {
        return cityLabel;
    }

    public void updateCity(String newCity) {
        this.cityName = newCity;
        cityLabel.setText(newCity);
    }
}