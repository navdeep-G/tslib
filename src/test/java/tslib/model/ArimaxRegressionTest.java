package tslib.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArimaxRegressionTest {

    @Test
    public void fitRejectsMismatchedExogenousRowCount() {
        assertThrows(IllegalArgumentException.class, () ->
                new ARIMAX(0, 0, 0).fit(List.of(1.0, 2.0, 3.0), new double[][] {{1.0}, {2.0}}));
    }

    @Test
    public void forecastRejectsWrongFutureFeatureWidth() {
        ARIMAX model = new ARIMAX(0, 0, 0).fit(
                List.of(7.0, 9.0, 11.0, 13.0),
                new double[][] {{1.0}, {2.0}, {3.0}, {4.0}});
        assertThrows(IllegalArgumentException.class, () -> model.forecast(new double[][] {{5.0, 6.0}}));
    }
}
