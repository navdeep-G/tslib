package tslib.dataquality;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class OutlierDetectorTest {

    @Test
    public void zScoreDetectsHighOutlier() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 2.0, 1.0, 100.0);
        List<Integer> outliers = OutlierDetector.zScore(data, 2.0);
        assertTrue(outliers.contains(5));
    }

    @Test
    public void zScoreNoOutliersForUniformData() {
        List<Double> data = Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0);
        List<Integer> outliers = OutlierDetector.zScore(data, 2.0);
        assertTrue(outliers.isEmpty());
    }

    @Test
    public void zScoreDetectsBothTails() {
        List<Double> data = Arrays.asList(-100.0, 0.0, 1.0, 0.0, 100.0);
        List<Integer> outliers = OutlierDetector.zScore(data, 1.0);
        assertTrue(outliers.contains(0));
        assertTrue(outliers.contains(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void zScoreThrowsOnNonPositiveThreshold() {
        OutlierDetector.zScore(Arrays.asList(1.0, 2.0, 3.0), 0.0);
    }

    @Test
    public void iqrDetectsOutlier() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 100.0);
        List<Integer> outliers = OutlierDetector.iqr(data, 1.5);
        assertTrue(outliers.contains(4));
    }

    @Test
    public void iqrNoOutliersForNormalDistribution() {
        List<Double> data = Arrays.asList(2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
        List<Integer> outliers = OutlierDetector.iqr(data, 1.5);
        assertTrue(outliers.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void iqrThrowsOnNonPositiveMultiplier() {
        OutlierDetector.iqr(Arrays.asList(1.0, 2.0, 3.0), 0.0);
    }

    @Test
    public void quantileMedianOfOddList() {
        List<Double> sorted = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        assertEquals(3.0, OutlierDetector.quantile(sorted, 0.5), 1e-10);
    }

    @Test
    public void quantileExtremes() {
        List<Double> sorted = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        assertEquals(1.0, OutlierDetector.quantile(sorted, 0.0), 1e-10);
        assertEquals(5.0, OutlierDetector.quantile(sorted, 1.0), 1e-10);
    }

    @Test
    public void quantileInterpolation() {
        List<Double> sorted = Arrays.asList(0.0, 10.0);
        assertEquals(5.0, OutlierDetector.quantile(sorted, 0.5), 1e-10);
    }
}
