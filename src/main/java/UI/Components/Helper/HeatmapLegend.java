package UI.Components.Helper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class HeatmapLegend {
    private static final double LEGEND_WIDTH = 250;
    private static final double LEGEND_HEIGHT = 20;
    private static final String FONT_FAMILY = "Raleway";

    public VBox createLegend() {
        Rectangle gradientRect = new Rectangle(LEGEND_WIDTH, LEGEND_HEIGHT);
        gradientRect.setFill(createGradient());

        HBox timeLabels = createTimeLabels();

        Label title = new Label("Travel Time Heatmap");
        title.setFont(Font.font(FONT_FAMILY, 14));

        VBox legendContainer = new VBox(2);
        legendContainer.getChildren().addAll(title, gradientRect, timeLabels);
        legendContainer.setAlignment(Pos.CENTER);
        legendContainer.setPadding(new Insets(5));
        legendContainer.setStyle("-fx-background-color: white;");
        legendContainer.setMaxWidth(LEGEND_WIDTH);

        return legendContainer;
    }

    private LinearGradient createGradient() {
        Stop[] stops = new Stop[5];
        double[] positions = {0.0, 0.25, 0.5, 0.75, 1.0};
        double min = 0;
        double max = 120;

        for (int i = 0; i < 5; i++) {
            double value = min + positions[i] * (max - min);
            stops[i] = new Stop(positions[i], getColorForValue(value, min, max));
        }

        return new LinearGradient(0, 0, 1, 0, true, null, stops);
    }

    private Color getColorForValue(double value, double min, double max) {
        if (value > max) value = max;

        float normalized = (float) ((value - min) / (max - min));

        float red = normalized;
        float green = 1 - normalized;

        return Color.rgb(
                (int) (red * 255),
                (int) (green * 255),
                0,
                0.6
        );
    }

    private HBox createTimeLabels() {
        HBox labels = new HBox();
        labels.setAlignment(Pos.CENTER);
        labels.setSpacing(40);

        String[] times = {"0", "15", "30", "60", "90+"};
        for (String time : times) {
            Label label = new Label(time);
            label.setFont(Font.font(FONT_FAMILY, 11));
            label.setStyle("-fx-text-fill: rgb(132, 132, 132);");
            labels.getChildren().add(label);
        }

        return labels;
    }
}
