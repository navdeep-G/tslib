package tslib.movingaverage;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleMovingAverageTest {

    @Test
    public void testSMAWithWindow2() {
        List<Double> input = Arrays.asList(2.0, 4.0, 6.0, 8.0);
        int window = 2;

        SimpleMovingAverage sma = new SimpleMovingAverage(window);
        List<Double> result = sma.compute(input);

        // Expect: [null, 3.0, 5.0, 7.0]
        assertEquals(input.size(), result.size());

        assertNull(result.get(0));
        assertEquals(3.0, result.get(1), 1e-6);
        assertEquals(5.0, result.get(2), 1e-6);
        assertEquals(7.0, result.get(3), 1e-6);
    }

    @Test
    public void testSMAWithWindow3() {
        List<Double> input = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        int window = 3;

        SimpleMovingAverage sma = new SimpleMovingAverage(window);
        List<Double> result = sma.compute(input);

        // Expect: [null, null, 2.0, 3.0, 4.0]
        assertEquals(5, result.size());

        assertNull(result.get(0));
        assertNull(result.get(1));
        assertEquals(2.0, result.get(2), 1e-6);
        assertEquals(3.0, result.get(3), 1e-6);
        assertEquals(4.0, result.get(4), 1e-6);
    }

    @Test
    public void testEmptyInput() {
        SimpleMovingAverage sma = new SimpleMovingAverage(2);
        List<Double> result = sma.compute(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testInvalidPeriod() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleMovingAverage(0));
    }
}
