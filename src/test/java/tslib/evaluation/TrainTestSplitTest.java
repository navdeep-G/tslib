package tslib.evaluation;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TrainTestSplitTest {

    private static final List<Double> SERIES = List.of(1.0, 2.0, 3.0, 4.0, 5.0);

    @Test
    public void atIndexSplitsCorrectly() {
        TrainTestSplit split = TrainTestSplit.atIndex(SERIES, 3);
        assertEquals(List.of(1.0, 2.0, 3.0), split.getTrain());
        assertEquals(List.of(4.0, 5.0), split.getTest());
    }

    @Test
    public void atIndexMinimalSplit() {
        TrainTestSplit split = TrainTestSplit.atIndex(SERIES, 1);
        assertEquals(1, split.getTrain().size());
        assertEquals(4, split.getTest().size());
    }

    @Test
    public void atIndexMaximalSplit() {
        TrainTestSplit split = TrainTestSplit.atIndex(SERIES, 4);
        assertEquals(4, split.getTrain().size());
        assertEquals(1, split.getTest().size());
    }

    @Test
    public void getTrainReturnsDefensiveCopy() {
        TrainTestSplit split = TrainTestSplit.atIndex(SERIES, 3);
        List<Double> train = split.getTrain();
        train.add(99.0);
        assertEquals(3, split.getTrain().size());
    }

    @Test
    public void getTestReturnsDefensiveCopy() {
        TrainTestSplit split = TrainTestSplit.atIndex(SERIES, 3);
        List<Double> test = split.getTest();
        test.add(99.0);
        assertEquals(2, split.getTest().size());
    }

    @Test
    public void ratioSplitsCorrectly() {
        List<Double> series = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        TrainTestSplit split = TrainTestSplit.ratio(series, 0.8);
        assertEquals(8, split.getTrain().size());
        assertEquals(2, split.getTest().size());
    }

    @Test
    public void ratioRoundsToNearestValidSize() {
        List<Double> series = List.of(1.0, 2.0, 3.0);
        TrainTestSplit split = TrainTestSplit.ratio(series, 0.6);
        assertEquals(3, split.getTrain().size() + split.getTest().size());
        assertTrue(split.getTrain().size() >= 1);
        assertTrue(split.getTest().size() >= 1);
    }

    @Test
    public void atIndexRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> TrainTestSplit.atIndex(null, 1));
    }

    @Test
    public void atIndexRejectsSingleElementList() {
        assertThrows(IllegalArgumentException.class, () -> TrainTestSplit.atIndex(List.of(1.0), 0));
    }

    @Test
    public void atIndexRejectsTrainSizeZero() {
        assertThrows(IllegalArgumentException.class, () -> TrainTestSplit.atIndex(SERIES, 0));
    }

    @Test
    public void atIndexRejectsTrainSizeEqualToDataSize() {
        assertThrows(IllegalArgumentException.class, () -> TrainTestSplit.atIndex(SERIES, SERIES.size()));
    }

    @Test
    public void ratioRejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> TrainTestSplit.ratio(SERIES, 0.0));
    }

    @Test
    public void ratioRejectsOne() {
        assertThrows(IllegalArgumentException.class, () -> TrainTestSplit.ratio(SERIES, 1.0));
    }
}
