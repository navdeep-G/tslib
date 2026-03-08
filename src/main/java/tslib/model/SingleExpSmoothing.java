package tslib.model;

public class SingleExpSmoothing extends tslib.model.expsmoothing.SingleExpSmoothing implements ExponentialSmoothing {
    public SingleExpSmoothing(double alpha) {
        super(alpha);
    }
}
