package tslib.util;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StatsOptimizationTest {

    @Test
    public void getMinimumIndexReturnsFirstOccurrence() {
        List<Double> data = Arrays.asList(3.0, 1.0, 4.0, 1.0, 5.0);
        assertEquals(1, Stats.getMinimumIndex(data));
    }

    @Test
    public void getMaximumIndexReturnsFirstOccurrence() {
        List<Double> data = Arrays.asList(3.0, 9.0, 4.0, 9.0, 2.0);
        assertEquals(1, Stats.getMaximumIndex(data));
    }

    @Test
    public void getMinimumIndexSingleElement() {
        List<Double> data = Arrays.asList(42.0);
        assertEquals(0, Stats.getMinimumIndex(data));
    }

    @Test
    public void getMaximumIndexSingleElement() {
        List<Double> data = Arrays.asList(42.0);
        assertEquals(0, Stats.getMaximumIndex(data));
    }

    @Test
    public void getMinimumIndexAllEqual() {
        List<Double> data = Arrays.asList(5.0, 5.0, 5.0);
        assertEquals(0, Stats.getMinimumIndex(data));
    }

    @Test
    public void getMinimumMatchesMinValue() {
        List<Double> data = Arrays.asList(3.0, -1.5, 7.0, 2.0);
        int idx = Stats.getMinimumIndex(data);
        assertEquals(data.get(idx), Stats.getMinimum(data), 1e-15);
        assertEquals(-1.5, Stats.getMinimum(data), 1e-15);
    }

    @Test
    public void getMaximumMatchesMaxValue() {
        List<Double> data = Arrays.asList(3.0, -1.5, 7.0, 2.0);
        int idx = Stats.getMaximumIndex(data);
        assertEquals(data.get(idx), Stats.getMaximum(data), 1e-15);
        assertEquals(7.0, Stats.getMaximum(data), 1e-15);
    }

    @Test
    public void getMinMaxConsistentWithIndividualMethods() {
        List<Double> data = Arrays.asList(4.0, 2.0, 8.0, 1.0, 5.0);
        double[] minMax = Stats.getMinMax(data);
        assertEquals(Stats.getMinimum(data), minMax[0], 1e-15);
        assertEquals(Stats.getMaximum(data), minMax[1], 1e-15);
    }

    @Test
    public void getMinMaxIndicesConsistentWithIndividualMethods() {
        List<Double> data = Arrays.asList(4.0, 2.0, 8.0, 1.0, 5.0);
        int[] indices = Stats.getMinMaxIndices(data);
        assertEquals(Stats.getMinimumIndex(data), indices[0]);
        assertEquals(Stats.getMaximumIndex(data), indices[1]);
    }

    @Test
    public void getMinimumIndexWithNegativeValues() {
        List<Double> data = Arrays.asList(-3.0, -10.0, -1.0, -7.0);
        assertEquals(1, Stats.getMinimumIndex(data));
        assertEquals(-10.0, Stats.getMinimum(data), 1e-15);
    }

    @Test
    public void getMinimumIndexThrowsOnEmpty() {
        assertThrows(IllegalArgumentException.class, () -> Stats.getMinimumIndex(Arrays.asList()));
    }

    @Test
    public void getMaximumIndexThrowsOnEmpty() {
        assertThrows(IllegalArgumentException.class, () -> Stats.getMaximumIndex(Arrays.asList()));
    }

    @Test
    public void largeArrayCorrectness() {
        List<Double> data = new java.util.ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            data.add((double) i);
        }
        data.set(5000, -999.0);
        assertEquals(5000, Stats.getMinimumIndex(data));
        assertEquals(9999, Stats.getMaximumIndex(data));
    }
}
