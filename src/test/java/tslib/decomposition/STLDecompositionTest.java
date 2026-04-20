package tslib.decomposition;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class STLDecompositionTest {

    @Test
    public void decompositionReturnsTrendSeasonalAndRemainder() {
        List<Double> data = new ArrayList<>();
        double[] seasonalPattern = {2.0, -1.0, 3.0, -2.0};
        for (int i = 0; i < 20; i++) {
            data.add(10.0 + i * 0.5 + seasonalPattern[i % seasonalPattern.length]);
        }

        STLDecomposition decomposition = new STLDecomposition(4);
        STLDecomposition.Result result = decomposition.decompose(data);

        assertEquals(data.size(), result.getTrend().size());
        assertEquals(data.size(), result.getSeasonal().size());
        assertEquals(data.size(), result.getRemainder().size());
        assertTrue(result.getTrend().get(result.getTrend().size() - 1) > result.getTrend().get(0));

        double seasonalAtTwo = averageModulo(result.getSeasonal(), 4, 2);
        double seasonalAtThree = averageModulo(result.getSeasonal(), 4, 3);
        assertTrue(seasonalAtTwo > seasonalAtThree);
    }

    private double averageModulo(List<Double> values, int period, int offset) {
        double total = 0.0;
        int count = 0;
        for (int i = offset; i < values.size(); i += period) {
            total += values.get(i);
            count++;
        }
        return total / count;
    }
}
