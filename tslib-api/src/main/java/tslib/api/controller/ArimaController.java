package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.model.arima.ARIMA;
import tslib.model.arima.ARIMAX;
import tslib.model.arima.ArimaOrderSearch;
import tslib.model.arima.SARIMA;
import tslib.model.arima.VARModel;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "ARIMA", description = "ARIMA, SARIMA, ARIMAX, VAR models and order search")
public class ArimaController {

    @Operation(summary = "Fit ARIMA and forecast")
    @PostMapping("/arima/forecast")
    public ArimaModelResponse arimaForecast(@Valid @RequestBody ArimaRequest req) {
        var model = new ARIMA(req.getP(), req.getD(), req.getQ(),
                req.getMaxIterations(), req.getTolerance());
        model.fit(req.getData());

        var resp = new ArimaModelResponse();
        resp.setForecasts(model.forecast(req.getSteps()));
        resp.setArCoefficients(model.getArCoefficients());
        resp.setMaCoefficients(model.getMaCoefficients());
        resp.setIntercept(model.getIntercept());
        resp.setInnovationVariance(model.getInnovationVariance());
        resp.setP(model.getP()); resp.setD(model.getD()); resp.setQ(model.getQ());
        return resp;
    }

    @Operation(summary = "Fit ARIMA and forecast with prediction intervals")
    @PostMapping("/arima/forecast-intervals")
    public ArimaModelResponse arimaForecastIntervals(@Valid @RequestBody ArimaRequest req) {
        var model = new ARIMA(req.getP(), req.getD(), req.getQ(),
                req.getMaxIterations(), req.getTolerance());
        model.fit(req.getData());
        var ivf = model.forecastWithIntervals(req.getSteps(), req.getConfidenceLevel());

        var resp = new ArimaModelResponse();
        resp.setForecasts(ivf.getForecasts());
        resp.setIntervals(ivf.getIntervals().stream()
                .map(PredictionIntervalDto::from).collect(Collectors.toList()));
        resp.setArCoefficients(model.getArCoefficients());
        resp.setMaCoefficients(model.getMaCoefficients());
        resp.setIntercept(model.getIntercept());
        resp.setInnovationVariance(model.getInnovationVariance());
        resp.setP(model.getP()); resp.setD(model.getD()); resp.setQ(model.getQ());
        return resp;
    }

    @Operation(summary = "Fit SARIMA and forecast")
    @PostMapping("/sarima/forecast")
    public SarimaModelResponse sarimaForecast(@Valid @RequestBody SarimaRequest req) {
        var model = new SARIMA(req.getP(), req.getD(), req.getQ(),
                req.getSeasonalP(), req.getSeasonalD(), req.getSeasonalQ(),
                req.getSeasonalPeriod(), req.getMaxIterations(), req.getTolerance());
        model.fit(req.getData());

        var resp = new SarimaModelResponse();
        resp.setForecasts(model.forecast(req.getSteps()));
        resp.setArCoefficients(model.getArCoefficients());
        resp.setMaCoefficients(model.getMaCoefficients());
        resp.setSeasonalArCoefficients(model.getSeasonalArCoefficients());
        resp.setSeasonalMaCoefficients(model.getSeasonalMaCoefficients());
        resp.setIntercept(model.getIntercept());
        resp.setInnovationVariance(model.getInnovationVariance());
        resp.setP(model.getP()); resp.setD(model.getD()); resp.setQ(model.getQ());
        resp.setSeasonalP(model.getSeasonalP()); resp.setSeasonalD(model.getSeasonalD());
        resp.setSeasonalQ(model.getSeasonalQ()); resp.setSeasonalPeriod(model.getSeasonalPeriod());
        return resp;
    }

