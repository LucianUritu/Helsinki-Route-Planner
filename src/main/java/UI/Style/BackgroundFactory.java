package UI.Style;

import UI.Config.UIConfig;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class BackgroundFactory {
    public static Background createGradientBackground() {
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(UIConfig.COLOR_LIGHT_BLUE)),
                new Stop(1, Color.web(UIConfig.COLOR_PURPLE))
        );

        return new Background(new BackgroundFill(
                gradient, CornerRadii.EMPTY, Insets.EMPTY
        ));
    }
}
