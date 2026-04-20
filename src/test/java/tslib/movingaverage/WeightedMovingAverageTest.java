package tslib.movingaverage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive unit tests for WeightedMovingAverage.
 * Tests edge cases, mathematical correctness, and API behavior.
 *
 * Author: navdeep
 */
public class WeightedMovingAverageTest {

    private WeightedMovingAverage wma;

    @BeforeEach
    public void setUp() {
        wma = new WeightedMovingAverage(5);
    }

    @Test
    public void testConstructorWithValidPeriod() {
        WeightedMovingAverage wma3 = new WeightedMovingAverage(3);
        WeightedMovingAverage wma10 = new WeightedMovingAverage(10);
        assertNotNull(wma3);
        assertNotNull(wma10);
    }

    @Test
    public void testConstructorWithInvalidPeriodZero() {
        assertThrows(IllegalArgumentException.class, () -> new WeightedMovingAverage(0));
    }

    @Test
    public void testConstructorWithInvalidPeriodNegative() {
        assertThrows(IllegalArgumentException.class, () -> new WeightedMovingAverage(-1));
    }

    @Test
    public void testConstructorWithInvalidPeriodNegativeFive() {
        assertThrows(IllegalArgumentException.class, () -> new WeightedMovingAverage(-5));
    }

    @Test
    public void testEmptyInput() {
        List<Double> result = wma.compute(new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testNullInput() {
        List<Double> result = wma.compute(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSingleValue() {
        List<Double> data = Arrays.asList(10.0);
        List<Double> result = wma.compute(data);
        
        assertEquals(1, result.size());
        assertNull(result.get(0)); // Not enough data for period 5
    }

    @Test
    public void testInsufficientData() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        List<Double> result = wma.compute(data);
        
        assertEquals(4, result.size());
        assertNull(result.get(0));
        assertNull(result.get(1));
        assertNull(result.get(2));
        assertNull(result.get(3));
    }

    @Test
    public void testExactPeriodData() {
        // Test with exactly 5 values: [1, 2, 3, 4, 5]
        // Weights: [1, 2, 3, 4, 5]
        // Weighted sum: 1*1 + 2*2 + 3*3 + 4*4 + 5*5 = 1 + 4 + 9 + 16 + 25 = 55
        // Weight sum: 1 + 2 + 3 + 4 + 5 = 15
        // Expected: 55/15 = 3.666...
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> result = wma.compute(data);
        
        assertEquals(5, result.size());
        assertNull(result.get(0));
        assertNull(result.get(1));
        assertNull(result.get(2));
        assertNull(result.get(3));
        assertEquals(55.0/15.0, result.get(4), 1e-10);
    }

    @Test
    public void testPeriod3Calculation() {
        WeightedMovingAverage wma3 = new WeightedMovingAverage(3);
        // Test with [1, 2, 3]
        // Weights: [1, 2, 3]
        // Weighted sum: 1*1 + 2*2 + 3*3 = 1 + 4 + 9 = 14
        // Weight sum: 1 + 2 + 3 = 6
        // Expected: 14/6 = 2.333...
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> result = wma3.compute(data);
        
        assertEquals(3, result.size());
        assertNull(result.get(0));
        assertNull(result.get(1));
        assertEquals(14.0/6.0, result.get(2), 1e-10);
    }

    @Test
    public void testSlidingWindow() {
        // Test sliding window behavior
        // Data: [1, 2, 3, 4, 5, 6, 7]
        // Window 1: [1, 2, 3, 4, 5] -> (1*1 + 2*2 + 3*3 + 4*4 + 5*5) / 15 = 55/15
        // Window 2: [2, 3, 4, 5, 6] -> (2*1 + 3*2 + 4*3 + 5*4 + 6*5) / 15 = 70/15
        // Window 3: [3, 4, 5, 6, 7] -> (3*1 + 4*2 + 5*3 + 6*4 + 7*5) / 15 = 85/15
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0);
        List<Double> result = wma.compute(data);
        
        assertEquals(7, result.size());
        assertNull(result.get(0));
        assertNull(result.get(1));
        assertNull(result.get(2));
        assertNull(result.get(3));
        assertEquals(55.0/15.0, result.get(4), 1e-10);
        assertEquals(70.0/15.0, result.get(5), 1e-10);
        assertEquals(85.0/15.0, result.get(6), 1e-10);
    }

    @Test
    public void testConstantValues() {
        // Test with constant values - WMA should equal the constant
        List<Double> data = Arrays.asList(5.0, 5.0, 5.0, 5.0, 5.0);
        List<Double> result = wma.compute(data);
        
        assertEquals(5, result.size());
        assertNull(result.get(0));
        assertNull(result.get(1));
        assertNull(result.get(2));
        assertNull(result.get(3));
        assertEquals(5.0, result.get(4), 1e-10);
    }

    @Test
    public void testIncreasingValues() {
        // Test with strictly increasing values
        // Data: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        List<Double> result = wma.compute(data);
        
        assertEquals(10, result.size());
        // Check that WMA values are increasing (trend following)
        assertTrue(result.get(4) < result.get(5));
        assertTrue(result.get(5) < result.get(6));
        assertTrue(result.get(6) < result.get(7));
        assertTrue(result.get(7) < result.get(8));
        assertTrue(result.get(8) < result.get(9));
    }

    @Test
    public void testDecreasingValues() {
        // Test with strictly decreasing values
        // Data: [10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
        List<Double> data = Arrays.asList(10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0);
        List<Double> result = wma.compute(data);
        
        assertEquals(10, result.size());
        // Check that WMA values are decreasing (trend following)
        assertTrue(result.get(4) > result.get(5));
        assertTrue(result.get(5) > result.get(6));
        assertTrue(result.get(6) > result.get(7));
        assertTrue(result.get(7) > result.get(8));
        assertTrue(result.get(8) > result.get(9));
    }

    @Test
    public void testResetFunctionality() {
        // Test that reset works correctly
        List<Double> data1 = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> result1 = wma.compute(data1);
        
        // Reset and compute with different data
        wma.reset();
        List<Double> data2 = Arrays.asList(10.0, 20.0, 30.0, 40.0, 50.0);
        List<Double> result2 = wma.compute(data2);
        
        // Results should be different
        assertNotEquals(result1.get(4), result2.get(4));
    }

    @Test
    public void testAddMethod() {
        // Test the add method directly
        wma.add(1.0);
        wma.add(2.0);
        wma.add(3.0);
        wma.add(4.0);
        wma.add(5.0);
        
        // Should have 5 values now
        assertEquals(5, wma.compute(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0)).size());
    }

    @Test
    public void testLargePeriod() {
        // Test with a larger period
        WeightedMovingAverage wma10 = new WeightedMovingAverage(10);
        List<Double> data = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            data.add((double) i);
        }
        
        List<Double> result = wma10.compute(data);
        assertEquals(15, result.size());
        
        // First 9 should be null
        for (int i = 0; i < 9; i++) {
            assertNull(result.get(i));
        }
        
        // 10th should have a value
        assertNotNull(result.get(9));
    }

