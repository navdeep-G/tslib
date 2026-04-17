package tslib.transform;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class TransformTest {
    @Test
    public void boxCoxWithZeroLambdaMatchesLogTransform() {
        List<Double> values = List.of(1.0, 2.0, 4.0, 8.0);
        List<Double> logged = Transform.log(values);
        List<Double> boxed = Transform.boxCox(values, 0.0);

        for (int i = 0; i < values.size(); i++) {
            assertEquals(logged.get(i), boxed.get(i), 1e-9);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void logRejectsNonPositiveValues() {
        Transform.log(List.of(1.0, 0.0, 2.0));
    }

    @Test
    public void inverseBoxCoxRoundTripLambdaZero() {
        List<Double> original = Arrays.asList(1.0, 2.0, 4.0, 8.0);
        List<Double> transformed = Transform.boxCox(original, 0.0);
        List<Double> restored = Transform.inverseBoxCox(transformed, 0.0);
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), restored.get(i), 1e-9);
        }
    }

    @Test
    public void inverseBoxCoxRoundTripLambdaHalf() {
        List<Double> original = Arrays.asList(1.0, 4.0, 9.0, 16.0);
        double lambda = 0.5;
        List<Double> transformed = Transform.boxCox(original, lambda);
        List<Double> restored = Transform.inverseBoxCox(transformed, lambda);
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), restored.get(i), 1e-9);
        }
    }

    @Test
    public void inverseBoxCoxRoundTripLambdaOne() {
        List<Double> original = Arrays.asList(2.0, 5.0, 10.0);
        double lambda = 1.0;
        List<Double> transformed = Transform.boxCox(original, lambda);
        List<Double> restored = Transform.inverseBoxCox(transformed, lambda);
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), restored.get(i), 1e-9);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void inverseBoxCoxThrowsWhenInnerNonPositive() {
        // lambda=2, y=-1: y*lambda+1 = -1*2+1 = -1 <= 0
        Transform.inverseBoxCox(Arrays.asList(-1.0), 2.0);
    }
}
