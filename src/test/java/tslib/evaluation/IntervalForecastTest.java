package tslib.evaluation;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class IntervalForecastTest {

    private static List<Double> forecasts() {
        return new ArrayList<>(List.of(10.0, 11.0, 12.0));
    }

    private static List<PredictionInterval> intervals() {
        return new ArrayList<>(List.of(
                new PredictionInterval(1, 10.0, 8.0, 12.0, 0.95),
                new PredictionInterval(2, 11.0, 8.5, 13.5, 0.95),
                new PredictionInterval(3, 12.0, 9.0, 15.0, 0.95)));
    }

    @Test
    public void getForecastsReturnsAllValues() {
        IntervalForecast f = new IntervalForecast(forecasts(), intervals());
        assertEquals(List.of(10.0, 11.0, 12.0), f.getForecasts());
    }

    @Test
    public void getIntervalsReturnsAllValues() {
        IntervalForecast f = new IntervalForecast(forecasts(), intervals());
        assertEquals(3, f.getIntervals().size());
    }

    @Test
    public void getForecastsReturnsDefensiveCopy() {
        IntervalForecast f = new IntervalForecast(forecasts(), intervals());
        f.getForecasts().add(99.0);
        assertEquals(3, f.getForecasts().size());
    }

    @Test
    public void getIntervalsReturnsDefensiveCopy() {
        IntervalForecast f = new IntervalForecast(forecasts(), intervals());
        f.getIntervals().clear();
        assertEquals(3, f.getIntervals().size());
    }

    @Test
    public void constructorIsDefensiveOverInputLists() {
        List<Double> fc = forecasts();
        IntervalForecast f = new IntervalForecast(fc, intervals());
        fc.add(99.0);
        assertEquals(3, f.getForecasts().size());
    }

    @Test
    public void intervalHorizonAndBoundsAreCorrect() {
        PredictionInterval pi = new PredictionInterval(1, 10.0, 8.0, 12.0, 0.95);
        assertEquals(1, pi.getStep());
        assertEquals(10.0, pi.getPointForecast(), 1e-15);
        assertEquals(8.0,  pi.getLower(), 1e-15);
        assertEquals(12.0, pi.getUpper(), 1e-15);
        assertEquals(0.95, pi.getConfidenceLevel(), 1e-15);
    }

    @Test
    public void predictionIntervalRejectsStepZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictionInterval(0, 10.0, 8.0, 12.0, 0.95));
    }

    @Test
    public void predictionIntervalRejectsNegativeStep() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictionInterval(-1, 10.0, 8.0, 12.0, 0.95));
    }

    @Test
    public void predictionIntervalRejectsConfidenceZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictionInterval(1, 10.0, 8.0, 12.0, 0.0));
    }

    @Test
    public void predictionIntervalRejectsConfidenceOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictionInterval(1, 10.0, 8.0, 12.0, 1.0));
    }
}
