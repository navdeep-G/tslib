import java.util.List;
import tslib.model.LocalLevelModel;

public class LocalLevelExample {
    public static void main(String[] args) {
        List<Double> data = List.of(10.0, 10.5, 9.8, 10.2, 10.1, 9.9, 10.0, 10.3);

        LocalLevelModel model = new LocalLevelModel().fit(data);
        System.out.println("Filtered states: " + model.getFilteredStates());
        System.out.println("Forecast: " + model.forecast(3));
    }
}
