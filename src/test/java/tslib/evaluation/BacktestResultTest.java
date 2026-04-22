package tslib.evaluation;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BacktestResultTest {

    private static final List<Double> ACTUAL   = List.of(10.0, 20.0, 30.0);
    private static final List<Double> FORECAST = List.of(11.0, 19.0, 31.0);
    private static final List<Integer> ORIGINS = List.of(0, 1, 2);
    private static final List<Double> FULL     = List.of(5.0, 7.0, 10.0, 20.0, 30.0);
    private static final int PERIOD = 1;

    private BacktestResult build() {
        return new BacktestResult(ACTUAL, FORECAST, ORIGINS, FULL, PERIOD);
    }

    @Test
    public void getMaeIsCorrect() {
        assertEquals(1.0, build().getMae(), 1e-9);
    }

    @Test
    public void getRmseIsCorrect() {
        assertEquals(1.0, build().getRmse(), 1e-9);
    }

    @Test
    public void getMapeIsPositive() {
        assertTrue(build().getMape() > 0.0);
    }

    @Test
    public void getSmapeIsPositive() {
        assertTrue(build().getSmape() > 0.0);
    }

    @Test
    public void getMaseIsPositive() {
        assertTrue(build().getMase() > 0.0);
    }

    @Test
    public void getActualReturnsDefensiveCopy() {
        BacktestResult result = build();
        result.getActual().add(99.0);
        assertEquals(3, result.getActual().size());
    }

    @Test
    public void getForecastReturnsDefensiveCopy() {
        BacktestResult result = build();
        result.getForecast().add(99.0);
        assertEquals(3, result.getForecast().size());
    }

    @Test
    public void getOriginsReturnsDefensiveCopy() {
        BacktestResult result = build();
        result.getOrigins().add(99);
        assertEquals(3, result.getOrigins().size());
    }

    @Test
    public void constructorIsDefensiveOverInputLists() {
        List<Double> actual = new ArrayList<>(ACTUAL);
        BacktestResult result = new BacktestResult(actual, FORECAST, ORIGINS, FULL, PERIOD);
        actual.add(99.0);
        assertEquals(3, result.getActual().size());
    }

    @Test
    public void perfectForecastHasZeroMae() {
        BacktestResult r = new BacktestResult(ACTUAL, ACTUAL, ORIGINS, FULL, PERIOD);
        assertEquals(0.0, r.getMae(), 1e-15);
    }
}
