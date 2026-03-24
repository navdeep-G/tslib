import java.util.List;
import tslib.evaluation.IntervalForecast;
import tslib.model.ARIMA;

public class IntervalsExample {
    public static void main(String[] args) {
        ARIMA model = new ARIMA(0, 1, 0).fit(List.of(100.0, 101.0, 102.0, 103.0));
        IntervalForecast forecast = model.forecastWithIntervals(3, 0.95);
        System.out.println(forecast.getForecasts());
        System.out.println(forecast.getIntervals().get(0).getLower() + " to " + forecast.getIntervals().get(0).getUpper());
    }
}
