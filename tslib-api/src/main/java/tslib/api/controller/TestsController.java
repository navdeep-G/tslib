package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.tests.AugmentedDickeyFuller;
import tslib.tests.KPSSTest;

@RestController
@RequestMapping("/api/tests")
@Tag(name = "Stationarity Tests", description = "Augmented Dickey-Fuller and KPSS tests")
public class TestsController {

    @Operation(summary = "Augmented Dickey-Fuller test for unit root (non-stationarity)")
    @PostMapping("/adf")
    public AdfResponse adf(@Valid @RequestBody AdfRequest req) {
        AugmentedDickeyFuller adf = req.getLag() != null
                ? new AugmentedDickeyFuller(req.getData(), req.getLag())
                : new AugmentedDickeyFuller(req.getData());

        var resp = new AdfResponse();
        resp.setStatistic(adf.getAdfStat());
        resp.setPValue(adf.getPValue());
        resp.setLag(adf.getLag());
        resp.setStationary(adf.isStationary());
        resp.setNeedsDiff(adf.isNeedsDiff());
        return resp;
    }

    @Operation(summary = "KPSS test for stationarity")
    @PostMapping("/kpss")
    public KpssResponse kpss(@Valid @RequestBody KpssRequest req) {
        var type = KPSSTest.RegressionType.valueOf(req.getRegressionType().toUpperCase());
        KPSSTest kpss = req.getLags() != null
                ? new KPSSTest(req.getData(), type, req.getLags())
                : new KPSSTest(req.getData(), type);

        var resp = new KpssResponse();
        resp.setStatistic(kpss.getStatistic());
        resp.setLags(kpss.getLags());
        resp.setRegressionType(kpss.getRegressionType().name());
        resp.setStationaryAtFivePercent(kpss.isStationaryAtFivePercent());
        resp.setStationaryAtOnePercent(kpss.isStationaryAtOnePercent());
        resp.setCriticalValueFivePercent(kpss.getCriticalValue(0.05));
        resp.setCriticalValueOnePercent(kpss.getCriticalValue(0.01));
        return resp;
    }
}
