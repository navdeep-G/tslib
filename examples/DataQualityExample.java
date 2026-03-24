import java.util.Arrays;
import java.util.List;
import tslib.dataquality.MissingValueImputer;
import tslib.dataquality.OutlierDetector;
import tslib.dataquality.Winsorizer;

public class DataQualityExample {
    public static void main(String[] args) {
        List<Double> raw = MissingValueImputer.linearInterpolation(Arrays.asList(1.0, null, null, 4.0, 100.0));
        System.out.println("Imputed: " + raw);
        System.out.println("Outliers: " + OutlierDetector.zScore(raw, 1.5));
        System.out.println("Winsorized: " + Winsorizer.winsorize(raw, 0.0, 0.8));
    }
}
