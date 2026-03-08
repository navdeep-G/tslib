package tslib.model;

public class DoubleExpSmoothing extends tslib.model.expsmoothing.DoubleExpSmoothing implements ExponentialSmoothing {
    public DoubleExpSmoothing(double alpha, double gamma, int initializationMethod) {
        super(alpha, gamma, initializationMethod);
    }
}
