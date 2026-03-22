package tslib.model;

public class ARIMA extends tslib.model.arima.ARIMA {
    public ARIMA(int p, int d, int q) {
        super(p, d, q);
    }

    public ARIMA(int p, int d, int q, int maxIterations, double tolerance) {
        super(p, d, q, maxIterations, tolerance);
    }
}
