package tslib.decomposition;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class STLRobustnessTest {

    /** Builds a clean trend+seasonal series, then inserts one large outlier. */
    private static List<Double> buildOutlierSeries(int outlierIndex, double outlierMagnitude) {
        double[] seasonal = {3.0, -1.5, 2.0, -2.0, 1.5, -1.0};
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            data.add(10.0 + i * 0.2 + seasonal[i % seasonal.length]);
        }
        data.set(outlierIndex, data.get(outlierIndex) + outlierMagnitude);
        return data;
    }

    @Test
    public void robustnessReducesOutlierInfluenceOnTrend() {
        List<Double> data = buildOutlierSeries(12, 20.0);

        STLDecomposition plain = new STLDecomposition(6, 7, 7, 2, 0);
        STLDecomposition robust = new STLDecomposition(6, 7, 7, 2, 2);

        STLDecomposition.Result plainResult = plain.decompose(data);
        STLDecomposition.Result robustResult = robust.decompose(data);

        // Robust trend should have smaller deviation at the outlier index
        double plainTrendDeviation = Math.abs(plainResult.getTrend().get(12) - plainResult.getTrend().get(11));
        double robustTrendDeviation = Math.abs(robustResult.getTrend().get(12) - robustResult.getTrend().get(11));
        assertTrue(robustTrendDeviation < plainTrendDeviation,
                "Robust trend deviation " + robustTrendDeviation
                        + " should be less than non-robust " + plainTrendDeviation);
    }

    @Test
    public void robustnessAbsorbsOutlierIntoRemainder() {
        List<Double> data = buildOutlierSeries(6, 15.0);

        STLDecomposition plain = new STLDecomposition(6, 7, 7, 2, 0);
        STLDecomposition robust = new STLDecomposition(6, 7, 7, 2, 2);

        STLDecomposition.Result plainResult = plain.decompose(data);
        STLDecomposition.Result robustResult = robust.decompose(data);

        double robustRemainder = Math.abs(robustResult.getRemainder().get(6));
        double plainRemainder = Math.abs(plainResult.getRemainder().get(6));

        // Robust decomposition should push more of the anomaly into remainder
        assertTrue(robustRemainder > plainRemainder,
                "Robust remainder " + robustRemainder + " should exceed plain " + plainRemainder);
    }

    @Test
    public void zeroOuterIterationsMatchesOriginalBehavior() {
        List<Double> data = buildOutlierSeries(5, 0.0); // no actual outlier
        STLDecomposition legacy = new STLDecomposition(6, 7, 7, 2);
        STLDecomposition explicit = new STLDecomposition(6, 7, 7, 2, 0);

        STLDecomposition.Result legacyResult = legacy.decompose(data);
        STLDecomposition.Result explicitResult = explicit.decompose(data);

        List<Double> legacyTrend = legacyResult.getTrend();
        List<Double> explicitTrend = explicitResult.getTrend();
        for (int i = 0; i < legacyTrend.size(); i++) {
            assertEquals(legacyTrend.get(i), explicitTrend.get(i), 1e-10,
                    "Trend differs at index " + i);
        }
    }

    @Test
    public void robustResultHasCorrectDimensions() {
        List<Double> data = buildOutlierSeries(3, 5.0);
        STLDecomposition robust = new STLDecomposition(6, 7, 7, 2, 1);
        STLDecomposition.Result result = robust.decompose(data);

        assertEquals(data.size(), result.getTrend().size());
        assertEquals(data.size(), result.getSeasonal().size());
        assertEquals(data.size(), result.getRemainder().size());
    }

    @Test
    public void reconstructionIsIdenticalToInput() {
        List<Double> data = buildOutlierSeries(9, 8.0);
        STLDecomposition robust = new STLDecomposition(6, 7, 7, 2, 2);
        STLDecomposition.Result result = robust.decompose(data);

        List<Double> reconstructed = result.reconstruct();
        for (int i = 0; i < data.size(); i++) {
            assertEquals(data.get(i), reconstructed.get(i), 1e-9,
                    "Reconstruction mismatch at index " + i);
        }
    }

    @Test
    public void negativeOuterIterationsThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new STLDecomposition(6, 7, 7, 2, -1));
    }
}
