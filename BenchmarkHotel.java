import java.nio.file.*;
import java.util.*;
import tslib.evaluation.*;
import tslib.model.*;

public class BenchmarkHotel {
  public static void main(String[] args) throws Exception {
    List<String> lines = Files.readAllLines(Path.of("data/hotel.txt"));
    List<Double> data = new ArrayList<>();
    for (String s : lines) {
      s=s.trim(); if (!s.isEmpty()) data.add(Double.parseDouble(s));
    }
    Map<String, ForecastFunction> models = new LinkedHashMap<>();
    models.put("ARIMA(0,1,0)", (train, horizon) -> new ARIMA(0,1,0).forecast(train, horizon));
    models.put("SARIMA(0,1,0)x(1,1,0,12)", (train, horizon) -> new SARIMA(0,1,0,1,1,0,12).forecast(train, horizon));
    models.put("LocalLevel", (train, horizon) -> new LocalLevelModel().fit(train).forecast(horizon));
    models.put("TripleExpSmoothing", (train, horizon) -> new TripleExpSmoothing(0.4,0.3,0.2,12,false).forecast(train,horizon));
    List<BenchmarkSummary> rows = new ModelBenchmark(60, 1).compare(data, models);
    System.out.println(BenchmarkMarkdown.toMarkdown(rows));
  }
}
