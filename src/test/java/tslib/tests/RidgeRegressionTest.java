package tslib.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RidgeRegressionTest {

    private static double[][] column(double... xs) {
        double[][] X = new double[xs.length][1];
        for (int i = 0; i < xs.length; i++) X[i][0] = xs[i];
        return X;
    }

    @Test
    public void defaultL2penaltyIsZero() {
        RidgeRegression r = new RidgeRegression(column(1, 2, 3), new double[]{2, 4, 6});
        assertEquals(0.0, r.getL2penalty(), 1e-15);
    }

    @Test
    public void smallPenaltyApproximatesOls() {
        double[][] X = column(1, 2, 3, 4, 5);
        double[] Y = {2, 4, 6, 8, 10};
        RidgeRegression r = new RidgeRegression(X, Y);
        r.updateCoefficients(1e-10);
        assertEquals(2.0, r.getCoefficients()[0], 1e-4);
    }

    @Test
    public void largerPenaltyShrinksCoefficientsByKnownAmount() {
        // Y = 2*X, so OLS β=2; with λ=||X||²=55, ridge β=110/(55+55)=1.0
        double[][] X = column(1, 2, 3, 4, 5);
        double[] Y = {2, 4, 6, 8, 10};
        RidgeRegression r = new RidgeRegression(X, Y);
        r.updateCoefficients(55.0);
        assertEquals(1.0, r.getCoefficients()[0], 1e-9);
    }

    @Test
    public void shrinkageIsMonotoneInPenalty() {
        double[][] X = column(1, 2, 3, 4, 5);
        double[] Y = {2, 4, 6, 8, 10};
        RidgeRegression r = new RidgeRegression(X, Y);
        r.updateCoefficients(1.0);
        double betaSmall = r.getCoefficients()[0];
        r.updateCoefficients(100.0);
        double betaLarge = r.getCoefficients()[0];
        assertTrue(betaLarge < betaSmall, "larger penalty should produce smaller coefficient");
    }

    @Test
    public void standardErrorsArePositiveWithNoisyData() {
        double[][] X = column(1, 2, 3, 4, 5);
        double[] Y = {2.1, 3.9, 6.2, 7.8, 10.1};
        RidgeRegression r = new RidgeRegression(X, Y);
        r.updateCoefficients(0.01);
        double[] se = r.getStandarderrors();
        assertNotNull(se);
        assertEquals(1, se.length);
        assertTrue(se[0] > 0.0, "standard error should be positive with noisy data");
    }

    @Test
    public void getL2penaltyReflectsLastUpdateCoefficients() {
        double[][] X = column(1, 2, 3);
        double[] Y = {1, 2, 3};
        RidgeRegression r = new RidgeRegression(X, Y);
        r.updateCoefficients(42.0);
        assertEquals(42.0, r.getL2penalty(), 1e-15);
    }

    @Test
    public void setL2penaltyUpdatesValue() {
        RidgeRegression r = new RidgeRegression(column(1, 2, 3), new double[]{1, 2, 3});
        r.setL2penalty(7.5);
        assertEquals(7.5, r.getL2penalty(), 1e-15);
    }

    @Test
    public void coefficientsLengthMatchesFeatureCount() {
        double[][] X = new double[][]{{1, 0}, {0, 1}, {1, 1}, {2, 1}};
        double[] Y = {1, 2, 3, 4};
        RidgeRegression r = new RidgeRegression(X, Y);
        r.updateCoefficients(0.1);
        assertEquals(2, r.getCoefficients().length);
    }
}
