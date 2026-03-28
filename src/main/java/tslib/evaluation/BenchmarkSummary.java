package tslib.evaluation;

/**
 * Flattened benchmark row for comparing forecasting models.
 */
public class BenchmarkSummary implements Comparable<BenchmarkSummary> {

    private final String modelName;
    private final double mae;
    private final double rmse;
    private final double mape;
    private final double smape;
    private final double mase;

    public BenchmarkSummary(String modelName, BacktestResult result) {
        this(modelName, result.getMae(), result.getRmse(), result.getMape(), result.getSmape(), result.getMase());
    }

    public BenchmarkSummary(String modelName, double mae, double rmse, double mape, double smape, double mase) {
        this.modelName = modelName;
        this.mae = mae;
        this.rmse = rmse;
        this.mape = mape;
        this.smape = smape;
        this.mase = mase;
    }

    public String getModelName() {
        return modelName;
    }

    public double getMae() {
        return mae;
    }

    public double getRmse() {
        return rmse;
    }

    public double getMape() {
        return mape;
    }

    public double getSmape() {
        return smape;
    }

    public double getMase() {
        return mase;
    }

    @Override
    public int compareTo(BenchmarkSummary other) {
        return Double.compare(this.rmse, other.rmse);
    }
}
