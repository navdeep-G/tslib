package tslib.evaluation;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BenchmarkSummaryTest {

    private BenchmarkSummary direct() {
        return new BenchmarkSummary("ModelA", 1.0, 2.0, 3.0, 4.0, 5.0);
    }

    @Test
    public void gettersReturnConstructorValues() {
        BenchmarkSummary s = direct();
        assertEquals("ModelA", s.getModelName());
        assertEquals(1.0, s.getMae(), 1e-15);
        assertEquals(2.0, s.getRmse(), 1e-15);
        assertEquals(3.0, s.getMape(), 1e-15);
        assertEquals(4.0, s.getSmape(), 1e-15);
        assertEquals(5.0, s.getMase(), 1e-15);
    }

    @Test
    public void constructFromBacktestResult() {
        List<Double> actual   = List.of(10.0, 20.0, 30.0);
        List<Double> forecast = List.of(11.0, 19.0, 31.0);
        BacktestResult result = new BacktestResult(
                actual, forecast, List.of(0, 1, 2),
                List.of(5.0, 7.0, 10.0, 20.0, 30.0), 1);

        BenchmarkSummary s = new BenchmarkSummary("TestModel", result);
        assertEquals("TestModel", s.getModelName());
        assertEquals(result.getMae(),   s.getMae(),   1e-15);
        assertEquals(result.getRmse(),  s.getRmse(),  1e-15);
        assertEquals(result.getMape(),  s.getMape(),  1e-15);
        assertEquals(result.getSmape(), s.getSmape(), 1e-15);
        assertEquals(result.getMase(),  s.getMase(),  1e-15);
    }

    @Test
    public void compareToSortsByRmseAscending() {
        BenchmarkSummary better = new BenchmarkSummary("A", 1.0, 1.0, 1.0, 1.0, 1.0);
        BenchmarkSummary worse  = new BenchmarkSummary("B", 1.0, 5.0, 1.0, 1.0, 1.0);
        assertTrue(better.compareTo(worse) < 0);
        assertTrue(worse.compareTo(better) > 0);
    }

    @Test
    public void compareToEqualRmseReturnsZero() {
        BenchmarkSummary a = new BenchmarkSummary("A", 1.0, 2.0, 3.0, 4.0, 5.0);
        BenchmarkSummary b = new BenchmarkSummary("B", 9.0, 2.0, 9.0, 9.0, 9.0);
        assertEquals(0, a.compareTo(b));
    }
}
