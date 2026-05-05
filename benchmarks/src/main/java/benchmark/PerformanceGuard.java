package benchmark;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Compares a fresh benchmark run against stored baselines and fails if any
 * metric regresses beyond its allowed tolerance.
 *
 * Tolerances:
 *   exec_ms          — 20× baseline  (CI machines are slower than Apple Silicon)
 *   lower-is-better  — baseline × 1.02  (2% slack for floating-point / JIT variance)
 *   higher-is-better — baseline × 0.98
 *   symmetric        — ±5% of baseline  (test statistics, p-values, lambda)
 *
 * Run via:
 *   ./gradlew benchmarks:guard
 *
 * The guard reads benchmarks/results/java_results.csv (written by BenchmarkRunner)
 * and benchmarks/results/baseline.csv (committed reference values).
 * Update baseline.csv whenever an algorithm improvement intentionally changes a metric.
 */
public class PerformanceGuard {

    private static final String BASELINE_FILE = "benchmarks/results/baseline.csv";
    private static final String RESULTS_FILE  = "benchmarks/results/java_results.csv";

    private static final double TIMING_MULTIPLIER   = 20.0;
    private static final double ACCURACY_TOLERANCE  = 0.02;
    private static final double SYMMETRIC_TOLERANCE = 0.05;

    private static final Set<String> LOWER_BETTER = Set.of(
            "MAE", "RMSE", "MAPE",
            "smoothing_mae", "smoothing_rmse",
            "remainder_var", "imputation_mae");

    private static final Set<String> HIGHER_BETTER = Set.of(
            "seasonal_strength", "trend_strength");

    // Metrics checked with symmetric tolerance (test outputs, not accuracy)
    private static final Set<String> SYMMETRIC = Set.of(
            "statistic", "pvalue", "lambda");

    public static void main(String[] args) throws Exception {
        Map<String, Double> baseline = loadBaseline(BASELINE_FILE);
        Map<String, Double> current  = loadResults(RESULTS_FILE);

        int pass = 0, fail = 0;
        List<String> failures = new ArrayList<>();

        int col1 = 58, col2 = 12, col3 = 12, col4 = 18;
        String header = String.format("%-" + col1 + "s  %-" + col2 + "s  %-" + col3 + "s  %-" + col4 + "s  %s",
                "Check", "Baseline", "Current", "Limit", "Status");
        System.out.println("=== tslib Performance Guard ===\n");
        System.out.println(header);
        System.out.println("=".repeat(header.length() + 4));

        for (Map.Entry<String, Double> entry : baseline.entrySet()) {
            String key          = entry.getKey();
            double baselineVal  = entry.getValue();
            Double currentVal   = current.get(key);

            if (currentVal == null) {
                System.out.printf("%-" + col1 + "s  %-" + col2 + "s  %-" + col3 + "s  %-" + col4 + "s  MISSING%n",
                        key, fmt(baselineVal), "N/A", "N/A");
                failures.add("MISSING: " + key);
                fail++;
                continue;
            }

            String metric = key.split("\\|")[2];
            boolean passed;
            String limit;

            if ("exec_ms".equals(metric)) {
                double max = baselineVal * TIMING_MULTIPLIER;
                passed = currentVal <= max;
                limit  = fmt(max) + " ms (20×)";
            } else if (LOWER_BETTER.contains(metric)) {
                double max = baselineVal * (1.0 + ACCURACY_TOLERANCE);
                passed = currentVal <= max;
                limit  = fmt(max) + " (+2%)";
            } else if (HIGHER_BETTER.contains(metric)) {
                double min = baselineVal * (1.0 - ACCURACY_TOLERANCE);
                passed = currentVal >= min;
                limit  = fmt(min) + " (-2%)";
            } else {
                // symmetric tolerance for test statistics, p-values, lambda
                double absBaseline = Math.max(Math.abs(baselineVal), 1e-10);
                double relDiff = Math.abs(currentVal - baselineVal) / absBaseline;
                passed = relDiff <= SYMMETRIC_TOLERANCE;
                limit  = "±5%";
            }

            String status = passed ? "PASS" : "FAIL";
            System.out.printf("%-" + col1 + "s  %-" + col2 + "s  %-" + col3 + "s  %-" + col4 + "s  %s%n",
                    key, fmt(baselineVal), fmt(currentVal), limit, status);

            if (passed) {
                pass++;
            } else {
                fail++;
                failures.add(key + "  baseline=" + fmt(baselineVal) + "  current=" + fmt(currentVal) + "  limit=" + limit);
            }
        }

        System.out.println("=".repeat(header.length() + 4));
        System.out.printf("%nResults: %d PASS, %d FAIL%n", pass, fail);

        if (!failures.isEmpty()) {
            System.out.println("\nFailed checks:");
            failures.forEach(f -> System.out.println("  FAIL  " + f));
            System.out.println("\nTo accept intentional improvements, update benchmarks/results/baseline.csv.");
            System.exit(1);
        }

        System.out.println("\nAll checks passed.");
    }

    /** Loads baseline.csv: algorithm,dataset,metric,baseline_value */
    private static Map<String, Double> loadBaseline(String path) throws IOException {
        Map<String, Double> map = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(Path.of(path));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 4) continue;
            String key = p[0] + "|" + p[1] + "|" + p[2];
            try {
                map.put(key, Double.parseDouble(p[3]));
            } catch (NumberFormatException ignored) {
                // skip non-numeric entries (e.g. boolean fields)
            }
        }
        return map;
    }

    /** Loads java_results.csv: library,algorithm,dataset,metric,value */
    private static Map<String, Double> loadResults(String path) throws IOException {
        Map<String, Double> map = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(Path.of(path));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 5) continue;
            // p[0]=library  p[1]=algorithm  p[2]=dataset  p[3]=metric  p[4]=value
            String key = p[1] + "|" + p[2] + "|" + p[3];
            try {
                map.put(key, Double.parseDouble(p[4]));
            } catch (NumberFormatException ignored) {
                // skip boolean fields like stationary_5pct
            }
        }
        return map;
    }

    private static String fmt(double v) {
        return String.format("%.6f", v);
    }
}
