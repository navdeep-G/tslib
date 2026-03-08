import java.util.List;
import tslib.model.ExponentialSmoothing;
import tslib.model.TripleExpSmoothing;
import tslib.util.Util;

public class ForecastExample {
    public static void main(String[] args) throws Exception {
        List<Double> data = Util.readFile("data/hotel.txt");
        ExponentialSmoothing model = new TripleExpSmoothing(0.5, 0.3, 0.2, 12, false);
        List<Double> forecast = model.forecast(data, 5);
        System.out.println(forecast);
    }
}
