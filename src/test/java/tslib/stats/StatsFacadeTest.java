package tslib.stats;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StatsFacadeTest {
    @Test
    public void facadeDelegatesToUtilityImplementation() {
        List<Double> values = List.of(1.0, 2.0, 3.0, 4.0);

        assertEquals(2.5, Stats.average(values), 1e-9);
        assertEquals(tslib.util.Stats.variance(values), Stats.variance(values), 1e-9);
        assertArrayEquals(tslib.util.Stats.getAcf(values, 2), Stats.getAcf(values, 2), 1e-9);
    }
}
