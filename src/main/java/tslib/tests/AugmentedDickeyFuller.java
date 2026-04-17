package tslib.tests;

import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class AugmentedDickeyFuller {

    private List<Double> ts;
    private int lag;
    private boolean needsDiff = true;
    private double adfStat;
    private double[] zeroPaddedDiff;

    public AugmentedDickeyFuller(List<Double> ts, int lag) {
        if (lag < 1) {
            throw new IllegalArgumentException("Lag must be >= 1");
        }
        if (ts.size() <= lag + 2) {
            throw new IllegalArgumentException("Time series too short for specified lag.");
        }
        this.ts = ts;
        this.lag = lag;
        computeADFStatistics();
    }

    public AugmentedDickeyFuller(List<Double> ts) {
        this.ts = ts;
        this.lag = (int) Math.floor(Math.cbrt((ts.size() - 1)));
        if (lag < 1) lag = 1;
        if (ts.size() <= lag + 2) {
            throw new IllegalArgumentException("Time series too short for calculated lag.");
        }
        computeADFStatistics();
    }

    private void computeADFStatistics() {
        double[] y = diff(ts);
        int k = lag + 1;
        int n = ts.size() - 1;

        RealMatrix z = MatrixUtils.createRealMatrix(laggedMatrix(y, k));
        RealVector zcol1 = z.getColumnVector(0);
        double[] xt1 = subsetArray(ts, k - 1, n - 1);
        double[] trend = sequence(k, n);

        RealMatrix designMatrix;
        if (k > 1) {
            RealMatrix yt1 = z.getSubMatrix(0, ts.size() - 1 - k, 1, k - 1);
            designMatrix = MatrixUtils.createRealMatrix(ts.size() - 1 - k + 1, 3 + k - 1);
            designMatrix.setColumn(0, xt1);
            designMatrix.setColumn(1, ones(ts.size() - 1 - k + 1));
            designMatrix.setColumn(2, trend);
            designMatrix.setSubMatrix(yt1.getData(), 0, 3);
        } else {
            designMatrix = MatrixUtils.createRealMatrix(ts.size() - 1 - k + 1, 3);
            designMatrix.setColumn(0, xt1);
            designMatrix.setColumn(1, ones(ts.size() - 1 - k + 1));
            designMatrix.setColumn(2, trend);
        }

        RidgeRegression regression = new RidgeRegression(designMatrix.getData(), zcol1.toArray());
        regression.updateCoefficients(0.0001);
        double[] beta = regression.getCoefficients();
        double[] sd = regression.getStandarderrors();

        double t = beta[0] / sd[0];
        this.adfStat = t;
        // Sample-size-aware 5% critical value from MacKinnon / Dickey-Fuller response surface
        double cv5pct = -3.41 + (-4.0 / n);
        this.needsDiff = t > cv5pct;
    }

    private double[] diff(List<Double> x) {
        double[] diff = new double[x.size() - 1];
        this.zeroPaddedDiff = new double[x.size()];
        zeroPaddedDiff[0] = 0;
        for (int i = 0; i < diff.length; i++) {
            double diff_i = x.get(i + 1) - x.get(i);
            diff[i] = diff_i;
            zeroPaddedDiff[i + 1] = diff_i;
        }
        return diff;
    }

    private double[] ones(int n) {
        double[] ones = new double[n];
        for (int i = 0; i < n; i++) {
            ones[i] = 1.0;
        }
        return ones;
    }

    private double[][] laggedMatrix(double[] x, int lag) {
        double[][] laggedMatrix = new double[x.length - lag + 1][lag];
        for (int j = 0; j < lag; j++) {
            for (int i = 0; i < laggedMatrix.length; i++) {
                laggedMatrix[i][j] = x[lag - j - 1 + i];
            }
        }
        return laggedMatrix;
    }

    private double[] subsetArray(List<Double> x, int start, int end) {
        double[] subset = new double[end - start + 1];
        for (int i = 0; i < subset.length && i + start < x.size(); i++) {
            subset[i] = x.get(i + start);
        }
        return subset;
    }

    private double[] sequence(int start, int end) {
        double[] sequence = new double[end - start + 1];
        for (int i = start; i <= end; i++) {
            sequence[i - start] = i;
        }
        return sequence;
    }

    /**
     * Returns an approximate p-value for the ADF test using the MacKinnon / Dickey-Fuller
     * response surface (constant + trend model). Calibrated to the Dickey-Fuller (1979) table.
     * Result is clamped to [0.01, 0.10]; values outside this range are reported at the boundary.
     */
    public double getPValue() {
        int n = ts.size() - 1;
        // Response surface: CV(alpha, n) = c_inf + c1/n
        double[] levels = {0.01,   0.025,  0.05,   0.10};
        double[] cInf   = {-3.96,  -3.68,  -3.41,  -3.12};
        double[] c1     = {-8.0,   -6.0,   -4.0,   -3.0};

        double[] cv = new double[levels.length];
        for (int i = 0; i < levels.length; i++) {
            cv[i] = cInf[i] + c1[i] / n;
        }

        if (adfStat <= cv[0]) return levels[0];
        if (adfStat >= cv[levels.length - 1]) return levels[levels.length - 1];

        for (int i = 0; i < levels.length - 1; i++) {
            if (adfStat >= cv[i] && adfStat <= cv[i + 1]) {
                double frac = (adfStat - cv[i]) / (cv[i + 1] - cv[i]);
                return levels[i] + frac * (levels[i + 1] - levels[i]);
            }
        }
        return levels[levels.length - 1];
    }

    public boolean isNeedsDiff() {
        return needsDiff;
    }

    public double getAdfStat() {
        return adfStat;
    }

    public double[] getZeroPaddedDiff() {
        return zeroPaddedDiff;
    }

    public int getLag() {
        return lag;
    }

    public boolean isStationary() {
        return !needsDiff;
    }
}
