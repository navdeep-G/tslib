package tslib.decomposition;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class STLDecompositionEdgeCaseTest {

    private static List<Double> pureSeasonalData(int periods, int period) {
        double[] pattern = new double[period];
        for (int i = 0; i < period; i++) {
            pattern[i] = Math.sin(2 * Math.PI * i / period) * 5.0;
        }
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < periods * period; i++) {
            data.add(10.0 + pattern[i % period]);
        }
        return data;
    }

    @Test
    public void reconstructionEqualsOriginal() {
        List<Double> data = pureSeasonalData(4, 4);
        STLDecomposition stl = new STLDecomposition(4);
        STLDecomposition.Result result = stl.decompose(data);
        List<Double> reconstructed = result.reconstruct();
        assertEquals(data.size(), reconstructed.size());
        for (int i = 0; i < data.size(); i++) {
            assertEquals(data.get(i), reconstructed.get(i), 1e-9);
        }
    }

    @Test
    public void seasonalComponentHasZeroMean() {
        List<Double> data = pureSeasonalData(6, 12);
        STLDecomposition stl = new STLDecomposition(12);
        STLDecomposition.Result result = stl.decompose(data);
        double sum = result.getSeasonal().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(0.0, sum, 1.0);
    }

    @Test
    public void trendCaptureLevelShift() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            data.add(i < 12 ? 5.0 : 15.0);
        }
        STLDecomposition stl = new STLDecomposition(4);
        STLDecomposition.Result result = stl.decompose(data);
        double earlyTrend = result.getTrend().subList(0, 4).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double lateTrend = result.getTrend().subList(20, 24).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        assertTrue(lateTrend > earlyTrend, "Late trend should be higher than early trend");
    }

    @Test
    public void customParametersAreRespected() {
        List<Double> data = pureSeasonalData(5, 4);
        STLDecomposition stl = new STLDecomposition(4, 7, 7, 3);
        STLDecomposition.Result result = stl.decompose(data);
        assertEquals(data.size(), result.getTrend().size());
        assertEquals(data.size(), result.getSeasonal().size());
        assertEquals(data.size(), result.getRemainder().size());
    }

    @Test
    public void constantSeriesProducesNearZeroRemainder() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) data.add(7.0);
        STLDecomposition stl = new STLDecomposition(4);
        STLDecomposition.Result result = stl.decompose(data);
        for (double r : result.getRemainder()) {
            assertEquals(0.0, r, 1e-6);
        }
    }

    @Test
    public void throwsOnPeriodLessThanTwo() {
        assertThrows(IllegalArgumentException.class, () -> new STLDecomposition(1));
    }

    @Test
    public void throwsOnTooShortSeries() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) data.add((double) i);
        assertThrows(IllegalArgumentException.class, () -> new STLDecomposition(4).decompose(data));
    }

    @Test
    public void throwsOnEvenTrendWindow() {
        assertThrows(IllegalArgumentException.class, () -> new STLDecomposition(4, 6, 7, 2));
    }

    @Test
    public void throwsOnEvenSeasonalWindow() {
        assertThrows(IllegalArgumentException.class, () -> new STLDecomposition(4, 7, 6, 2));
    }

    @Test
    public void resultIsImmutable() {
        List<Double> data = pureSeasonalData(3, 4);
        STLDecomposition stl = new STLDecomposition(4);
        STLDecomposition.Result result = stl.decompose(data);
        assertThrows(UnsupportedOperationException.class, () -> result.getTrend().set(0, 999.0));
    }
}
