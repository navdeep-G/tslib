package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.model.expsmoothing.DoubleExpSmoothing;
import tslib.model.expsmoothing.SingleExpSmoothing;
import tslib.model.expsmoothing.TripleExpSmoothing;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ets")
@Tag(name = "Exponential Smoothing", description = "Single, Double, Triple ETS models")
public class EtsController {

    @Operation(summary = "Single exponential smoothing forecast")
    @PostMapping("/single/forecast")
    public IntervalForecastResponse singleForecast(@Valid @RequestBody SingleEtsRequest req) {
        var model = new SingleExpSmoothing(req.getAlpha());
        model.fit(req.getData());
        return IntervalForecastResponse.from(
                model.forecastWithIntervals(req.getSteps(), req.getConfidenceLevel()));
    }

    @Operation(summary = "Double exponential smoothing (Holt linear) forecast")
    @PostMapping("/double/forecast")
    public IntervalForecastResponse doubleForecast(@Valid @RequestBody DoubleEtsRequest req) {
        var model = new DoubleExpSmoothing(req.getAlpha(), req.getGamma(), req.getInitializationMethod());
        model.fit(req.getData());
        return IntervalForecastResponse.from(
                model.forecastWithIntervals(req.getSteps(), req.getConfidenceLevel()));
    }

    @Operation(summary = "Triple exponential smoothing (Holt-Winters) forecast")
    @PostMapping("/triple/forecast")
    public IntervalForecastResponse tripleForecast(@Valid @RequestBody TripleEtsRequest req) {
        var model = new TripleExpSmoothing(
                req.getAlpha(), req.getBeta(), req.getGamma(), req.getPeriod());
        model.fit(req.getData());
        return IntervalForecastResponse.from(
                model.forecastWithIntervals(req.getSteps(), req.getConfidenceLevel()));
    }
}
