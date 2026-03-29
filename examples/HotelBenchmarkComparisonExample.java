import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tslib.evaluation.BenchmarkMarkdown;
import tslib.evaluation.BenchmarkSummary;
import tslib.evaluation.ForecastFunction;
import tslib.evaluation.ModelBenchmark;
import tslib.model.ARIMA;
import tslib.model.LocalLevelModel;
import tslib.model.SARIMA;
import tslib.model.TripleExpSmoothing;

public class HotelBenchmarkComparisonExample {
    public static void main(String[] args) throws Exception {
        List<String> lines = Files.readAllLines(Path.of("data/hotel.txt"));
        List<Double> data = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                data.add(Double.parseDouble(trimmed));
            }
        }

        Map<String, ForecastFunction> models = new LinkedHashMap<>();
        models.put("ARIMA(0,1,0)", (train, horizon) -> new ARIMA(0, 1, 0).forecast(train, horizon));
        models.put("SARIMA(0,1,0)x(1,1,0,12)",
                (train, horizon) -> new SARIMA(0, 1, 0, 1, 1, 0, 12).forecast(train, horizon));
        models.put("LocalLevel", (train, horizon) -> new LocalLevelModel().fit(train).forecast(horizon));
        models.put("TripleExpSmoothing",
                (train, horizon) -> new TripleExpSmoothing(0.4, 0.3, 0.2, 12, false).forecast(train, horizon));

        List<BenchmarkSummary> rows = new ModelBenchmark(60, 1).compare(data, models);
        System.out.println(BenchmarkMarkdown.toMarkdown(rows));
    }
}
