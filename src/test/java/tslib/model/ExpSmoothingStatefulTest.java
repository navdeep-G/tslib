package tslib.model;

import org.junit.jupiter.api.Test;
import tslib.evaluation.IntervalForecast;
import tslib.model.expsmoothing.DoubleExpSmoothing;
import tslib.model.expsmoothing.SingleExpSmoothing;
import tslib.model.expsmoothing.TripleExpSmoothing;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExpSmoothingStatefulTest {

    private static final List<Double> NIST = Arrays.asList(
            362.0, 385.0, 432.0, 341.0, 382.0, 409.0, 498.0, 387.0, 473.0, 513.0,
            582.0, 474.0, 544.0, 582.0, 681.0, 557.0, 628.0, 707.0, 773.0, 592.0,
            627.0, 725.0, 854.0, 661.0);

    // ── SingleExpSmoothing ────────────────────────────────────────────────────

    @Test
    public void sesStatefulForecastMatchesStatelessFuturePortion() {
        SingleExpSmoothing model = new SingleExpSmoothing(0.5);
        int steps = 4;
        List<Double> stateless = model.forecast(NIST, steps);
        List<Double> stateful = model.fit(NIST).forecast(steps);
        for (int i = 0; i < steps; i++) {
            assertEquals(stateless.get(NIST.size() + i), stateful.get(i), 1e-9,
                    "step " + (i + 1) + " mismatch");
        }
    }

    @Test
    public void sesStatefulForecastIsConstant() {
        List<Double> fc = new SingleExpSmoothing(0.5).fit(NIST).forecast(5);
        assertEquals(5, fc.size());
        for (int i = 1; i < fc.size(); i++) {
            assertEquals(fc.get(0), fc.get(i), 1e-12, "SES forecast should be flat");
        }
    }

    @Test
    public void sesStatefulForecastReturnsCorrectLength() {
        assertEquals(6, new SingleExpSmoothing(0.3).fit(NIST).forecast(6).size());
    }

    @Test
    public void sesIntervalsForecastCountMatchesSteps() {
        IntervalForecast iv = new SingleExpSmoothing(0.5).fit(NIST).forecastWithIntervals(4, 0.95);
        assertEquals(4, iv.getForecasts().size());
        assertEquals(4, iv.getIntervals().size());
    }

    @Test
    public void sesIntervalsWideningWithHorizon() {
        IntervalForecast iv = new SingleExpSmoothing(0.5).fit(NIST).forecastWithIntervals(4, 0.95);
        double first = iv.getIntervals().get(0).getUpper() - iv.getIntervals().get(0).getLower();
        double last = iv.getIntervals().get(3).getUpper() - iv.getIntervals().get(3).getLower();
        assertTrue(last > first, "intervals should widen with horizon");
    }

    @Test
    public void sesThrowsIfForecastCalledBeforeFit() {
        assertThrows(IllegalStateException.class, () -> new SingleExpSmoothing(0.5).forecast(4));
    }

    @Test
    public void sesThrowsIfIntervalsCalledBeforeFit() {
        assertThrows(IllegalStateException.class,
                () -> new SingleExpSmoothing(0.5).forecastWithIntervals(4, 0.95));
    }

    // ── DoubleExpSmoothing ────────────────────────────────────────────────────

    @Test
    public void desStatefulForecastMatchesStatelessFuturePortion() {
        DoubleExpSmoothing model = new DoubleExpSmoothing(0.5, 0.6, 0);
        int steps = 4;
        List<Double> stateless = model.forecast(NIST, steps);
        List<Double> stateful = model.fit(NIST).forecast(steps);
        for (int i = 0; i < steps; i++) {
            assertEquals(stateless.get(NIST.size() + i), stateful.get(i), 1e-9,
                    "step " + (i + 1) + " mismatch");
        }
    }

    @Test
    public void desStatefulForecastFollowsTrend() {
        List<Double> fc = new DoubleExpSmoothing(0.5, 0.6, 0).fit(NIST).forecast(4);
        assertEquals(4, fc.size());
        for (double v : fc) assertTrue(v > 0, "forecast should be positive");
    }

    @Test
    public void desIntervalsWidenWithHorizon() {
        IntervalForecast iv = new DoubleExpSmoothing(0.5, 0.6, 0).fit(NIST).forecastWithIntervals(4, 0.95);
        assertEquals(4, iv.getIntervals().size());
        double first = iv.getIntervals().get(0).getUpper() - iv.getIntervals().get(0).getLower();
        double last = iv.getIntervals().get(3).getUpper() - iv.getIntervals().get(3).getLower();
        assertTrue(last > first, "intervals should widen with horizon");
    }

    @Test
    public void desThrowsIfForecastCalledBeforeFit() {
        assertThrows(IllegalStateException.class, () -> new DoubleExpSmoothing(0.5, 0.5, 0).forecast(4));
    }

    // ── TripleExpSmoothing ────────────────────────────────────────────────────

    @Test
    public void tesCleanConstructorWorks() {
        // should not require debug flag
        assertDoesNotThrow(() -> new TripleExpSmoothing(0.5, 0.4, 0.6, 4).fit(NIST).forecast(4));
    }

    @Test
    public void tesStatefulForecastReturnsCorrectLength() {
        List<Double> fc = new TripleExpSmoothing(0.5, 0.4, 0.6, 4).fit(NIST).forecast(8);
        assertEquals(8, fc.size());
    }

    @Test
    public void tesStatefulForecastValuesArePositive() {
        List<Double> fc = new TripleExpSmoothing(0.5, 0.4, 0.6, 4).fit(NIST).forecast(4);
        for (double v : fc) assertTrue(v > 0, "forecast values should be positive");
    }

    @Test
    public void tesSeasonalPatternRepeatsAfterOnePeriod() {
        TripleExpSmoothing model = new TripleExpSmoothing(0.5, 0.4, 0.6, 4).fit(NIST);
        List<Double> fc = model.forecast(8);
        // Same season at h=1 and h=5 should have same seasonal factor applied
        // (exact values differ by trend, but ratio is approximately the same)
        double ratio1 = fc.get(0) / fc.get(4);
        double ratio2 = fc.get(1) / fc.get(5);
        // Both ratios should be close to 1 (trend-adjusted), not wildly different
        assertTrue(ratio1 > 0.5 && ratio1 < 2.0, "seasonal cycling should be coherent h=1 vs h=5");
        assertTrue(ratio2 > 0.5 && ratio2 < 2.0, "seasonal cycling should be coherent h=2 vs h=6");
    }

    @Test
    public void tesIntervalsReturnCorrectCount() {
        IntervalForecast iv = new TripleExpSmoothing(0.5, 0.4, 0.6, 4).fit(NIST)
                .forecastWithIntervals(4, 0.95);
        assertEquals(4, iv.getForecasts().size());
        assertEquals(4, iv.getIntervals().size());
    }

    @Test
    public void tesThrowsIfForecastCalledBeforeFit() {
        assertThrows(IllegalStateException.class,
                () -> new TripleExpSmoothing(0.5, 0.4, 0.6, 4).forecast(4));
    }

    @Test
    public void tesThrowsIfDataTooShort() {
        List<Double> short4 = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        assertThrows(IllegalArgumentException.class,
                () -> new TripleExpSmoothing(0.5, 0.4, 0.6, 4).fit(short4));
    }

    // ── TimeSeriesModel interface ─────────────────────────────────────────────

    @Test
    public void allEtsModelsImplementTimeSeriesModel() {
        tslib.model.TimeSeriesModel ses = new SingleExpSmoothing(0.5);
        tslib.model.TimeSeriesModel des = new DoubleExpSmoothing(0.5, 0.5, 0);
        tslib.model.TimeSeriesModel tes = new TripleExpSmoothing(0.5, 0.4, 0.6, 4);
        assertNotNull(ses.fit(NIST).forecast(2));
        assertNotNull(des.fit(NIST).forecast(2));
        assertNotNull(tes.fit(NIST).forecast(2));
    }
}
