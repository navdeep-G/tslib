package tslib.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class KPSSTestTest {

    @Test
    public void whiteNoiseLooksStationaryAtFivePercent() {
        Random random = new Random(7);
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            data.add(random.nextGaussian());
        }

        KPSSTest test = new KPSSTest(data);
        assertTrue(test.isStationaryAtFivePercent());
    }

    @Test
    public void strongTrendFailsTrendStationarityCheck() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 120; i++) {
            data.add(5.0 + i * 0.5);
        }

        KPSSTest test = new KPSSTest(data, KPSSTest.RegressionType.LEVEL, 4);
        assertFalse(test.isStationaryAtFivePercent());
    }
}
