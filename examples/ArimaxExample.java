import java.util.List;
import tslib.model.ARIMAX;

public class ArimaxExample {
    public static void main(String[] args) {
        List<Double> y = List.of(7.0, 9.0, 11.0, 13.0, 15.0, 17.0);
        double[][] x = {{1.0}, {2.0}, {3.0}, {4.0}, {5.0}, {6.0}};
        ARIMAX model = new ARIMAX(0, 0, 0).fit(y, x);

        System.out.println(model.forecast(new double[][] {{7.0}, {8.0}}));
    }
}
