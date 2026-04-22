package tslib.api.dto;

import java.util.List;

public class VarForecastResponse {
    private List<List<Double>> forecasts;
    private int lagOrder;
    private int numSeries;
    private double aic;

    public List<List<Double>> getForecasts() { return forecasts; }
    public void setForecasts(List<List<Double>> forecasts) { this.forecasts = forecasts; }
    public int getLagOrder() { return lagOrder; }
    public void setLagOrder(int lagOrder) { this.lagOrder = lagOrder; }
    public int getNumSeries() { return numSeries; }
    public void setNumSeries(int numSeries) { this.numSeries = numSeries; }
    public double getAic() { return aic; }
    public void setAic(double aic) { this.aic = aic; }
}
