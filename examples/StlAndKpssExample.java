import java.util.ArrayList;
import java.util.List;
import tslib.decomposition.STLDecomposition;
import tslib.tests.KPSSTest;

public class StlAndKpssExample {
    public static void main(String[] args) {
        List<Double> data = new ArrayList<>();
        double[] seasonalPattern = {2.0, -1.0, 3.0, -2.0};
        for (int i = 0; i < 20; i++) {
            data.add(10.0 + i * 0.5 + seasonalPattern[i % seasonalPattern.length]);
        }

        STLDecomposition.Result result = new STLDecomposition(4).decompose(data);
        KPSSTest kpss = new KPSSTest(result.getRemainder());

        System.out.println("Trend tail: " + result.getTrend().subList(result.getTrend().size() - 4, result.getTrend().size()));
        System.out.println("Seasonal tail: " + result.getSeasonal().subList(result.getSeasonal().size() - 4, result.getSeasonal().size()));
        System.out.println("KPSS statistic on remainder: " + kpss.getStatistic());
    }
}
