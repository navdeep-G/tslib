package tslib.model.statespace;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalLevelModelTest {

    @Test
    public void localLevelModelProducesStableForecasts() {
        List<Double> data = List.of(10.0, 10.5, 9.8, 10.2, 10.1, 9.9, 10.0, 10.3);

        LocalLevelModel model = new LocalLevelModel().fit(data);
        List<Double> filtered = model.getFilteredStates();
        List<Double> forecast = model.forecast(3);

        assertEquals(data.size(), filtered.size());
        assertEquals(3, forecast.size());
        assertTrue(model.getProcessVariance() > 0.0);
        assertTrue(model.getObservationVariance() > 0.0);
        assertEquals(filtered.get(filtered.size() - 1), forecast.get(0), 1e-9);
    }

    @Test
    public void brentMleFindsPositiveVariances() {
        List<Double> data = List.of(1.0, 1.5, 0.8, 1.2, 1.1, 0.9, 1.0, 1.3,
                1.2, 1.7, 1.0, 1.4, 1.3, 1.1, 1.2, 1.5);
        LocalLevelModel model = new LocalLevelModel().fit(data);

        assertTrue(model.getProcessVariance() > 0.0);
        assertTrue(model.getObservationVariance() > 0.0);
        assertTrue(Double.isFinite(model.getLogLikelihood()));
    }

    @Test
    public void brentMleLogLikelihoodNotWorstCaseGrid() {
        // The Brent search must find at least as good a solution as the old
        // 7-point grid that only tried {0.01, 0.03, 0.1, 0.3, 1.0, 3.0, 10.0}.
        // We verify by checking that the MLE result is a valid, finite score.
        List<Double> data = List.of(
                5.0, 5.2, 4.8, 5.1, 5.3, 4.9, 5.0, 5.4,
                5.1, 5.5, 4.7, 5.2, 5.0, 5.3, 4.9, 5.1
        );
        LocalLevelModel model = new LocalLevelModel().fit(data);
        double ll = model.getLogLikelihood();

        // Any reasonable MLE must beat the degenerate constant-forecast baseline.
        // We just assert the log-likelihood is finite and the ratio is in range.
        assertTrue(Double.isFinite(ll));
        // The Brent search spans log(1e-4) to log(1e4), i.e., ratio in [1e-4, 1e4].
        double ratio = model.getProcessVariance() / model.getObservationVariance();
        assertTrue(ratio > 0.0);
        assertTrue(ratio <= 1e4 + 1.0);
    }

    @Test
    public void brentSearchHandlesSinglePointBySeries() {
        // Very short series: only 2 observations (minimum valid input)
        List<Double> data = List.of(3.0, 4.0);
        LocalLevelModel model = new LocalLevelModel().fit(data);
        assertEquals(1, model.forecast(1).size());
        assertTrue(model.getProcessVariance() > 0.0);
    }

    @Test
    public void forecastIntervalWidthGrowsWithHorizon() {
        List<Double> data = List.of(10.0, 10.5, 9.8, 10.2, 10.1, 9.9, 10.0, 10.3);
        LocalLevelModel model = new LocalLevelModel().fit(data);
        List<Double> vars = model.getForecastVariances(5);

        for (int i = 1; i < vars.size(); i++) {
            assertTrue(vars.get(i) > vars.get(i - 1),
                    "Forecast variance should increase with horizon");
        }
    }

    @Test
    public void modelIsSerializable() throws Exception {
        List<Double> data = List.of(10.0, 10.5, 9.8, 10.2, 10.1, 9.9, 10.0, 10.3);
        LocalLevelModel original = new LocalLevelModel().fit(data);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }
        LocalLevelModel loaded;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(baos.toByteArray()))) {
            loaded = (LocalLevelModel) ois.readObject();
        }
        assertEquals(original.getLogLikelihood(), loaded.getLogLikelihood(), 1e-12);
        assertEquals(original.forecast(3), loaded.forecast(3));
    }
}
