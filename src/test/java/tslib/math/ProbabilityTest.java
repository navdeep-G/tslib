package tslib.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProbabilityTest {

    @Test
    public void normalCdfAtZeroIsHalf() {
        assertEquals(0.5, Probability.normalCdf(0.0), 1e-6);
    }

    @Test
    public void normalCdfKnownQuantiles() {
        assertEquals(0.975, Probability.normalCdf(1.96), 1e-3);
        assertEquals(0.025, Probability.normalCdf(-1.96), 1e-3);
        assertEquals(0.841, Probability.normalCdf(1.0), 1e-3);
    }

    @Test
    public void normalCdfSymmetry() {
        double[] xs = {0.5, 1.0, 1.645, 2.0, 2.576};
        for (double x : xs) {
            assertEquals(1.0, Probability.normalCdf(x) + Probability.normalCdf(-x), 1e-9,
                    "symmetry violated at x=" + x);
        }
    }

    @Test
    public void inverseNormalCdfAtHalf() {
        assertEquals(0.0, Probability.inverseNormalCdf(0.5), 1e-6);
    }

    @Test
    public void inverseNormalCdfKnownQuantiles() {
        assertEquals(1.96, Probability.inverseNormalCdf(0.975), 1e-2);
        assertEquals(-1.96, Probability.inverseNormalCdf(0.025), 1e-2);
        assertEquals(2.576, Probability.inverseNormalCdf(0.995), 1e-2);
    }

    @Test
    public void inverseNormalCdfLowerTailBranch() {
        double p = 0.005;
        double z = Probability.inverseNormalCdf(p);
        assertTrue(z < -2.5, "lower tail z should be < -2.5 for p=0.005");
    }

    @Test
    public void inverseNormalCdfUpperTailBranch() {
        double p = 0.995;
        double z = Probability.inverseNormalCdf(p);
        assertTrue(z > 2.5, "upper tail z should be > 2.5 for p=0.995");
    }

    @Test
    public void roundTrip() {
        double[] probs = {0.01, 0.1, 0.25, 0.5, 0.75, 0.9, 0.99};
        for (double p : probs) {
            assertEquals(p, Probability.normalCdf(Probability.inverseNormalCdf(p)), 1e-6,
                    "round-trip failed at p=" + p);
        }
    }

    @Test
    public void inverseNormalCdfRejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> Probability.inverseNormalCdf(0.0));
    }

    @Test
    public void inverseNormalCdfRejectsOne() {
        assertThrows(IllegalArgumentException.class, () -> Probability.inverseNormalCdf(1.0));
    }

    @Test
    public void inverseNormalCdfRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Probability.inverseNormalCdf(-0.1));
    }

    @Test
    public void inverseNormalCdfRejectsGreaterThanOne() {
        assertThrows(IllegalArgumentException.class, () -> Probability.inverseNormalCdf(1.5));
    }
}
