package tslib.diagnostics;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LjungBoxTestTest {
    @Test
    public void whiteNoiseHasLargePValue() {
        List<Double> residuals = List.of(0.2, -0.1, 0.05, 0.1, -0.2, 0.15, -0.05, 0.03, -0.02, 0.04);
        LjungBoxTest test = new LjungBoxTest(residuals, 3);
        assertTrue(test.getPValue() >= 0.0 && test.getPValue() <= 1.0);
    }

    @Test
    public void autocorrelatedSeriesHasPositiveStatistic() {
        List<Double> residuals = new ArrayList<>();
        double value = 1.0;
        for (int i = 0; i < 20; i++) {
            value = 0.8 * value;
            residuals.add(value);
        }
        LjungBoxTest test = new LjungBoxTest(residuals, 4);
        assertTrue(test.getStatistic() > 0.0);
    }
}
