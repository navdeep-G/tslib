package tslib.model.arima;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ARIMAXEdgeCaseTest {

    @Test
    public void multipleExogenousRegressors() {
        // y = 2*x1 + 3*x2 + 1
        List<Double> y = Arrays.asList(6.0, 11.0, 16.0, 21.0, 26.0, 31.0);
        double[][] x = {
            {1.0, 1.0}, {2.0, 2.0}, {3.0, 3.0},
            {4.0, 4.0}, {5.0, 5.0}, {6.0, 6.0}
        };
        ARIMAX model = new ARIMAX(0, 0, 0).fit(y, x);
        assertEquals(2, model.getExogenousCoefficients().length);
        List<Double> forecast = model.forecast(new double[][]{{7.0, 7.0}});
        assertEquals(1, forecast.size());
        assertFalse(Double.isNaN(forecast.get(0)));
    }

    @Test
    public void arimaxWithDifferencing() {
        List<Double> y = Arrays.asList(1.0, 3.0, 6.0, 10.0, 15.0, 21.0, 28.0, 36.0);
        double[][] x = new double[8][1];
        for (int i = 0; i < 8; i++) x[i][0] = i + 1.0;
        ARIMAX model = new ARIMAX(0, 1, 0).fit(y, x);
        List<Double> forecast = model.forecast(new double[][]{{9.0}, {10.0}});
        assertEquals(2, forecast.size());
        assertTrue(forecast.get(0) > 0);
    }

    @Test
    public void arimaxResidualsHaveExpectedSize() {
        List<Double> y = Arrays.asList(5.0, 7.0, 9.0, 11.0, 13.0, 15.0);
        double[][] x = {{1.0}, {2.0}, {3.0}, {4.0}, {5.0}, {6.0}};
        ARIMAX model = new ARIMAX(1, 0, 0).fit(y, x);
        assertEquals(y.size(), model.getResiduals().size());
    }

    @Test
    public void arimaxInnovationVarianceIsPositive() {
        List<Double> y = Arrays.asList(5.0, 7.0, 9.0, 11.0, 13.0, 15.0);
        double[][] x = {{1.0}, {2.0}, {3.0}, {4.0}, {5.0}, {6.0}};
        ARIMAX model = new ARIMAX(0, 0, 1).fit(y, x);
        assertTrue(model.getInnovationVariance() >= 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void arimaxThrowsOnNullData() {
        double[][] x = {{1.0}, {2.0}};
        new ARIMAX(0, 0, 0).fit(null, x);
    }

    @Test(expected = IllegalArgumentException.class)
    public void arimaxThrowsOnMismatchedExogenousRows() {
        List<Double> y = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        double[][] x = {{1.0}, {2.0}};
        new ARIMAX(0, 0, 0).fit(y, x);
    }

    @Test(expected = IllegalArgumentException.class)
    public void arimaxThrowsOnNullExogenous() {
        List<Double> y = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        new ARIMAX(0, 0, 0).fit(y, null);
    }

    @Test(expected = IllegalStateException.class)
    public void arimaxThrowsOnForecastBeforeFit() {
        new ARIMAX(0, 0, 0).forecast(new double[][]{{1.0}});
    }

    @Test
    public void arimaxForecastDimensionMustMatchTraining() {
        List<Double> y = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        double[][] x = {{1.0}, {2.0}, {3.0}, {4.0}, {5.0}, {6.0}};
        ARIMAX model = new ARIMAX(0, 0, 0).fit(y, x);
        try {
            model.forecast(new double[][]{{1.0, 2.0}});
            fail("Expected IllegalArgumentException for wrong exogenous dimension");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void arimaxGetArAndMaCoefficients() {
        List<Double> y = Arrays.asList(2.0, 4.0, 6.0, 8.0, 10.0, 12.0, 14.0);
        double[][] x = {{1.0}, {2.0}, {3.0}, {4.0}, {5.0}, {6.0}, {7.0}};
        ARIMAX model = new ARIMAX(1, 0, 1).fit(y, x);
        assertEquals(1, model.getArCoefficients().length);
        assertEquals(1, model.getMaCoefficients().length);
    }
}
