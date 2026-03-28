import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tslib.evaluation.BenchmarkSummary;
import tslib.evaluation.ModelBenchmark;
import tslib.evaluation.ForecastFunction;
import tslib.model.ARIMA;
import tslib.model.LocalLevelModel;
import tslib.model.TripleExpSmoothing;

public class BenchmarkComparisonExample {
    public static void main(String[] args) {
        List<Double> data = List.of(12.0, 14.0, 13.0, 16.0, 18.0, 17.0, 20.0, 22.0, 21.0, 24.0, 26.0, 25.0);

        Map<String, ForecastFunction> models = new LinkedHashMap<>();
        models.put("ARIMA(0,1,0)", (train, horizon) -> new ARIMA(0, 1, 0).forecast(train, horizon));
        models.put("LocalLevel", (train, horizon) -> new LocalLevelModel().fit(train).forecast(horizon));
        models.put("TripleExpSmoothing", (train, horizon) -> new TripleExpSmoothing(0.4, 0.3, 0.2, 3, false).forecast(train, horizon));

        List<BenchmarkSummary> rows = new ModelBenchmark(6, 1).compare(data, models);
        for (BenchmarkSummary row : rows) {
            System.out.printf("%s -> RMSE=%.4f, MAE=%.4f, sMAPE=%.2f%%%n",
                    row.getModelName(), row.getRmse(), row.getMae(), row.getSmape());
        }
    }
}
