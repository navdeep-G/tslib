package tslib.dataquality;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DataQualityTest {
    @Test
    public void imputesMissingValuesAndWinsorizesOutliers() {
        List<Double> imputed = MissingValueImputer.linearInterpolation(Arrays.asList(1.0, null, null, 4.0));
        assertEquals(2.0, imputed.get(1), 1e-9);
        assertEquals(3.0, imputed.get(2), 1e-9);

        List<Double> winsorized = Winsorizer.winsorize(List.of(1.0, 2.0, 3.0, 100.0), 0.0, 0.75);
        assertTrue(winsorized.get(3) < 100.0);
    }

    @Test
    public void detectsOutlierIndices() {
        List<Integer> outliers = OutlierDetector.zScore(List.of(10.0, 11.0, 10.5, 100.0), 1.5);
        assertEquals(List.of(3), outliers);
    }
}
