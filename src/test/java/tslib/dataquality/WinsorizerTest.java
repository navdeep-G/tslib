package tslib.dataquality;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WinsorizerTest {

    @Test
    public void winsorizeClipsOutliers() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 100.0);
        List<Double> result = Winsorizer.winsorize(data, 0.0, 0.75);
        assertTrue(result.get(4) < 100.0);
    }

    @Test
    public void winsorizePreservesInliers() {
        List<Double> data = Arrays.asList(2.0, 3.0, 4.0, 5.0, 6.0);
        List<Double> result = Winsorizer.winsorize(data, 0.1, 0.9);
        // Middle values (well inside bounds) are unchanged
        assertEquals(4.0, result.get(2), 1e-9);
        // Boundary values are clipped up/down toward interior quantiles
        assertTrue(result.get(0) >= 2.0);
        assertTrue(result.get(4) <= 6.0);
    }

    @Test
    public void winsorizeSymmetric() {
        List<Double> data = Arrays.asList(-10.0, 1.0, 2.0, 3.0, 10.0);
        List<Double> result = Winsorizer.winsorize(data, 0.1, 0.9);
        assertTrue(result.get(0) > -10.0);
        assertTrue(result.get(4) < 10.0);
    }

    @Test
    public void winsorizeNoChangeWhenNoOutliers() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> result = Winsorizer.winsorize(data, 0.0, 1.0);
        for (int i = 0; i < data.size(); i++) {
            assertEquals(data.get(i), result.get(i), 1e-10);
        }
    }

    @Test
    public void winsorizeReturnsSameSizeList() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 100.0, -100.0);
        List<Double> result = Winsorizer.winsorize(data, 0.1, 0.9);
        assertEquals(data.size(), result.size());
    }

    @Test
    public void winsorizeThrowsOnInvalidProbabilities() {
        assertThrows(IllegalArgumentException.class, () -> Winsorizer.winsorize(Arrays.asList(1.0, 2.0, 3.0), 0.8, 0.2));
    }

    @Test
    public void winsorizeThrowsWhenLowerEqualsUpper() {
        assertThrows(IllegalArgumentException.class, () -> Winsorizer.winsorize(Arrays.asList(1.0, 2.0, 3.0), 0.5, 0.5));
    }

    @Test
    public void winsorizeThrowsOnNegativeLower() {
        assertThrows(IllegalArgumentException.class, () -> Winsorizer.winsorize(Arrays.asList(1.0, 2.0, 3.0), -0.1, 0.9));
    }

    @Test
    public void winsorizeAllSameValues() {
        List<Double> data = Arrays.asList(5.0, 5.0, 5.0, 5.0);
        List<Double> result = Winsorizer.winsorize(data, 0.1, 0.9);
        for (double v : result) {
            assertEquals(5.0, v, 1e-10);
        }
    }
}
