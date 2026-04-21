package tslib.collect;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollectInMemoryTest {

    private static final double[] ARRAY_DATA = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
    private static final List<Double> LIST_DATA = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);

    @Test
    public void doubleArrayConstructorComputesCorrectAverage() {
        Collect c = new Collect(ARRAY_DATA, 1, 3);
        assertEquals(5.5, c.getAverage(), 1e-10);
    }

    @Test
    public void listConstructorComputesCorrectAverage() {
        Collect c = new Collect(LIST_DATA, 1, 3);
        assertEquals(5.5, c.getAverage(), 1e-10);
    }

    @Test
    public void doubleArrayConstructorComputesCorrectMin() {
        Collect c = new Collect(ARRAY_DATA, 1, 3);
        assertEquals(1.0, c.getMin(), 1e-10);
        assertEquals(0, c.getMinIndex());
    }

    @Test
    public void doubleArrayConstructorComputesCorrectMax() {
        Collect c = new Collect(ARRAY_DATA, 1, 3);
        assertEquals(10.0, c.getMax(), 1e-10);
        assertEquals(9, c.getMaxIndex());
    }

    @Test
    public void listAndArrayConstructorsProduceSameStats() {
        Collect fromArray = new Collect(ARRAY_DATA, 1, 3);
        Collect fromList = new Collect(LIST_DATA, 1, 3);

        assertEquals(fromList.getAverage(), fromArray.getAverage(), 1e-10);
        assertEquals(fromList.getVariance(), fromArray.getVariance(), 1e-10);
        assertEquals(fromList.getMin(), fromArray.getMin(), 1e-10);
        assertEquals(fromList.getMax(), fromArray.getMax(), 1e-10);
    }

    @Test
    public void readFileOnInMemoryCollectReturnsCopyOfData() throws Exception {
        Collect c = new Collect(ARRAY_DATA, 1, 3);
        List<Double> data = c.readFile();
        assertEquals(ARRAY_DATA.length, data.size());
        assertEquals(1.0, data.get(0), 1e-10);
    }

    @Test
    public void inMemoryCollectComputesFirstDifference() {
        Collect c = new Collect(new double[]{1.0, 3.0, 6.0, 10.0}, 1, 2);
        List<Double> diff = c.getFirstDifference();
        assertEquals(3, diff.size());
        assertEquals(2.0, diff.get(0), 1e-10);
        assertEquals(3.0, diff.get(1), 1e-10);
        assertEquals(4.0, diff.get(2), 1e-10);
    }

    @Test
    public void inMemoryCollectComputesRollingAverage() {
        Collect c = new Collect(new double[]{2.0, 4.0, 6.0, 8.0}, 1, 2);
        List<Double> rolling = c.getRollingAverage(2);
        assertEquals(3, rolling.size());
        assertEquals(3.0, rolling.get(0), 1e-10);
        assertEquals(5.0, rolling.get(1), 1e-10);
        assertEquals(7.0, rolling.get(2), 1e-10);
    }

    @Test
    public void inMemoryCollectComputesAcf() {
        Collect c = new Collect(ARRAY_DATA, 1, 3);
        double[] acf = c.acf(3);
        // getAcf returns n+1 values
        assertEquals(4, acf.length);
        // ACF values are bounded [-1, 1]
        for (double v : acf) {
            assertTrue(v >= -1.0 && v <= 1.0, "ACF value out of bounds: " + v);
        }
    }

    @Test
    public void nullArrayThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Collect((double[]) null, 1, 3));
    }

    @Test
    public void emptyArrayThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Collect(new double[0], 1, 3));
    }

    @Test
    public void nullListThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Collect((List<Double>) null, 1, 3));
    }
}
