package tslib.util;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LinearAlgebraTest {

    @Test
    public void solveIdentitySystem() {
        double[][] identity = {{1, 0}, {0, 1}};
        double[] rhs = {3.0, 7.0};
        double[] solution = LinearAlgebra.solveLinearSystem(identity, rhs);
        assertEquals(3.0, solution[0], 1e-10);
        assertEquals(7.0, solution[1], 1e-10);
    }

    @Test
    public void solve2x2System() {
        // 2x + y = 5, x + 3y = 10  => x=1, y=3
        double[][] matrix = {{2, 1}, {1, 3}};
        double[] rhs = {5.0, 10.0};
        double[] solution = LinearAlgebra.solveLinearSystem(matrix, rhs);
        assertEquals(1.0, solution[0], 1e-9);
        assertEquals(3.0, solution[1], 1e-9);
    }

    @Test
    public void olsSimpleLinearRegression() {
        // y = 2x + 1: intercept=1, slope=2
        double[][] x = {{1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5}};
        double[] y = {3.0, 5.0, 7.0, 9.0, 11.0};
        double[] params = LinearAlgebra.ordinaryLeastSquares(x, y, 0.0);
        assertEquals(1.0, params[0], 1e-6);
        assertEquals(2.0, params[1], 1e-6);
    }

    @Test
    public void olsRidgeRegularizationShrinks() {
        double[][] x = {{1, 1}, {1, 2}, {1, 3}};
        double[] y = {2.0, 4.0, 6.0};
        double[] noRidge = LinearAlgebra.ordinaryLeastSquares(x, y, 0.0);
        double[] withRidge = LinearAlgebra.ordinaryLeastSquares(x, y, 10.0);
        // Ridge shrinks the slope toward zero
        assertTrue(Math.abs(withRidge[1]) < Math.abs(noRidge[1]));
    }

    @Test
    public void meanOfUniformArray() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertEquals(3.0, LinearAlgebra.mean(values), 1e-10);
    }

    @Test
    public void meanSquaredSkipsFirstElements() {
        double[] values = {100.0, 1.0, 2.0, 3.0};
        double result = LinearAlgebra.meanSquared(values, 1);
        assertEquals((1.0 + 4.0 + 9.0) / 3.0, result, 1e-10);
    }

    @Test
    public void meanSquaredReturnsZeroWhenSkipExceedsLength() {
        double[] values = {1.0, 2.0};
        assertEquals(0.0, LinearAlgebra.meanSquared(values, 5), 1e-10);
    }

    @Test
    public void maxAbsDiffFindsLargestDifference() {
        double[] a = {1.0, 5.0, 3.0};
        double[] b = {1.0, 2.0, 3.5};
        assertEquals(3.0, LinearAlgebra.maxAbsDiff(a, b), 1e-10);
    }

    @Test
    public void toArrayAndToListRoundTrip() {
        List<Double> original = Arrays.asList(1.1, 2.2, 3.3);
        double[] array = LinearAlgebra.toArray(original);
        List<Double> back = LinearAlgebra.toList(array);
        assertEquals(original.size(), back.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), back.get(i), 1e-15);
        }
    }

    @Test
    public void toArrayPreservesOrder() {
        List<Double> data = Arrays.asList(9.0, 1.0, 5.0);
        double[] arr = LinearAlgebra.toArray(data);
        assertEquals(9.0, arr[0], 1e-15);
        assertEquals(1.0, arr[1], 1e-15);
        assertEquals(5.0, arr[2], 1e-15);
    }

    @Test
    public void solveLinearSystemHandlesNearSingularWithRidge() {
        // Near-singular matrix, but with ridge OLS should still produce a result
        double[][] x = {{1, 1}, {1, 1.0001}};
        double[] y = {2.0, 2.0};
        double[] params = LinearAlgebra.ordinaryLeastSquares(x, y, 1e-4);
        assertFalse(Double.isNaN(params[0]));
        assertFalse(Double.isNaN(params[1]));
    }
}
