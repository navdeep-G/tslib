package tslib.model;

import java.util.List;

public class ARIMAX extends tslib.model.arima.ARIMAX {
    public ARIMAX(int p, int d, int q) {
        super(p, d, q);
    }

    public ARIMAX(int p, int d, int q, int maxIterations, double tolerance) {
        super(p, d, q, maxIterations, tolerance);
    }

    @Override
    public ARIMAX fit(List<Double> data, double[][] exogenous) {
        super.fit(data, exogenous);
        return this;
    }
}
