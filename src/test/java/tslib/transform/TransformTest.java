package tslib.transform;

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
}
