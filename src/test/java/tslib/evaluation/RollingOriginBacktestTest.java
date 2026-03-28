package tslib.evaluation;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class RollingOriginBacktestTest {
    @Test
    public void backtestCollectsRollingForecasts() {
        List<Double> data = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        RollingOriginBacktest backtest = new RollingOriginBacktest(3, 1);
        BacktestResult result = backtest.run(data, (train, horizon) -> List.of(train.get(train.size() - 1) + 1.0));

        assertEquals(3, result.getActual().size());
        assertEquals(0.0, result.getMae(), 1e-9);
        assertEquals(0.0, result.getRmse(), 1e-9);
    }
}
