package tslib.selection;

import java.util.List;
import org.junit.Test;
import tslib.model.arima.ArimaOrderSearch;
import static org.junit.Assert.*;

public class AutoSelectionTest {
    @Test
    public void autoArimaFitsAndForecasts() {
        AutoArima model = new AutoArima(1, 1, 1, ArimaOrderSearch.Criterion.AIC)
                .fit(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
        assertNotNull(model.getBestOrder());
        assertEquals(2, model.forecast(2).size());
    }

    @Test
    public void autoEtsFitsAndForecasts() {
        AutoETS ets = new AutoETS().fit(List.of(3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
        assertNotNull(ets.getBestType());
        assertEquals(2, ets.forecast(2).size());
    }
}
