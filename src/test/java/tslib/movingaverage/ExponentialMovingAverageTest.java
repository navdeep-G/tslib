package tslib.movingaverage;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExponentialMovingAverageTest {

    @Test
    public void testValidEMAComputation() {
        List<Double> input = Arrays.asList(10.0, 12.0, 14.0, 13.0, 15.0);
        double alpha = 0.5;
        ExponentialMovingAverage ema = new ExponentialMovingAverage(alpha);
        List<Double> output = ema.compute(input);

        assertEquals(input.size(), output.size());

        // First value of EMA should equal first input
        assertEquals(input.get(0), output.get(0), 1e-6);

        // EMA should be between previous EMA and current value
        for (int i = 1; i < output.size(); i++) {
            double prev = output.get(i - 1);
            double curr = output.get(i);
            double actual = input.get(i);
            assertTrue((curr > prev && curr < actual) || (curr < prev && curr > actual), "EMA should move toward input");
        }
    }

    @Test
    public void testResetBehavior() {
        List<Double> input = Arrays.asList(5.0, 6.0, 7.0);
        ExponentialMovingAverage ema = new ExponentialMovingAverage(0.3);
        List<Double> firstRun = ema.compute(input);

        ema.reset();
        List<Double> secondRun = ema.compute(input);

        assertEquals(firstRun.size(), secondRun.size());
        for (int i = 0; i < input.size(); i++) {
            assertEquals(firstRun.get(i), secondRun.get(i), 1e-6);
        }
    }

    @Test
    public void testInvalidAlphaZero() {
        assertThrows(IllegalArgumentException.class, () -> new ExponentialMovingAverage(0.0));
    }

    @Test
    public void testInvalidAlphaGreaterThanOne() {
        assertThrows(IllegalArgumentException.class, () -> new ExponentialMovingAverage(1.1));
    }
}