    @Operation(summary = "Fit SARIMA and forecast with prediction intervals")
    @PostMapping("/sarima/forecast-intervals")
    public SarimaModelResponse sarimaForecastIntervals(@Valid @RequestBody SarimaRequest req) {
        var model = new SARIMA(req.getP(), req.getD(), req.getQ(),
                req.getSeasonalP(), req.getSeasonalD(), req.getSeasonalQ(),
                req.getSeasonalPeriod(), req.getMaxIterations(), req.getTolerance());
        model.fit(req.getData());
        var ivf = model.forecastWithIntervals(req.getSteps(), req.getConfidenceLevel());

        var resp = new SarimaModelResponse();
        resp.setForecasts(ivf.getForecasts());
        resp.setIntervals(ivf.getIntervals().stream()
                .map(PredictionIntervalDto::from).collect(Collectors.toList()));
        resp.setArCoefficients(model.getArCoefficients());
        resp.setMaCoefficients(model.getMaCoefficients());
        resp.setSeasonalArCoefficients(model.getSeasonalArCoefficients());
        resp.setSeasonalMaCoefficients(model.getSeasonalMaCoefficients());
        resp.setIntercept(model.getIntercept());
        resp.setInnovationVariance(model.getInnovationVariance());
        resp.setP(model.getP()); resp.setD(model.getD()); resp.setQ(model.getQ());
        resp.setSeasonalP(model.getSeasonalP()); resp.setSeasonalD(model.getSeasonalD());
        resp.setSeasonalQ(model.getSeasonalQ()); resp.setSeasonalPeriod(model.getSeasonalPeriod());
        return resp;
    }

    @Operation(summary = "Fit ARIMAX (with exogenous regressors) and forecast")
    @PostMapping("/arimax/forecast")
    public ArimaXForecastResponse arimaxForecast(@Valid @RequestBody ArimaXRequest req) {
        double[][] exog = toMatrix(req.getExogenous());
        double[][] futureExog = toMatrix(req.getFutureExogenous());

        var model = new ARIMAX(req.getP(), req.getD(), req.getQ(),
                req.getMaxIterations(), req.getTolerance());
        model.fit(req.getData(), exog);

        var resp = new ArimaXForecastResponse();
        resp.setForecasts(model.forecast(futureExog));
        resp.setArCoefficients(model.getArCoefficients());
        resp.setMaCoefficients(model.getMaCoefficients());
        resp.setExogenousCoefficients(model.getExogenousCoefficients());
        resp.setInnovationVariance(model.getInnovationVariance());
        return resp;
    }

    @Operation(summary = "Search for best ARIMA or SARIMA order using information criteria")
    @PostMapping("/arima/order-search")
    public OrderSearchResponse orderSearch(@Valid @RequestBody OrderSearchRequest req) {
        var criterion = ArimaOrderSearch.Criterion.valueOf(req.getCriterion().toUpperCase());
        ArimaOrderSearch.OrderScore best;
        if (req.getSeasonalPeriod() != null) {
            best = ArimaOrderSearch.searchBestSarima(
                    req.getData(),
                    req.getMaxP(), req.getMaxD(), req.getMaxQ(),
                    req.getMaxSeasonalP() != null ? req.getMaxSeasonalP() : 1,
                    req.getMaxSeasonalD() != null ? req.getMaxSeasonalD() : 1,
                    req.getMaxSeasonalQ() != null ? req.getMaxSeasonalQ() : 1,
                    req.getSeasonalPeriod(), criterion);
        } else {
            best = ArimaOrderSearch.searchBestArima(
                    req.getData(), req.getMaxP(), req.getMaxD(), req.getMaxQ(), criterion);
        }
        return OrderSearchResponse.from(best);
    }

    @Operation(summary = "Fit VAR model and forecast multiple series")
    @PostMapping("/var/forecast")
    public VarForecastResponse varForecast(@Valid @RequestBody VarRequest req) {
        VARModel model;
        if (req.getLagOrder() != null) {
            model = new VARModel(req.getLagOrder());
            model.fit(req.getSeries());
        } else {
            model = VARModel.fitOptimal(req.getSeries(), req.getMaxLag());
        }

        var resp = new VarForecastResponse();
        resp.setForecasts(model.forecast(req.getSteps()));
        resp.setLagOrder(model.getLagOrder());
        resp.setNumSeries(model.getNumSeries());
        resp.setAic(model.getAic());
        return resp;
    }

    private double[][] toMatrix(List<List<Double>> list) {
        if (list == null || list.isEmpty()) return new double[0][0];
        double[][] matrix = new double[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            List<Double> row = list.get(i);
            matrix[i] = new double[row.size()];
            for (int j = 0; j < row.size(); j++) {
                matrix[i][j] = row.get(j);
            }
        }
        return matrix;
    }
}
