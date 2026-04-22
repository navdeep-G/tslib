package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.decomposition.STLDecomposition;

@RestController
@RequestMapping("/api/decompose")
@Tag(name = "Decomposition", description = "STL seasonal-trend decomposition")
public class DecompositionController {

    @Operation(summary = "STL decomposition — decompose into trend, seasonal, and remainder components")
    @PostMapping("/stl")
    public StlResponse stl(@Valid @RequestBody StlRequest req) {
        STLDecomposition stl;
        if (req.getTrendWindow() != null && req.getSeasonalWindow() != null
                && req.getIterations() != null && req.getOuterIterations() != null) {
            stl = new STLDecomposition(req.getPeriod(), req.getTrendWindow(),
                    req.getSeasonalWindow(), req.getIterations(), req.getOuterIterations());
        } else if (req.getTrendWindow() != null && req.getSeasonalWindow() != null
                && req.getIterations() != null) {
            stl = new STLDecomposition(req.getPeriod(), req.getTrendWindow(),
                    req.getSeasonalWindow(), req.getIterations());
        } else {
            stl = new STLDecomposition(req.getPeriod());
        }

        var result = stl.decompose(req.getData());
        var resp = new StlResponse();
        resp.setTrend(result.getTrend());
        resp.setSeasonal(result.getSeasonal());
        resp.setRemainder(result.getRemainder());
        resp.setReconstructed(result.reconstruct());
        return resp;
    }
}
