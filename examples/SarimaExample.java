import java.util.List;
import tslib.model.SARIMA;

public class SarimaExample {
    public static void main(String[] args) {
        List<Double> data = List.of(
                10.0, 20.0, 30.0, 40.0,
                11.0, 21.0, 31.0, 41.0,
                12.0, 22.0, 32.0, 42.0);

        SARIMA model = new SARIMA(0, 0, 0, 0, 1, 0, 4).fit(data);
        System.out.println("Next season: " + model.forecast(4));
    }
}
