package tslib.model.arima;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArimaOrderSearchParallelTest {

    private static List<Double> trendData(int n) {
        List<Double> data = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            data.add((double) i + (i % 3) * 0.1);
        }
        return data;
    }

    private static List<Double> seasonalData() {
        double[] pattern = {10.0, 20.0, 30.0, 40.0};
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            data.add(pattern[i % 4] + i * 0.5);
        }
        return data;
    }

    @Test
    public void arimaSearchReturnsValidModel() {
        List<Double> data = trendData(20);
        ArimaOrderSearch.OrderScore result = ArimaOrderSearch.searchBestArima(
                data, 1, 1, 1, ArimaOrderSearch.Criterion.AIC);
        assertEquals("ARIMA", result.getModelType());
        assertFalse(Double.isNaN(result.getScore()));
        assertFalse(Double.isInfinite(result.getScore()));
    }

    @Test
    public void arimaSearchBicProducesValidScore() {
        List<Double> data = trendData(20);
        ArimaOrderSearch.OrderScore bic = ArimaOrderSearch.searchBestArima(
                data, 1, 1, 1, ArimaOrderSearch.Criterion.BIC);
        ArimaOrderSearch.OrderScore aicc = ArimaOrderSearch.searchBestArima(
                data, 1, 1, 1, ArimaOrderSearch.Criterion.AICC);
        assertFalse(Double.isNaN(bic.getScore()));
        assertFalse(Double.isNaN(aicc.getScore()));
    }

    @Test
    public void sarimaMustReturnValidModel() {
        List<Double> data = seasonalData();
        ArimaOrderSearch.OrderScore result = ArimaOrderSearch.searchBestSarima(
                data, 1, 0, 0, 0, 1, 0, 4, ArimaOrderSearch.Criterion.AIC);
        assertEquals("SARIMA", result.getModelType());
        assertEquals(4, result.getSeasonalPeriod());
        assertFalse(Double.isNaN(result.getScore()));
    }

    @Test
    public void parallelSearchProducesScoreBoundedByManualBest() {
        List<Double> data = trendData(20);

        // Compute the best score from a known good model manually
        ARIMA known = new ARIMA(1, 1, 0).fit(data);
        double knownScore = InformationCriteria.aic(
                rss(known.getResiduals()), known.getResiduals().size(), 2);

        ArimaOrderSearch.OrderScore searched = ArimaOrderSearch.searchBestArima(
                data, 1, 1, 0, ArimaOrderSearch.Criterion.AIC);

        // The searched result must be at least as good as the known model
        assertTrue(searched.getScore() <= knownScore + 1e-6);
    }

    @Test
    public void searchWithZeroMaxReturnsOnlyNonTrivialModel() {
        List<Double> data = trendData(20);
        // max (0,0,1) forces at most MA(1) with no AR, no differencing
        ArimaOrderSearch.OrderScore result = ArimaOrderSearch.searchBestArima(
                data, 0, 0, 1, ArimaOrderSearch.Criterion.AIC);
        assertEquals(0, result.getP());
        assertEquals(0, result.getD());
        assertEquals(1, result.getQ());
    }

    @Test(expected = IllegalArgumentException.class)
    public void searchThrowsWhenNothingFits() {
        // Grid (0,0,0) is skipped; grid of just (0,0,0) should throw
        ArimaOrderSearch.searchBestArima(
                trendData(20), 0, 0, 0, ArimaOrderSearch.Criterion.AIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void searchThrowsOnNegativeBounds() {
        ArimaOrderSearch.searchBestArima(
                trendData(20), -1, 0, 0, ArimaOrderSearch.Criterion.AIC);
    }

    private static double rss(List<Double> residuals) {
        double total = 0.0;
        for (double r : residuals) total += r * r;
        return total;
    }
}
