import java.util.List;
import tslib.transform.Transform;

public class TransformExample {
    public static void main(String[] args) {
        List<Double> data = List.of(1.0, 2.0, 4.0, 8.0);
        double lambda = Transform.boxCoxLambdaSearch(data);
        System.out.println("lambda=" + lambda);
        System.out.println(Transform.boxCox(data, lambda));
    }
}
