package tslib.model.arima;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ARIMAXTest {
    @Test
    public void arimaxLearnsSimpleExogenousRelationship() {
        List<Double> y = List.of(7.0, 9.0, 11.0, 13.0, 15.0, 17.0);
        double[][] x = {
                {1.0}, {2.0}, {3.0}, {4.0}, {5.0}, {6.0}
        };

        ARIMAX model = new ARIMAX(0, 0, 0).fit(y, x);
        List<Double> forecast = model.forecast(new double[][] {{7.0}, {8.0}});

        assertEquals(2, forecast.size());
        assertEquals(19.0, forecast.get(0), 0.5);
        assertEquals(21.0, forecast.get(1), 0.5);
    }
}
