package tslib.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic train/test splitting helpers.
 */
public class TrainTestSplit {

    private final List<Double> train;
    private final List<Double> test;

    private TrainTestSplit(List<Double> train, List<Double> test) {
        this.train = train;
        this.test = test;
    }

    public static TrainTestSplit atIndex(List<Double> data, int trainSize) {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("Data must contain at least 2 points");
        }
        if (trainSize < 1 || trainSize >= data.size()) {
            throw new IllegalArgumentException("Train size must be in [1, data.size() - 1]");
        }
        return new TrainTestSplit(
                new ArrayList<>(data.subList(0, trainSize)),
                new ArrayList<>(data.subList(trainSize, data.size())));
    }

    public static TrainTestSplit ratio(List<Double> data, double trainRatio) {
        if (!(trainRatio > 0.0 && trainRatio < 1.0)) {
            throw new IllegalArgumentException("Train ratio must be in (0, 1)");
        }
        int trainSize = Math.max(1, Math.min(data.size() - 1, (int) Math.round(data.size() * trainRatio)));
        return atIndex(data, trainSize);
    }

    public List<Double> getTrain() {
        return new ArrayList<>(train);
    }

    public List<Double> getTest() {
        return new ArrayList<>(test);
    }
}
