package tslib.collect;

import org.junit.jupiter.api.Test;
import tslib.model.expsmoothing.SingleExpSmoothing;
import tslib.model.expsmoothing.TripleExpSmoothing;
import tslib.movingaverage.*;
import tslib.tests.AugmentedDickeyFuller;
import tslib.transform.Transform;
import tslib.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CollectTest {

    private final String pathToData = "data/hotel.txt";
    private final int lag = 1;
    private final double lambda = 1.6;

    @Test
    public void testForecastAndStatsRun() throws Exception {
        List<Double> file = Util.readFile(pathToData);
        assertNotNull(file);
        assertFalse(file.isEmpty());

        Collect collect = new Collect(pathToData, lag, lag);

        // Basic statistical checks
        assertTrue(collect.getAverage() > 0);
        assertTrue(collect.getVariance() > 0);

        // Transformations
        List<Double> fileBoxCox = Transform.boxCox(file, lambda);
        assertEquals(file.size(), fileBoxCox.size());

        // ADF Test
        AugmentedDickeyFuller adf = new AugmentedDickeyFuller(file);
        assertNotNull(adf);
        assertTrue(adf.getLag() >= 1);

        // SMA
        SimpleMovingAverage sma = new SimpleMovingAverage(2);
        List<Double> smaResult = sma.compute(file);
        assertEquals(file.size(), smaResult.size());

        // CMA
        CumulativeMovingAverage cma = new CumulativeMovingAverage();
        List<Double> cmaResult = cma.compute(file);
        assertEquals(file.size(), cmaResult.size());

        // EMA
        ExponentialMovingAverage ema = new ExponentialMovingAverage(0.3);
        List<Double> emaResult = ema.compute(file);
        assertEquals(file.size(), emaResult.size());

        // SES
        SingleExpSmoothing ses = new SingleExpSmoothing(0.5);
        List<Double> sesForecast = ses.forecast(file, 2);
        assertEquals(file.size() + 2, sesForecast.size());

        // TES
        TripleExpSmoothing tes = new TripleExpSmoothing(0.5411, 0.0086, 1e-04, 12, false);
        List<Double> tesForecast = tes.forecast(file, 2);
        assertEquals(file.size() + 2, tesForecast.size());
    }

    @Test
    public void testPerformanceOptimizations() throws Exception {
        // Generate test data
        List<Double> testData = generateTestData(1000);
        String tempFile = createTempDataFile(testData);
        
        try {
            Collect collect = new Collect(tempFile, 5, 10);
            
            // Test 1: Multiple calls to cached methods
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 100; i++) {
                collect.getAverage();
                collect.getVariance();
                collect.getStandardDeviation();
                collect.getMin();
                collect.getMax();
                collect.getMinIndex();
                collect.getMaxIndex();
            }
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds
            
            // Performance assertion - should complete within reasonable time
            assertTrue(duration < 1000, "Performance test failed: took " + duration + " ms");
            
            // Test 2: Rolling average performance
            startTime = System.nanoTime();
            
            List<Double> rollingAvg = collect.getRollingAverage(50);
            
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1_000_000;
            
            assertNotNull(rollingAvg);
            assertTrue(duration < 500, "Rolling average performance test failed: took " + duration + " ms");
            
            // Test 3: Stationarity tests (expensive operations)
            startTime = System.nanoTime();
            
            double adfStat = collect.getADFStat();
            boolean isStationary = collect.isStationary();
            
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1_000_000;
            
            assertTrue(duration < 2000, "ADF test performance failed: took " + duration + " ms");
            
            // Test 4: Second call to ADF (should be cached)
            startTime = System.nanoTime();
            
            double adfStat2 = collect.getADFStat();
            boolean isStationary2 = collect.isStationary();
            
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1_000_000;
            
            // Cached calls should be much faster
            assertTrue(duration < 100, "Cached ADF test should be faster: took " + duration + " ms");
            
            // Verify cached results are consistent
            assertEquals(adfStat, adfStat2, 1e-10);
            assertEquals(isStationary, isStationary2);
            
            // Test 5: Summary output
            startTime = System.nanoTime();
            
            String summary = collect.toString();
            
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1_000_000;
            
            assertNotNull(summary);
            assertFalse(summary.isEmpty());
            assertTrue(duration < 100, "Summary generation performance failed: took " + duration + " ms");
            
        } finally {
            // Clean up temporary file
            new java.io.File(tempFile).delete();
        }
    }

    @Test
    public void testCachingBehavior() throws Exception {
        List<Double> testData = generateTestData(500);
        String tempFile = createTempDataFile(testData);
        
        try {
            Collect collect = new Collect(tempFile, 3, 5);
            
            // First call - should compute
            long startTime = System.nanoTime();
            double firstAverage = collect.getAverage();
            long firstCallTime = System.nanoTime() - startTime;
            
            // Second call - should use cache
            startTime = System.nanoTime();
            double secondAverage = collect.getAverage();
            long secondCallTime = System.nanoTime() - startTime;
            
            // Verify results are identical
            assertEquals(firstAverage, secondAverage, 1e-10);
            
            // Verify second call is faster (cached)
            assertTrue(secondCallTime < firstCallTime, "Cached call should be faster");
            
            // Test variance caching
            startTime = System.nanoTime();
            double firstVariance = collect.getVariance();
            firstCallTime = System.nanoTime() - startTime;
            
            startTime = System.nanoTime();
            double secondVariance = collect.getVariance();
            secondCallTime = System.nanoTime() - startTime;
            
            assertEquals(firstVariance, secondVariance, 1e-10);
            assertTrue(secondCallTime < firstCallTime, "Cached variance call should be faster");
            
        } finally {
            new java.io.File(tempFile).delete();
        }
    }

    private List<Double> generateTestData(int size) {
        List<Double> data = new ArrayList<>(size);
        Random random = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < size; i++) {
            data.add(random.nextDouble() * 1000);
        }
        
        return data;
    }

    private String createTempDataFile(List<Double> data) throws IOException {
        String tempFile = "temp_test_data.txt";
        try (java.io.PrintWriter writer = new java.io.PrintWriter(tempFile)) {
            for (Double value : data) {
                writer.println(value);
            }
        }
        return tempFile;
    }
}
