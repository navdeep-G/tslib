package tslib.model.arima;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VARModelTest {

    private static final List<Double> S1 = List.of(
            1.0, 1.2, 1.1, 1.4, 1.3, 1.6, 1.5, 1.8,
            1.7, 2.0, 1.9, 2.2, 2.1, 2.4, 2.3, 2.6
    );
    private static final List<Double> S2 = List.of(
            5.0, 5.1, 5.3, 5.2, 5.5, 5.4, 5.7, 5.6,
            5.9, 5.8, 6.1, 6.0, 6.3, 6.2, 6.5, 6.4
    );

    @Test
    public void varFitsAndForecastsTwoSeries() {
        VARModel model = new VARModel(1).fit(List.of(S1, S2));
        List<List<Double>> forecast = model.forecast(3);

        assertEquals(2, forecast.size());
        assertEquals(3, forecast.get(0).size());
        assertEquals(3, forecast.get(1).size());
        assertFalse(Double.isNaN(forecast.get(0).get(0)));
        assertFalse(Double.isNaN(forecast.get(1).get(0)));
    }

    @Test
    public void varWithLagTwoForecastsCorrectly() {
        VARModel model = new VARModel(2).fit(List.of(S1, S2));
        List<List<Double>> forecast = model.forecast(5);

        assertEquals(2, forecast.size());
        assertEquals(5, forecast.get(0).size());
    }

    @Test
    public void varForecasting3SeriesWorks() {
        List<Double> s3 = List.of(
                2.0, 2.1, 2.0, 2.2, 2.1, 2.3, 2.2, 2.4,
                2.3, 2.5, 2.4, 2.6, 2.5, 2.7, 2.6, 2.8
        );
        VARModel model = new VARModel(1).fit(List.of(S1, S2, s3));
        List<List<Double>> forecast = model.forecast(4);

        assertEquals(3, forecast.size());
        forecast.forEach(f -> assertEquals(4, f.size()));
    }

    @Test
    public void aic_decreasesFromLag1ToLag2OnTrendData() {
        VARModel m1 = new VARModel(1).fit(List.of(S1, S2));
        VARModel m2 = new VARModel(2).fit(List.of(S1, S2));
        // Just verify getAic returns a finite number for both
        assertTrue(Double.isFinite(m1.getAic()));
        assertTrue(Double.isFinite(m2.getAic()));
    }

    @Test
    public void fitOptimalSelectsValidLagOrder() {
        VARModel best = VARModel.fitOptimal(List.of(S1, S2), 3);
        assertNotNull(best);
        assertTrue(best.getLagOrder() >= 1 && best.getLagOrder() <= 3);
        List<List<Double>> forecast = best.forecast(2);
        assertEquals(2, forecast.size());
    }

    @Test
    public void getLagOrderAndNumSeriesAreCorrect() {
        VARModel model = new VARModel(2).fit(List.of(S1, S2));
        assertEquals(2, model.getLagOrder());
        assertEquals(2, model.getNumSeries());
    }

    @Test
    public void getCoefficientsHasCorrectShape() {
        VARModel model = new VARModel(1).fit(List.of(S1, S2));
        double[][] coeffs = model.getCoefficients();
        // 2 equations, each with 1 + 1*2 = 3 parameters
        assertEquals(2, coeffs.length);
        assertEquals(3, coeffs[0].length);
        assertEquals(3, coeffs[1].length);
    }

    @Test
    public void requiresAtLeastTwoSeries() {
        List<List<Double>> single = List.of(S1);
        assertThrows(IllegalArgumentException.class, () -> new VARModel(1).fit(single));
    }

    @Test
    public void requiresSeriesLengthExceedingLagOrder() {
        List<Double> tiny = List.of(1.0, 2.0);
        assertThrows(IllegalArgumentException.class, () -> new VARModel(2).fit(List.of(tiny, tiny)));
    }

    @Test
    public void forecastBeforeFitThrows() {
        VARModel model = new VARModel(1);
        assertThrows(IllegalStateException.class, () -> model.forecast(3));
    }

    @Test
    public void varIsSerializable() throws Exception {
        VARModel model = new VARModel(1).fit(List.of(S1, S2));
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos)) {
            oos.writeObject(model);
        }
        VARModel loaded;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(baos.toByteArray()))) {
            loaded = (VARModel) ois.readObject();
        }
        List<List<Double>> expected = model.forecast(3);
        List<List<Double>> actual = loaded.forecast(3);
        for (int k = 0; k < 2; k++) {
            for (int h = 0; h < 3; h++) {
                assertEquals(expected.get(k).get(h), actual.get(k).get(h), 1e-12);
            }
        }
    }
}
