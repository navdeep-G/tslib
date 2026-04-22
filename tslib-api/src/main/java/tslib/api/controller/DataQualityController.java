package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.dataquality.MissingValueImputer;
import tslib.dataquality.OutlierDetector;
import tslib.dataquality.Winsorizer;

import java.util.List;

@RestController
@RequestMapping("/api/dataquality")
@Tag(name = "Data Quality", description = "Missing value imputation and outlier detection")
public class DataQualityController {

    @Operation(summary = "Impute missing values (null → Double.NaN in the list)")
    @PostMapping("/impute")
    public ForecastResponse impute(@Valid @RequestBody ImputeRequest req) {
        var strategy = MissingValueImputer.Strategy.valueOf(req.getStrategy().toUpperCase());
        return new ForecastResponse(MissingValueImputer.impute(req.getData(), strategy));
    }

    @Operation(summary = "Detect outlier indices using Z-score or IQR method")
    @PostMapping("/outliers")
    public OutlierResponse outliers(@Valid @RequestBody OutlierRequest req) {
        List<Integer> indices = switch (req.getMethod().toUpperCase()) {
            case "Z_SCORE" -> OutlierDetector.zScore(req.getData(), req.getThreshold());
            case "IQR"     -> OutlierDetector.iqr(req.getData(), req.getThreshold());
            default -> throw new IllegalArgumentException(
                    "Unknown method: " + req.getMethod() + ". Valid: Z_SCORE, IQR");
        };
        return new OutlierResponse(indices);
    }

    @Operation(summary = "Winsorize — clip extreme values to specified quantile bounds")
    @PostMapping("/winsorize")
    public ForecastResponse winsorize(@Valid @RequestBody WinsorizerRequest req) {
        return new ForecastResponse(
                Winsorizer.winsorize(req.getData(), req.getLowerProbability(), req.getUpperProbability()));
    }

    public record OutlierResponse(List<Integer> outlierIndices) {}
}
