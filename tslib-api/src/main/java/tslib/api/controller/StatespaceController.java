package tslib.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tslib.api.dto.*;
import tslib.model.statespace.KalmanFilter;
import tslib.model.statespace.LocalLevelModel;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statespace")
@Tag(name = "State Space", description = "Local Level and Kalman Filter models")
public class StatespaceController {

    @Operation(summary = "Local Level (random walk with noise) model — fit and forecast")
    @PostMapping("/local-level/forecast")
    public IntervalForecastResponse localLevelForecast(@Valid @RequestBody LocalLevelRequest req) {
        var model = new LocalLevelModel();
        model.fit(req.getData());
        return IntervalForecastResponse.from(
                model.forecastWithIntervals(req.getSteps(), req.getConfidenceLevel()));
    }

    @Operation(summary = "Local Level model — filtered states and diagnostics")
    @PostMapping("/local-level/filter")
    public LocalLevelFilterResponse localLevelFilter(@Valid @RequestBody LocalLevelRequest req) {
        var model = new LocalLevelModel();
        model.fit(req.getData());

        var resp = new LocalLevelFilterResponse();
        resp.setFilteredStates(model.getFilteredStates());
        resp.setSmoothedSignal(model.getSmoothedSignal());
        resp.setProcessVariance(model.getProcessVariance());
        resp.setObservationVariance(model.getObservationVariance());
        resp.setLogLikelihood(model.getLogLikelihood());
        if (req.getSteps() > 0) {
            resp.setForecasts(model.forecast(req.getSteps()));
            resp.setForecastVariances(model.getForecastVariances(req.getSteps()));
            var intervals = model.forecastIntervals(req.getSteps(), req.getConfidenceLevel());
            resp.setIntervals(intervals.stream()
                    .map(PredictionIntervalDto::from).collect(Collectors.toList()));
        }
        return resp;
    }

    @Operation(summary = "Kalman Filter — filter observations and optionally forecast")
    @PostMapping("/kalman/filter")
    public KalmanFilterResponse kalmanFilter(@Valid @RequestBody KalmanRequest req) {
        var kf = new KalmanFilter(req.getProcessVariance(), req.getObservationVariance());
        var result = kf.filter(req.getData());

        var resp = new KalmanFilterResponse();
        resp.setPredictedStates(result.getPredictedStates());
        resp.setFilteredStates(result.getFilteredStates());
        resp.setFilteredCovariances(result.getFilteredCovariances());
        resp.setInnovations(result.getInnovations());
        resp.setLogLikelihood(result.getLogLikelihood());
        if (req.getSteps() > 0) {
            resp.setForecasts(kf.forecast(req.getSteps()));
            resp.setForecastVariances(kf.forecastVariances(req.getSteps()));
        }
        return resp;
    }

    // Inner response DTO for Local Level filter endpoint
    public static class LocalLevelFilterResponse {
        private java.util.List<Double> filteredStates;
        private java.util.List<Double> smoothedSignal;
        private double processVariance;
        private double observationVariance;
        private double logLikelihood;
        private java.util.List<Double> forecasts;
        private java.util.List<Double> forecastVariances;
        private java.util.List<PredictionIntervalDto> intervals;

        public java.util.List<Double> getFilteredStates() { return filteredStates; }
        public void setFilteredStates(java.util.List<Double> v) { this.filteredStates = v; }
        public java.util.List<Double> getSmoothedSignal() { return smoothedSignal; }
        public void setSmoothedSignal(java.util.List<Double> v) { this.smoothedSignal = v; }
        public double getProcessVariance() { return processVariance; }
        public void setProcessVariance(double v) { this.processVariance = v; }
        public double getObservationVariance() { return observationVariance; }
        public void setObservationVariance(double v) { this.observationVariance = v; }
        public double getLogLikelihood() { return logLikelihood; }
        public void setLogLikelihood(double v) { this.logLikelihood = v; }
        public java.util.List<Double> getForecasts() { return forecasts; }
        public void setForecasts(java.util.List<Double> v) { this.forecasts = v; }
        public java.util.List<Double> getForecastVariances() { return forecastVariances; }
        public void setForecastVariances(java.util.List<Double> v) { this.forecastVariances = v; }
        public java.util.List<PredictionIntervalDto> getIntervals() { return intervals; }
        public void setIntervals(java.util.List<PredictionIntervalDto> v) { this.intervals = v; }
    }
}
