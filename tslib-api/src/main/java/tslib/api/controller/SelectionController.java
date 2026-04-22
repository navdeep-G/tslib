package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.model.arima.ArimaOrderSearch;
import tslib.selection.AutoArima;
import tslib.selection.AutoETS;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auto")
@Tag(name = "Auto-Selection", description = "Automatic model selection — AutoARIMA and AutoETS")
public class SelectionController {

    @Operation(summary = "AutoARIMA — automatically select and fit the best ARIMA/SARIMA order, then forecast")
    @PostMapping("/arima/forecast")
    public AutoArimaResponse autoArima(@Valid @RequestBody AutoArimaRequest req) {
        var criterion = ArimaOrderSearch.Criterion.valueOf(req.getCriterion().toUpperCase());

        AutoArima model;
        if (req.getSeasonalPeriod() != null) {
            model = new AutoArima(
                    req.getMaxP(), req.getMaxD(), req.getMaxQ(),
                    req.getMaxSeasonalP() != null ? req.getMaxSeasonalP() : 1,
                    req.getMaxSeasonalD() != null ? req.getMaxSeasonalD() : 1,
                    req.getMaxSeasonalQ() != null ? req.getMaxSeasonalQ() : 1,
                    req.getSeasonalPeriod(), criterion);
        } else {
            model = new AutoArima(req.getMaxP(), req.getMaxD(), req.getMaxQ(), criterion);
        }
        model.fit(req.getData());

        var ivf = model.forecastWithIntervals(req.getSteps(), req.getConfidenceLevel());
        var resp = new AutoArimaResponse();
        resp.setForecasts(ivf.getForecasts());
        resp.setIntervals(ivf.getIntervals().stream()
                .map(PredictionIntervalDto::from).collect(Collectors.toList()));
        resp.setBestOrder(OrderSearchResponse.from(model.getBestOrder()));
        resp.setSeasonal(model.isSeasonalModel());
        return resp;
    }

    @Operation(summary = "AutoETS — automatically select and fit the best ETS model, then forecast")
    @PostMapping("/ets/forecast")
    public AutoEtsResponse autoEts(@Valid @RequestBody AutoEtsRequest req) {
        AutoETS model = req.getSeasonalPeriod() != null
                ? new AutoETS(req.getSeasonalPeriod())
                : new AutoETS();
        model.fit(req.getData());

        var ivf = model.forecastWithIntervals(req.getSteps(), req.getConfidenceLevel());
        var resp = new AutoEtsResponse();
        resp.setForecasts(ivf.getForecasts());
        resp.setIntervals(ivf.getIntervals().stream()
                .map(PredictionIntervalDto::from).collect(Collectors.toList()));
        resp.setBestType(model.getBestType().name());
        resp.setBestParameters(model.getBestParameters());
        resp.setBestScore(model.getBestScore());
        return resp;
    }
}
