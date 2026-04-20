package tslib.selection;

import java.util.List;
import org.junit.jupiter.api.Test;
import tslib.model.arima.ArimaOrderSearch;
import static org.junit.jupiter.api.Assertions.*;

public class AutoSelectionEdgeCaseTest {

    @Test
    public void seasonalAutoArimaChoosesSeasonalPathWhenConfigured() {
        List<Double> data = List.of(10.0, 12.0, 11.0, 13.0, 10.5, 12.5, 11.5, 13.5, 11.0, 13.0, 12.0, 14.0);
        AutoArima model = new AutoArima(1, 1, 1, 1, 1, 1, 4, ArimaOrderSearch.Criterion.AIC).fit(data);
        assertNotNull(model.getBestOrder());
        assertNotNull(model.forecast(2));
    }
}
