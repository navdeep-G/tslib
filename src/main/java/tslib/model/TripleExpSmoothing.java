package tslib.model;

public class TripleExpSmoothing extends tslib.model.expsmoothing.TripleExpSmoothing implements ExponentialSmoothing {
    public TripleExpSmoothing(double alpha, double beta, double gamma, int period, boolean debug) {
        super(alpha, beta, gamma, period, debug);
    }
}
