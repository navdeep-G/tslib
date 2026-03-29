package tslib.model;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArimaxRegressionTest {

    @Test(expected = IllegalArgumentException.class)
    public void fitRejectsMismatchedExogenousRowCount() {
        new ARIMAX(0, 0, 0).fit(List.of(1.0, 2.0, 3.0), new double[][] {{1.0}, {2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void forecastRejectsWrongFutureFeatureWidth() {
        ARIMAX model = new ARIMAX(0, 0, 0).fit(
                List.of(7.0, 9.0, 11.0, 13.0),
                new double[][] {{1.0}, {2.0}, {3.0}, {4.0}});
        model.forecast(new double[][] {{5.0, 6.0}});
    }
}
