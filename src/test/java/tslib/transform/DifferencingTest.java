package tslib.transform;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DifferencingTest {

    @Test
    public void firstDifferenceCanBeInvertedBackToOriginalTail() {
        List<Double> data = Arrays.asList(10.0, 13.0, 15.0, 18.0);
        List<Double> diff = Differencing.difference(data);
        List<Double> restored = Differencing.inverseDifference(diff, List.of(data.get(0)));

        assertEquals(Arrays.asList(3.0, 2.0, 3.0), diff);
        assertEquals(Arrays.asList(13.0, 15.0, 18.0), restored);
    }

    @Test
    public void secondDifferenceCanBeInvertedWithTwoPointHistory() {
        List<Double> data = Arrays.asList(1.0, 2.0, 4.0, 7.0, 11.0);
        List<Double> diff2 = Differencing.difference(data, 2);
        List<Double> restored = Differencing.inverseDifference(diff2, data.subList(0, 2), 2);

        assertEquals(Arrays.asList(1.0, 1.0, 1.0), diff2);
        assertEquals(Arrays.asList(4.0, 7.0, 11.0), restored);
    }

    @Test
    public void seasonalDifferenceUsesConfiguredLag() {
        List<Double> data = Arrays.asList(10.0, 12.0, 15.0, 13.0, 16.0, 20.0);
        List<Double> seasonal = Differencing.seasonalDifference(data, 3);

        assertEquals(Arrays.asList(3.0, 4.0, 5.0), seasonal);
    }
}
