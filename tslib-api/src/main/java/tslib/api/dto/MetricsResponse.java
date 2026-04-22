package tslib.api.dto;

public class MetricsResponse {
    private double mae;
    private double rmse;
    private double mape;
    private double smape;
    private double mase;
    private double meanError;

    public double getMae() { return mae; }
    public void setMae(double mae) { this.mae = mae; }
    public double getRmse() { return rmse; }
    public void setRmse(double rmse) { this.rmse = rmse; }
    public double getMape() { return mape; }
    public void setMape(double mape) { this.mape = mape; }
    public double getSmape() { return smape; }
    public void setSmape(double smape) { this.smape = smape; }
    public double getMase() { return mase; }
    public void setMase(double mase) { this.mase = mase; }
    public double getMeanError() { return meanError; }
    public void setMeanError(double meanError) { this.meanError = meanError; }
}
