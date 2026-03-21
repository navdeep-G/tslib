import java.util.List;
import tslib.model.ARIMA;
import tslib.transform.Differencing;
import tslib.util.Util;

public class ArimaExample {
    public static void main(String[] args) throws Exception {
        List<Double> data = Util.readFile("data/hotel.txt");

        List<Double> firstDifference = Differencing.difference(data);
        System.out.println("First 5 first-difference values: " + firstDifference.subList(0, 5));

        ARIMA model = new ARIMA(1, 1, 0).fit(data);
        List<Double> future = model.forecast(5);

        System.out.println("AR coefficients: " + java.util.Arrays.toString(model.getArCoefficients()));
        System.out.println("Next 5 forecasts: " + future);
    }
}
