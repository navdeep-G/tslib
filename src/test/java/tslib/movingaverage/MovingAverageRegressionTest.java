package tslib.movingaverage;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MovingAverageRegressionTest {
    @Test
    public void simpleMovingAverageUsesNullUntilWindowIsFull() {
        SimpleMovingAverage sma = new SimpleMovingAverage(3);
        List<Double> out = sma.compute(List.of(1.0, 2.0, 3.0, 4.0));

        assertNull(out.get(0));
        assertNull(out.get(1));
        assertEquals(2.0, out.get(2), 1e-9);
        assertEquals(3.0, out.get(3), 1e-9);
    }

    @Test
    public void weightedMovingAverageBiasesTowardRecentValues() {
        WeightedMovingAverage wma = new WeightedMovingAverage(3);
        List<Double> out = wma.compute(List.of(1.0, 2.0, 4.0));

        assertNull(out.get(0));
        assertNull(out.get(1));
        assertEquals((1.0 * 1 + 2.0 * 2 + 4.0 * 3) / 6.0, out.get(2), 1e-9);
    }
}
