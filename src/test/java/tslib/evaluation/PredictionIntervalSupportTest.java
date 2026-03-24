package tslib.evaluation;

import java.util.List;
import org.junit.Test;
import tslib.model.ARIMA;
import tslib.model.LocalLevelModel;
import static org.junit.Assert.*;

public class PredictionIntervalSupportTest {
    @Test
    public void arimaForecastIntervalsExpandByHorizon() {
        ARIMA model = new ARIMA(0, 1, 0).fit(List.of(1.0, 2.0, 3.0, 4.0));
        List<PredictionInterval> intervals = model.forecastIntervals(3, 0.95);

        assertEquals(3, intervals.size());
        double width1 = intervals.get(0).getUpper() - intervals.get(0).getLower();
        double width3 = intervals.get(2).getUpper() - intervals.get(2).getLower();
        assertTrue(width3 > width1);
    }

    @Test
    public void localLevelForecastWithIntervalsMatchesPointCount() {
        LocalLevelModel model = new LocalLevelModel().fit(List.of(10.0, 10.1, 9.9, 10.2, 10.1));
        IntervalForecast forecast = model.forecastWithIntervals(2, 0.9);
        assertEquals(2, forecast.getForecasts().size());
        assertEquals(2, forecast.getIntervals().size());
    }
}
