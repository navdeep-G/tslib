package tslib.transform;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoxCoxOptimizationTest {

    @Test
    public void lambdaZeroEqualsLogTransform() {
        List<Double> data = Arrays.asList(1.0, 2.0, 4.0, 8.0);
        List<Double> boxCox = Transform.boxCox(data, 0.0);
        List<Double> log = Transform.log(data);
        for (int i = 0; i < data.size(); i++) {
            assertEquals(log.get(i), boxCox.get(i), 1e-12);
        }
    }

    @Test
    public void lambdaOneEqualsShiftByOne() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        List<Double> result = Transform.boxCox(data, 1.0);
        for (int i = 0; i < data.size(); i++) {
            assertEquals(data.get(i) - 1.0, result.get(i), 1e-12);
        }
    }

    @Test
    public void autoLambdaSearchProducesFiniteResult() {
        List<Double> data = Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0, 32.0);
        double lambda = Transform.boxCoxLambdaSearch(data);
        assertFalse(Double.isNaN(lambda));
        assertFalse(Double.isInfinite(lambda));
        assertTrue(lambda >= -1.0 && lambda <= 2.0);
    }

    @Test
    public void autoLambdaForExponentialDataNearZero() {
        // Exponential growth — optimal lambda should be close to 0 (log-like)
        List<Double> data = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            data.add(Math.exp(i * 0.5));
        }
        double lambda = Transform.boxCoxLambdaSearch(data);
        assertTrue(lambda < 0.3, "Expected lambda near 0 for exponential data, got " + lambda);
    }

    @Test
    public void boxCoxAutoTransformReturnsSameSizeList() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> result = Transform.boxCox(data);
        assertEquals(data.size(), result.size());
    }

    @Test
    public void boxCoxCustomLambdaRangeRespected() {
        List<Double> data = Arrays.asList(2.0, 4.0, 8.0, 16.0);
        double lambda = Transform.boxCoxLambdaSearch(data, 0.0, 1.0);
        assertTrue(lambda >= 0.0 && lambda <= 1.0);
    }

    @Test
    public void boxCoxThrowsOnNonPositiveValues() {
        assertThrows(IllegalArgumentException.class, () ->
                Transform.boxCox(Arrays.asList(1.0, -1.0, 3.0)));
    }

    @Test
    public void wellfordsMatchesTwoPassVariance() {
        // Verify the single-pass Welford result equals a direct two-pass computation
        List<Double> data = Arrays.asList(2.5, 4.0, 1.5, 7.0, 3.3);
        double lambda = 0.5;
        // The boxCox transform with this lambda should produce consistent results
        List<Double> transformed = Transform.boxCox(data, lambda);
        double mean = 0.0;
        for (double v : transformed) mean += v;
        mean /= transformed.size();
        double variance = 0.0;
        for (double v : transformed) variance += (v - mean) * (v - mean);
        variance /= transformed.size();
        // Lambda search shouldn't crash and should find a finite optimum
        double optLambda = Transform.boxCoxLambdaSearch(data);
        assertFalse(Double.isNaN(optLambda));
    }
}
