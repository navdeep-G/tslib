package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.diagnostics.LjungBoxTest;

@RestController
@RequestMapping("/api/diagnostics")
@Tag(name = "Diagnostics", description = "Residual diagnostics — Ljung-Box test for autocorrelation")
public class DiagnosticsController {

    @Operation(summary = "Ljung-Box test — check residuals for autocorrelation (null: no autocorrelation)")
    @PostMapping("/ljung-box")
    public LjungBoxResponse ljungBox(@Valid @RequestBody LjungBoxRequest req) {
        LjungBoxTest test = req.getDegreesOfFreedomAdjustment() > 0
                ? new LjungBoxTest(req.getResiduals(), req.getLags(), req.getDegreesOfFreedomAdjustment())
                : new LjungBoxTest(req.getResiduals(), req.getLags());

        var resp = new LjungBoxResponse();
        resp.setStatistic(test.getStatistic());
        resp.setPValue(test.getPValue());
        resp.setLags(req.getLags());
        resp.setRejectsAtFivePercent(test.rejectsAtFivePercent());
        return resp;
    }
}
