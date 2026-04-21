package tslib.examples;

import tslib.model.ARIMA;
import tslib.model.LocalLevelModel;
import tslib.util.ModelSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example 13: Model serialization — save a fitted model to disk and reload it
 * for inference without re-fitting.
 *
 * All model classes in tslib implement java.io.Serializable.
 * ModelSerializer wraps ObjectOutputStream/ObjectInputStream for convenience.
 */
public class Example13_Serialization {

    public static void run() {
        System.out.println("=== Example 13: Model Serialization ===\n");

        List<Double> series = generateAR1(100, 0.75, 0.4, 1.0, 7);

        // --- 1. Save and reload an ARIMA model ---
        System.out.println("-- ARIMA save / load --");
        ARIMA originalArima = new ARIMA(1, 0, 1).fit(series);
        List<Double> forecastBefore = originalArima.forecast(5);
        System.out.println("Forecast before save : " + fmt(forecastBefore));

        try {
            Path tmpFile = Files.createTempFile("arima_model", ".ser");
            String path = tmpFile.toString();

            // Save to disk
            ModelSerializer.save(originalArima, path);
            System.out.printf("Model saved to       : %s%n", path);

            // Load from disk — no re-fitting needed
            ARIMA loadedArima = ModelSerializer.load(path);
            List<Double> forecastAfter = loadedArima.forecast(5);
            System.out.println("Forecast after load  : " + fmt(forecastAfter));
            System.out.printf("Forecasts match      : %b%n",
                    forecastBefore.equals(forecastAfter));

            // The loaded model's state is fully preserved
            System.out.printf("Innovation variance  : %.6f%n", loadedArima.getInnovationVariance());

            Files.deleteIfExists(tmpFile);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Serialization error: " + e.getMessage());
        }

        // --- 2. Save and reload a LocalLevelModel ---
        System.out.println("\n-- LocalLevelModel save / load --");
        LocalLevelModel llm = new LocalLevelModel().fit(series);
        List<Double> llmForecastBefore = llm.forecast(3);
        System.out.println("LLM forecast before save : " + fmt(llmForecastBefore));

        try {
            Path tmpFile = Files.createTempFile("llm_model", ".ser");
            String path = tmpFile.toString();

            ModelSerializer.save(llm, path);
            LocalLevelModel loadedLlm = ModelSerializer.load(path);

            List<Double> llmForecastAfter = loadedLlm.forecast(3);
            System.out.println("LLM forecast after load  : " + fmt(llmForecastAfter));
            System.out.printf("Forecasts match          : %b%n",
                    llmForecastBefore.equals(llmForecastAfter));

            Files.deleteIfExists(tmpFile);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Serialization error: " + e.getMessage());
        }

        // --- 3. Demonstrate the typical production workflow ---
        System.out.println("\n-- Typical workflow: train once, serve many times --");
        System.out.println("1. Fit model on historical data (expensive)");
        System.out.println("2. ModelSerializer.save(model, \"/models/arima.ser\")");
        System.out.println("3. On service startup: model = ModelSerializer.load(\"/models/arima.ser\")");
        System.out.println("4. Serve real-time forecasts without re-fitting");

        System.out.println();
    }

    static List<Double> generateAR1(int n, double phi, double drift, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        double val = 0.0;
        for (int i = 0; i < n; i++) {
            val = drift + phi * val + sigma * rng.nextGaussian();
            out.add(val);
        }
        return out;
    }

    static List<Double> fmt(List<Double> list) {
        List<Double> out = new ArrayList<>(list.size());
        for (Double v : list) out.add(v == null ? null : Math.round(v * 1000.0) / 1000.0);
        return out;
    }
}
