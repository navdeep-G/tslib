package tslib.evaluation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelBenchmarkTest {
    @Test
    public void benchmarkSortsRowsByRmse() {
        List<Double> data = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
        Map<String, ForecastFunction> models = new LinkedHashMap<>();
        models.put("naive", (train, h) -> java.util.Collections.nCopies(h, train.get(train.size() - 1)));
        models.put("oracle-ish", (train, h) -> {
            double last = train.get(train.size() - 1);
            java.util.List<Double> out = new java.util.ArrayList<>();
            for (int i = 1; i <= h; i++) {
                out.add(last + i);
            }
            return out;
        });

        List<BenchmarkSummary> rows = new ModelBenchmark(4, 1).compare(data, models);
        assertEquals(2, rows.size());
        assertEquals("oracle-ish", rows.get(0).getModelName());
        assertTrue(rows.get(0).getRmse() <= rows.get(1).getRmse());
    }
}
