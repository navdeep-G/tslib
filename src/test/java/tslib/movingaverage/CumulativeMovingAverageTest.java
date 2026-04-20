package tslib.movingaverage;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CumulativeMovingAverageTest {

    @Test
    public void testCMAComputation() {
        List<Double> input = Arrays.asList(2.0, 4.0, 6.0, 8.0);
        CumulativeMovingAverage cma = new CumulativeMovingAverage();
        List<Double> output = cma.compute(input);

        assertEquals(input.size(), output.size());

        // Manually compute expected values
        assertEquals(2.0, output.get(0), 1e-6);
        assertEquals(3.0, output.get(1), 1e-6);  // (2 + 4)/2
        assertEquals(4.0, output.get(2), 1e-6);  // (2 + 4 + 6)/3
        assertEquals(5.0, output.get(3), 1e-6);  // (2 + 4 + 6 + 8)/4
    }

    @Test
    public void testResetBehavior() {
        List<Double> input = Arrays.asList(10.0, 20.0, 30.0);
        CumulativeMovingAverage cma = new CumulativeMovingAverage();

        List<Double> firstRun = cma.compute(input);
        cma.reset();
        List<Double> secondRun = cma.compute(input);

        assertEquals(firstRun.size(), secondRun.size());
        for (int i = 0; i < input.size(); i++) {
            assertEquals(firstRun.get(i), secondRun.get(i), 1e-6);
        }
    }

    @Test
    public void testEmptyInput() {
        List<Double> input = List.of();
        CumulativeMovingAverage cma = new CumulativeMovingAverage();
        List<Double> output = cma.compute(input);
        assertTrue(output.isEmpty());
    }
}
