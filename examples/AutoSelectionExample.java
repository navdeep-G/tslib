import java.util.List;
import tslib.model.arima.ArimaOrderSearch;
import tslib.selection.AutoArima;
import tslib.selection.AutoETS;

public class AutoSelectionExample {
    public static void main(String[] args) {
        List<Double> trend = List.of(3.0, 4.0, 5.0, 6.0, 7.0, 8.0);

        AutoArima autoArima = new AutoArima(1, 1, 1, ArimaOrderSearch.Criterion.AIC).fit(trend);
        System.out.println(autoArima.getBestOrder().getModelType() + " forecast: " + autoArima.forecast(2));

        AutoETS autoETS = new AutoETS().fit(trend);
        System.out.println(autoETS.getBestType() + " forecast: " + autoETS.forecast(2));
    }
}
