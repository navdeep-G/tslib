package tslib.evaluation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import tslib.dataquality.MissingValueImputer;
import tslib.model.ARIMA;
import tslib.model.LocalLevelModel;
import tslib.model.TripleExpSmoothing;
import static org.junit.Assert.*;

public class ReleaseCandidateWorkflowTest {

    @Test
    public void benchmarkWorkflowProducesSortedMarkdownReadyRows() {
        List<Double> raw = MissingValueImputer.linearInterpolation(List.of(12.0, null, 13.0, 16.0, 18.0, 17.0, 20.0, 22.0, 21.0, 24.0, 26.0, 25.0));

        Map<String, ForecastFunction> models = new LinkedHashMap<>();
        models.put("ARIMA(0,1,0)", (train, horizon) -> new ARIMA(0, 1, 0).forecast(train, horizon));
        models.put("LocalLevel", (train, horizon) -> new LocalLevelModel().fit(train).forecast(horizon));
        models.put("TripleExpSmoothing", (train, horizon) -> new TripleExpSmoothing(0.4, 0.3, 0.2, 3, false).forecast(train, horizon));

        List<BenchmarkSummary> rows = new ModelBenchmark(6, 1).compare(raw, models);
        assertEquals(3, rows.size());
        assertTrue(rows.get(0).getRmse() <= rows.get(1).getRmse());
        assertTrue(rows.get(1).getRmse() <= rows.get(2).getRmse());

        String markdown = BenchmarkMarkdown.toMarkdown(rows);
        assertTrue(markdown.contains("ARIMA(0,1,0)"));
        assertTrue(markdown.contains("TripleExpSmoothing"));
    }
}
