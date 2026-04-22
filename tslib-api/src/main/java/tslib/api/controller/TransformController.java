package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.transform.Differencing;
import tslib.transform.Transform;

@RestController
@RequestMapping("/api/transform")
@Tag(name = "Transforms", description = "Log, Box-Cox, differencing, and inverse transforms")
public class TransformController {

    @Operation(summary = "Log transform")
    @PostMapping("/log")
    public ForecastResponse log(@Valid @RequestBody TransformRequest req) {
        return new ForecastResponse(Transform.log(req.getData()));
    }

    @Operation(summary = "Square root transform")
    @PostMapping("/sqrt")
    public ForecastResponse sqrt(@Valid @RequestBody TransformRequest req) {
        return new ForecastResponse(Transform.sqrt(req.getData()));
    }

    @Operation(summary = "Cube root transform")
    @PostMapping("/cbrt")
    public ForecastResponse cbrt(@Valid @RequestBody TransformRequest req) {
        return new ForecastResponse(Transform.cbrt(req.getData()));
    }

    @Operation(summary = "Arbitrary root transform (requires r — the root degree, e.g. 4 for fourth root)")
    @PostMapping("/root")
    public ForecastResponse root(@Valid @RequestBody TransformRequest req) {
        if (req.getR() == null) throw new IllegalArgumentException("r (root degree) is required");
        return new ForecastResponse(Transform.root(req.getData(), req.getR()));
    }

    @Operation(summary = "Box-Cox transform (provide lambda, or omit for optimal lambda search)")
    @PostMapping("/boxcox")
    public TransformResponse boxCox(@Valid @RequestBody TransformRequest req) {
        var resp = new TransformResponse();
        if (req.getLambda() != null) {
            resp.setResult(Transform.boxCox(req.getData(), req.getLambda()));
            resp.setLambda(req.getLambda());
        } else {
            double lambda = req.getLowerBound() != null && req.getUpperBound() != null
                    ? Transform.boxCoxLambdaSearch(req.getData(), req.getLowerBound(), req.getUpperBound())
                    : Transform.boxCoxLambdaSearch(req.getData());
            resp.setResult(Transform.boxCox(req.getData(), lambda));
            resp.setLambda(lambda);
        }
        return resp;
    }

    @Operation(summary = "Inverse Box-Cox transform (requires lambda)")
    @PostMapping("/boxcox/inverse")
    public ForecastResponse inverseBoxCox(@Valid @RequestBody TransformRequest req) {
        if (req.getLambda() == null) throw new IllegalArgumentException("lambda is required");
        return new ForecastResponse(Transform.inverseBoxCox(req.getData(), req.getLambda()));
    }

    @Operation(summary = "Regular differencing")
    @PostMapping("/difference")
    public ForecastResponse difference(@Valid @RequestBody DifferenceRequest req) {
        return new ForecastResponse(Differencing.difference(req.getData(), req.getOrder()));
    }

    @Operation(summary = "Seasonal differencing")
    @PostMapping("/seasonal-difference")
    public ForecastResponse seasonalDifference(@Valid @RequestBody DifferenceRequest req) {
        if (req.getLag() == null) throw new IllegalArgumentException("lag is required");
        return new ForecastResponse(
                Differencing.seasonalDifference(req.getData(), req.getLag(), req.getOrder()));
    }

    @Operation(summary = "Inverse differencing (reconstruct original scale from differenced series)")
    @PostMapping("/difference/inverse")
    public ForecastResponse inverseDifference(@Valid @RequestBody DifferenceRequest req) {
        if (req.getHistory() == null) throw new IllegalArgumentException("history is required");
        return new ForecastResponse(
                Differencing.inverseDifference(req.getData(), req.getHistory(), req.getOrder()));
    }
}
