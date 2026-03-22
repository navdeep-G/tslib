package tslib.model.statespace;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class LocalLevelModelTest {

    @Test
    public void localLevelModelProducesStableForecasts() {
        List<Double> data = List.of(10.0, 10.5, 9.8, 10.2, 10.1, 9.9, 10.0, 10.3);

        LocalLevelModel model = new LocalLevelModel().fit(data);
        List<Double> filtered = model.getFilteredStates();
        List<Double> forecast = model.forecast(3);

        assertEquals(data.size(), filtered.size());
        assertEquals(3, forecast.size());
        assertTrue(model.getProcessVariance() > 0.0);
        assertTrue(model.getObservationVariance() > 0.0);
        assertEquals(filtered.get(filtered.size() - 1), forecast.get(0), 1e-9);
    }
}
