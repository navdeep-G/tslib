package tslib.tstest;

import java.util.ArrayList;
import tslib.tests.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit Test for Augmented Dickey Fuller
 * Verifies detection of non-stationarity.
 */
public class AugmentedDickeyFullerTest {

    @Test
    public void testClearLinearTrend() {
        ArrayList<Double> x = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            x.add(i * 1.5);  // Strong linear trend, no randomness
        }
        AugmentedDickeyFuller adf = new AugmentedDickeyFuller(x);
        System.out.println("ADF stat (linear trend): " + adf.getAdfStat());
        assertFalse(adf.isNeedsDiff(), "Expected non-stationary series");
    }

    @Test
    public void testLinearTrendWithOutlier() {
        ArrayList<Double> x = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            x.add(i * 1.5);
        }
        x.set(50, 1000.0);  // Inject an outlier
        AugmentedDickeyFuller adf = new AugmentedDickeyFuller(x);
        System.out.println("ADF stat (trend + outlier): " + adf.getAdfStat());
        assertFalse(adf.isNeedsDiff(), "Expected non-stationary series with outlier");
    }

    @Test
    public void testGetPValueInRange() {
        ArrayList<Double> x = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            x.add(i * 1.5);
        }
        AugmentedDickeyFuller adf = new AugmentedDickeyFuller(x);
        double pValue = adf.getPValue();
        assertTrue(pValue >= 0.01 && pValue <= 0.10, "p-value must be in [0.01, 0.10]");
    }

    @Test
    public void testGetPValueStationarySeries() {
        // Stationary: white noise should have a very negative ADF stat → p-value at minimum (0.01)
        ArrayList<Double> x = new ArrayList<>();
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < 200; i++) {
            x.add(rng.nextGaussian());
        }
        AugmentedDickeyFuller adf = new AugmentedDickeyFuller(x);
        // White noise is stationary; ADF stat should be very negative
        assertTrue(adf.getAdfStat() < -3.0, "Stationary series should have ADF stat < -3.0");
        assertEquals(0.01, adf.getPValue(), 1e-9, "p-value should be at lower boundary (0.01) for stationary white noise");
    }
}
