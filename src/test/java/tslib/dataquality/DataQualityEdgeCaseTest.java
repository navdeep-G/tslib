package tslib.dataquality;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class DataQualityEdgeCaseTest {

    @Test
    public void forwardAndBackwardFillHandleBoundaryNulls() {
        List<Double> values = Arrays.asList(null, null, 3.0, null, 5.0, null);

        assertEquals(List.of(0.0, 0.0, 3.0, 3.0, 5.0, 5.0), MissingValueImputer.forwardFill(values));
        assertEquals(List.of(3.0, 3.0, 3.0, 5.0, 5.0, 0.0), MissingValueImputer.backwardFill(values));
    }

    @Test
    public void meanFillTreatsNaNsAsMissing() {
        List<Double> result = MissingValueImputer.meanFill(Arrays.asList(1.0, Double.NaN, 3.0, null));
        assertEquals(1.0, result.get(0), 1e-9);
        assertEquals(2.0, result.get(1), 1e-9);
        assertEquals(3.0, result.get(2), 1e-9);
        assertEquals(2.0, result.get(3), 1e-9);
    }
}
