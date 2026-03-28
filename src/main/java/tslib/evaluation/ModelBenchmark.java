package tslib.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small helper for comparing several models on the same rolling-origin protocol.
 */
public class ModelBenchmark {

    private final RollingOriginBacktest backtest;

    public ModelBenchmark(int minTrainSize, int horizon) {
        this(new RollingOriginBacktest(minTrainSize, horizon));
    }

    public ModelBenchmark(RollingOriginBacktest backtest) {
        this.backtest = backtest;
    }

    public List<BenchmarkSummary> compare(List<Double> data, Map<String, ForecastFunction> forecasters) {
        if (forecasters == null || forecasters.isEmpty()) {
            throw new IllegalArgumentException("At least one forecaster must be provided");
        }
        List<BenchmarkSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, ForecastFunction> entry : forecasters.entrySet()) {
            BacktestResult result = backtest.run(data, entry.getValue());
            summaries.add(new BenchmarkSummary(entry.getKey(), result));
        }
        Collections.sort(summaries);
        return summaries;
    }

    public Map<String, BacktestResult> compareDetailed(List<Double> data, Map<String, ForecastFunction> forecasters) {
        if (forecasters == null || forecasters.isEmpty()) {
            throw new IllegalArgumentException("At least one forecaster must be provided");
        }
        Map<String, BacktestResult> results = new LinkedHashMap<>();
        for (Map.Entry<String, ForecastFunction> entry : forecasters.entrySet()) {
            results.put(entry.getKey(), backtest.run(data, entry.getValue()));
        }
        return results;
    }
}
