package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.collect.Collect;

@RestController
@RequestMapping("/api/analyze")
@Tag(name = "Analyze", description = "Summary statistics, ACF/PACF, and exploratory analysis")
public class AnalyzeController {

    @Operation(summary = "Analyze a time series — stats, ACF/PACF, stationarity, transforms")
    @PostMapping
    public AnalyzeResponse analyze(@Valid @RequestBody AnalyzeRequest req) {
        var c = new Collect(req.getData(), req.getK(), req.getN());

        var resp = new AnalyzeResponse();
        resp.setAverage(c.getAverage());
        resp.setVariance(c.getVariance());
        resp.setStandardDeviation(c.getStandardDeviation());
        resp.setMin(c.getMin());
        resp.setMax(c.getMax());
        resp.setMinIndex(c.getMinIndex());
        resp.setMaxIndex(c.getMaxIndex());
        resp.setAutocovariance(c.getAutocovariance());
        resp.setAutocorrelation(c.getAutocorrelation());
        resp.setAcf(c.acf(req.getN()));
        resp.setPacf(c.pacf());
        resp.setAdfStatistic(c.getADFStat());
        resp.setStationary(c.isStationary());
        resp.setLogTransformed(c.getLogTransformed());
        resp.setFirstDifference(c.getFirstDifference());
        resp.setRollingAverage(c.getRollingAverage(req.getWindowSize()));
        return resp;
    }
}
