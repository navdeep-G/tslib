package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.movingaverage.CumulativeMovingAverage;
import tslib.movingaverage.ExponentialMovingAverage;
import tslib.movingaverage.SimpleMovingAverage;
import tslib.movingaverage.WeightedMovingAverage;

@RestController
@RequestMapping("/api/moving-average")
@Tag(name = "Moving Averages", description = "Simple, Exponential, Weighted, and Cumulative moving averages")
public class MovingAverageController {

    @Operation(summary = "Compute moving average — type: SIMPLE (default), EMA, WEIGHTED, CUMULATIVE")
    @PostMapping
    public ForecastResponse movingAverage(@Valid @RequestBody MovingAverageRequest req) {
        return switch (req.getType().toUpperCase()) {
            case "SIMPLE"     -> new ForecastResponse(
                    new SimpleMovingAverage(req.getPeriod()).compute(req.getData()));
            case "EMA" -> {
                double alpha = req.getAlpha() != null ? req.getAlpha()
                        : 2.0 / (req.getPeriod() + 1);
                yield new ForecastResponse(
                        new ExponentialMovingAverage(alpha).compute(req.getData()));
            }
            case "WEIGHTED"   -> new ForecastResponse(
                    new WeightedMovingAverage(req.getPeriod()).compute(req.getData()));
            case "CUMULATIVE" -> new ForecastResponse(
                    new CumulativeMovingAverage().compute(req.getData()));
            default -> throw new IllegalArgumentException(
                    "Unknown type: " + req.getType() + ". Valid: SIMPLE, EMA, WEIGHTED, CUMULATIVE");
        };
    }
}
