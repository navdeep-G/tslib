package tslib.model.arima;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArimaOrderSearchTest {

    @Test
    public void informationCriteriaFavorDifferencingForSimpleTrend() {
        List<Double> data = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            data.add((double) i);
        }

        ARIMA undifferenced = new ARIMA(0, 0, 0).fit(data);
        ARIMA differenced = new ARIMA(0, 1, 0).fit(data);

        double undifferencedRss = rss(undifferenced.getResiduals());
        double differencedRss = rss(differenced.getResiduals());

        double undifferencedAic = InformationCriteria.aic(undifferencedRss, undifferenced.getResiduals().size(), 1);
        double differencedAic = InformationCriteria.aic(differencedRss, differenced.getResiduals().size(), 1);

        assertTrue(differencedAic < undifferencedAic);
    }

    @Test
    public void seasonalOrderSearchFindsSeasonalDifferencing() {
        List<Double> data = List.of(
                10.0, 20.0, 30.0, 40.0,
                11.0, 21.0, 31.0, 41.0,
                12.0, 22.0, 32.0, 42.0);

        ArimaOrderSearch.OrderScore best = ArimaOrderSearch.searchBestSarima(
                data,
                1,
                0,
                0,
                0,
                1,
                0,
                4,
                ArimaOrderSearch.Criterion.AIC);

        assertEquals("SARIMA", best.getModelType());
        assertEquals(1, best.getSeasonalD());
        assertEquals(4, best.getSeasonalPeriod());
    }

    @Test
    public void informationCriteriaPenalizeLargerModels() {
        double rss = 10.0;
        double aicSmall = InformationCriteria.aic(rss, 50, 2);
        double aicLarge = InformationCriteria.aic(rss, 50, 5);
        double bicSmall = InformationCriteria.bic(rss, 50, 2);
        double bicLarge = InformationCriteria.bic(rss, 50, 5);

        assertTrue(aicLarge > aicSmall);
        assertTrue(bicLarge > bicSmall);
    }

    private double rss(List<Double> residuals) {
        double total = 0.0;
        for (double residual : residuals) {
            total += residual * residual;
        }
        return total;
    }
}
