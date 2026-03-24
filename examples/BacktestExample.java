import java.util.List;
import tslib.evaluation.BacktestResult;
import tslib.evaluation.RollingOriginBacktest;
import tslib.model.ARIMA;

public class BacktestExample {
    public static void main(String[] args) {
        List<Double> data = List.of(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0);
        RollingOriginBacktest backtest = new RollingOriginBacktest(4, 1);
        BacktestResult result = backtest.run(data, (train, horizon) -> new ARIMA(0, 1, 0).forecast(train, horizon));

        System.out.println("RMSE: " + result.getRmse());
        System.out.println("MAE: " + result.getMae());
    }
}
