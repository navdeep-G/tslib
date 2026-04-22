package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.evaluation.*;
import tslib.model.arima.ARIMA;
import tslib.model.arima.SARIMA;
import tslib.model.expsmoothing.DoubleExpSmoothing;
import tslib.model.expsmoothing.SingleExpSmoothing;
import tslib.model.expsmoothing.TripleExpSmoothing;
import tslib.model.statespace.LocalLevelModel;
import tslib.selection.AutoArima;
import tslib.selection.AutoETS;
import tslib.model.arima.ArimaOrderSearch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluate")
@Tag(name = "Evaluation", description = "Forecast metrics, backtesting, and train/test splitting")
public class EvaluationController {

    @Operation(summary = "Compute forecast accuracy metrics (MAE, RMSE, MAPE, SMAPE, MASE, MeanError)")
    @PostMapping("/metrics")
    public MetricsResponse metrics(@Valid @RequestBody MetricsRequest req) {
        var resp = new MetricsResponse();
        resp.setMae(ForecastMetrics.mae(req.getActual(), req.getForecast()));
        resp.setRmse(ForecastMetrics.rmse(req.getActual(), req.getForecast()));
        resp.setMape(ForecastMetrics.mape(req.getActual(), req.getForecast()));
        resp.setSmape(ForecastMetrics.smape(req.getActual(), req.getForecast()));
        resp.setMeanError(ForecastMetrics.meanError(req.getActual(), req.getForecast()));
        if (req.getTrainingSeries() != null && !req.getTrainingSeries().isEmpty()) {
            resp.setMase(ForecastMetrics.mase(req.getActual(), req.getForecast(),
                    req.getTrainingSeries(), req.getSeasonalPeriod()));
        }
        return resp;
    }

    @Operation(summary = "Rolling-origin backtest — evaluate a model over multiple forecast origins")
    @PostMapping("/backtest")
    public BacktestResponse backtest(@Valid @RequestBody BacktestRequest req) {
        var backtest = new RollingOriginBacktest(
                req.getMinTrainSize(), req.getHorizon(),
                req.getStepSize(), req.getSeasonalPeriod());
        ForecastFunction fn = buildForecastFunction(req.getModelSpec());
        return BacktestResponse.from(backtest.run(req.getData(), fn));
    }

    @Operation(summary = "Split a series into train and test sets")
    @PostMapping("/train-test-split")
    public TrainTestSplitResponse trainTestSplit(@Valid @RequestBody TrainTestSplitRequest req) {
        TrainTestSplit split;
        if (req.getTrainSize() != null) {
            split = TrainTestSplit.atIndex(req.getData(), req.getTrainSize());
        } else if (req.getTrainRatio() != null) {
            split = TrainTestSplit.ratio(req.getData(), req.getTrainRatio());
        } else {
            throw new IllegalArgumentException("Provide either trainSize or trainRatio");
        }
        var resp = new TrainTestSplitResponse();
        resp.setTrain(split.getTrain());
        resp.setTest(split.getTest());
        resp.setTrainSize(split.getTrain().size());
        resp.setTestSize(split.getTest().size());
        return resp;
    }

    @Operation(summary = "Benchmark multiple models side-by-side via rolling-origin backtest")
    @PostMapping("/benchmark")
    public List<BenchmarkSummaryDto> benchmark(@Valid @RequestBody BenchmarkRequest req) {
        var backtest = new RollingOriginBacktest(
                req.getMinTrainSize(), req.getHorizon(),
                req.getStepSize(), req.getSeasonalPeriod());

        Map<String, ForecastFunction> forecasters = new LinkedHashMap<>();
        for (var nm : req.getModels()) {
            forecasters.put(nm.getName(), buildForecastFunction(nm.getSpec()));
        }

        return new ModelBenchmark(backtest).compare(req.getData(), forecasters).stream()
                .map(s -> new BenchmarkSummaryDto(
                        s.getModelName(), s.getMae(), s.getRmse(),
                        s.getMape(), s.getSmape(), s.getMase()))
                .collect(Collectors.toList());
    }

    public record BenchmarkSummaryDto(
            String modelName, double mae, double rmse,
            double mape, double smape, double mase) {}

    private ForecastFunction buildForecastFunction(ModelSpec spec) {
        return switch (spec.getType().toUpperCase()) {
            case "ARIMA" -> (data, horizon) -> {
                var m = new ARIMA(
                        nvl(spec.getP(), 1), nvl(spec.getD(), 1), nvl(spec.getQ(), 1));
                m.fit(data);
                return m.forecast(horizon);
            };
            case "SARIMA" -> (data, horizon) -> {
                var m = new SARIMA(
                        nvl(spec.getP(), 1), nvl(spec.getD(), 1), nvl(spec.getQ(), 1),
                        nvl(spec.getSeasonalP(), 1), nvl(spec.getSeasonalD(), 1),
                        nvl(spec.getSeasonalQ(), 1), nvl(spec.getSeasonalPeriod(), 12));
                m.fit(data);
                return m.forecast(horizon);
            };
            case "SINGLE_ETS" -> (data, horizon) -> {
                var m = new SingleExpSmoothing(nvld(spec.getAlpha(), 0.3));
                m.fit(data);
                return m.forecast(horizon);
            };
            case "DOUBLE_ETS" -> (data, horizon) -> {
                var m = new DoubleExpSmoothing(
                        nvld(spec.getAlpha(), 0.3), nvld(spec.getGamma(), 0.1),
                        nvl(spec.getInitializationMethod(), 0));
                m.fit(data);
                return m.forecast(horizon);
            };
            case "TRIPLE_ETS" -> (data, horizon) -> {
                var m = new TripleExpSmoothing(
                        nvld(spec.getAlpha(), 0.3), nvld(spec.getBeta(), 0.1),
                        nvld(spec.getGamma(), 0.1), nvl(spec.getPeriod(), 12));
                m.fit(data);
                return m.forecast(horizon);
            };
            case "AUTO_ARIMA" -> (data, horizon) -> {
                var criterion = ArimaOrderSearch.Criterion.valueOf(
                        spec.getCriterion() != null ? spec.getCriterion().toUpperCase() : "AIC");
                var m = new AutoArima(nvl(spec.getMaxP(), 3), nvl(spec.getMaxD(), 2),
                        nvl(spec.getMaxQ(), 3), criterion);
                m.fit(data);
                return m.forecast(horizon);
            };
            case "AUTO_ETS" -> (data, horizon) -> {
                var m = spec.getSeasonalPeriod() != null
                        ? new AutoETS(spec.getSeasonalPeriod()) : new AutoETS();
                m.fit(data);
                return m.forecast(horizon);
            };
            case "LOCAL_LEVEL" -> (data, horizon) -> {
                var m = new LocalLevelModel();
                m.fit(data);
                return m.forecast(horizon);
            };
            default -> throw new IllegalArgumentException(
                    "Unknown model type: " + spec.getType() +
                    ". Valid: ARIMA, SARIMA, SINGLE_ETS, DOUBLE_ETS, TRIPLE_ETS, AUTO_ARIMA, AUTO_ETS, LOCAL_LEVEL");
        };
    }

    private int nvl(Integer v, int def) { return v != null ? v : def; }
    private double nvld(Double v, double def) { return v != null ? v : def; }
}
