package tslib.api.dto;

import tslib.evaluation.BacktestResult;

import java.util.List;

public class BacktestResponse {
    private List<Double> actual;
    private List<Double> forecast;
    private List<Integer> origins;
    private double mae;
    private double rmse;
    private double mape;
    private double smape;
    private double mase;

    public static BacktestResponse from(BacktestResult r) {
        var resp = new BacktestResponse();
        resp.actual = r.getActual();
        resp.forecast = r.getForecast();
        resp.origins = r.getOrigins();
        resp.mae = r.getMae();
        resp.rmse = r.getRmse();
        resp.mape = r.getMape();
        resp.smape = r.getSmape();
        resp.mase = r.getMase();
        return resp;
    }

    public List<Double> getActual() { return actual; }
    public List<Double> getForecast() { return forecast; }
    public List<Integer> getOrigins() { return origins; }
    public double getMae() { return mae; }
    public double getRmse() { return rmse; }
    public double getMape() { return mape; }
    public double getSmape() { return smape; }
    public double getMase() { return mase; }
}
