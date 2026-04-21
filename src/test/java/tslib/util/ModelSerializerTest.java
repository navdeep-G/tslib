package tslib.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tslib.model.arima.ARIMA;
import tslib.model.arima.SARIMA;
import tslib.model.statespace.LocalLevelModel;

import static org.junit.jupiter.api.Assertions.*;

public class ModelSerializerTest {

    @TempDir
    Path tempDir;

    private static final List<Double> SERIES = List.of(
            1.0, 1.3, 1.1, 1.5, 1.4, 1.8, 1.6, 2.0,
            1.9, 2.3, 2.1, 2.5, 2.4, 2.8, 2.6, 3.0
    );

    @Test
    public void arimaRoundTripProducesIdenticalForecasts() throws IOException, ClassNotFoundException {
        ARIMA original = new ARIMA(1, 0, 1).fit(SERIES);
        Path dest = tempDir.resolve("arima.ser");

        ModelSerializer.save(original, dest.toString());
        ARIMA loaded = ModelSerializer.load(dest.toString());

        List<Double> expectedForecast = original.forecast(5);
        List<Double> actualForecast = loaded.forecast(5);
        assertEquals(expectedForecast.size(), actualForecast.size());
        for (int i = 0; i < expectedForecast.size(); i++) {
            assertEquals(expectedForecast.get(i), actualForecast.get(i), 1e-12);
        }
    }

    @Test
    public void arimaRoundTripPreservesCoefficients() throws IOException, ClassNotFoundException {
        ARIMA original = new ARIMA(2, 1, 1).fit(SERIES);
        Path dest = tempDir.resolve("arima_coeffs.ser");

        ModelSerializer.save(original, dest.toString());
        ARIMA loaded = ModelSerializer.load(dest.toString());

        assertArrayEquals(original.getArCoefficients(), loaded.getArCoefficients(), 1e-15);
        assertArrayEquals(original.getMaCoefficients(), loaded.getMaCoefficients(), 1e-15);
        assertEquals(original.getIntercept(), loaded.getIntercept(), 1e-15);
        assertEquals(original.getInnovationVariance(), loaded.getInnovationVariance(), 1e-15);
    }

    @Test
    public void sarimaRoundTripProducesIdenticalForecasts() throws IOException, ClassNotFoundException {
        List<Double> seasonal = new java.util.ArrayList<>();
        for (int i = 0; i < 24; i++) {
            seasonal.add(10.0 + Math.sin(2 * Math.PI * i / 12) + i * 0.1);
        }
        SARIMA original = new SARIMA(1, 0, 0, 0, 0, 0, 12).fit(seasonal);
        Path dest = tempDir.resolve("sarima.ser");

        ModelSerializer.save(original, dest.toString());
        SARIMA loaded = ModelSerializer.load(dest.toString());

        List<Double> expected = original.forecast(4);
        List<Double> actual = loaded.forecast(4);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i), 1e-12);
        }
    }

    @Test
    public void localLevelModelRoundTripPreservesForecasts() throws IOException, ClassNotFoundException {
        LocalLevelModel original = new LocalLevelModel().fit(SERIES);
        Path dest = tempDir.resolve("llm.ser");

        ModelSerializer.save(original, dest.toString());
        LocalLevelModel loaded = ModelSerializer.load(dest.toString());

        List<Double> expected = original.forecast(3);
        List<Double> actual = loaded.forecast(3);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i), 1e-12);
        }
        assertEquals(original.getLogLikelihood(), loaded.getLogLikelihood(), 1e-12);
    }

    @Test
    public void serializedFileSizeIsNonZero() throws IOException {
        ARIMA model = new ARIMA(1, 0, 0).fit(SERIES);
        Path dest = tempDir.resolve("size_check.ser");
        ModelSerializer.save(model, dest.toString());
        assertTrue(Files.size(dest) > 0);
    }
}