    @Test
    public void testPrecision() {
        // Test precision with decimal values
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        List<Double> result = wma.compute(data);
        
        // Manual calculation:
        // Weights: [1, 2, 3, 4, 5]
        // Weighted sum: 1.1*1 + 2.2*2 + 3.3*3 + 4.4*4 + 5.5*5 = 1.1 + 4.4 + 9.9 + 17.6 + 27.5 = 60.5
        // Weight sum: 15
        // Expected: 60.5/15 = 4.033...
        assertEquals(60.5/15.0, result.get(4), 1e-10);
    }

    @Test
    public void testNegativeValues() {
        // Test with negative values
        List<Double> data = Arrays.asList(-1.0, -2.0, -3.0, -4.0, -5.0);
        List<Double> result = wma.compute(data);
        
        // Manual calculation:
        // Weights: [1, 2, 3, 4, 5]
        // Weighted sum: -1*1 + -2*2 + -3*3 + -4*4 + -5*5 = -1 - 4 - 9 - 16 - 25 = -55
        // Weight sum: 15
        // Expected: -55/15 = -3.666...
        assertEquals(-55.0/15.0, result.get(4), 1e-10);
    }

    @Test
    public void testMixedPositiveNegative() {
        // Test with mixed positive and negative values
        List<Double> data = Arrays.asList(-1.0, 2.0, -3.0, 4.0, -5.0);
        List<Double> result = wma.compute(data);
        
        // Manual calculation:
        // Weights: [1, 2, 3, 4, 5]
        // Weighted sum: -1*1 + 2*2 + -3*3 + 4*4 + -5*5 = -1 + 4 - 9 + 16 - 25 = -15
        // Weight sum: 15
        // Expected: -15/15 = -1.0
        assertEquals(-1.0, result.get(4), 1e-10);
    }
} 