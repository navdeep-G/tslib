package tslib.collect;

import tslib.transform.Transform;
import tslib.tests.AugmentedDickeyFuller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tslib.util.Util;
import tslib.util.Stats;

/**
 * Capture relevant metrics from a time series dataset.
 * Includes statistics, transforms, stationarity checks, and more.
 * Optimized with caching and lazy initialization for better performance.
 */
public class Collect {

    private final String _filepath;
    private final int _k;
    private final int _n;
    protected final List<Double> _data;
    
    // Cached values for performance — volatile ensures cross-thread visibility
    private volatile Double _cachedAverage = null;
    private volatile Double _cachedVariance = null;
    private volatile Double _cachedStandardDeviation = null;
    private volatile Integer _cachedMinIndex = null;
    private volatile Integer _cachedMaxIndex = null;
    private volatile Double _cachedMin = null;
    private volatile Double _cachedMax = null;
    private volatile Double _cachedADFStat = null;
    private volatile Boolean _cachedIsStationary = null;
    private volatile AugmentedDickeyFuller _adfInstance = null;

    public Collect(String filepath, int k, int n) throws IOException {
        this._filepath = filepath;
        this._k = k;
        this._n = n;
        this._data = readFile();
    }

    /**
     * Private file reader method
     */
    public List<Double> readFile() throws IOException {
        return Util.readFile(_filepath);
    }

    // === Summary Stats ===

    public double getAverage() {
        if (_cachedAverage == null) {
            _cachedAverage = Stats.average(_data);
        }
        return _cachedAverage;
    }

    public double getVariance() {
        if (_cachedVariance == null) {
            _cachedVariance = Stats.variance(_data);
        }
        return _cachedVariance;
    }

    public double getStandardDeviation() {
        if (_cachedStandardDeviation == null) {
            _cachedStandardDeviation = Stats.standardDeviation(_data);
        }
        return _cachedStandardDeviation;
    }

    public int getMinIndex() {
        if (_cachedMinIndex == null) {
            _cachedMinIndex = findMinIndexOptimized();
        }
        return _cachedMinIndex;
    }

    public int getMaxIndex() {
        if (_cachedMaxIndex == null) {
            _cachedMaxIndex = findMaxIndexOptimized();
        }
        return _cachedMaxIndex;
    }

    public double getMin() {
        if (_cachedMin == null) {
            _cachedMin = _data.get(getMinIndex());
        }
        return _cachedMin;
    }

    public double getMax() {
        if (_cachedMax == null) {
            _cachedMax = _data.get(getMaxIndex());
        }
        return _cachedMax;
    }

    // === Autocorrelation Metrics ===

    public double getAutocovariance() {
        return Stats.getAutoCovariance(_data, _k);
    }

    public double getAutocorrelation() {
        return Stats.getAutoCorrelation(_data, _k);
    }

    public double[] acf(int n) {
        return Stats.getAcf(_data, n);
    }

    public double[] pacf() {
        return Stats.getPacf(_data, _n);
    }

    // === Transformations ===

    public List<Double> getLogTransformed() {
        return Transform.log(_data);
    }

    public List<Double> getBoxCoxTransformed() {
        return Transform.boxCox(_data);
    }

    public List<Double> getFirstDifference() {
        List<Double> diff = new ArrayList<>(_data.size() - 1);
        for (int i = 1; i < _data.size(); i++) {
            diff.add(_data.get(i) - _data.get(i - 1));
        }
        return diff;
    }

    public List<Double> getRollingAverage(int windowSize) {
        if (windowSize <= 0 || windowSize > _data.size()) {
            throw new IllegalArgumentException("Window size must be positive and <= data size");
        }
        
        int resultSize = _data.size() - windowSize + 1;
        List<Double> rolling = new ArrayList<>(resultSize);
        
        // Calculate first window sum
        double windowSum = 0;
        for (int i = 0; i < windowSize; i++) {
            windowSum += _data.get(i);
        }
        rolling.add(windowSum / windowSize);
        
        // Use sliding window technique for O(n) performance
        for (int i = 1; i <= _data.size() - windowSize; i++) {
            windowSum = windowSum - _data.get(i - 1) + _data.get(i + windowSize - 1);
            rolling.add(windowSum / windowSize);
        }
        
        return rolling;
    }

    // === Stationarity Checks ===

    public double getADFStat() {
        if (_cachedADFStat == null) {
            if (_adfInstance == null) {
                _adfInstance = new AugmentedDickeyFuller(_data);
            }
            _cachedADFStat = _adfInstance.getAdfStat();
        }
        return _cachedADFStat;
    }

    public boolean isStationary() {
        if (_cachedIsStationary == null) {
            if (_adfInstance == null) {
                _adfInstance = new AugmentedDickeyFuller(_data);
            }
            _cachedIsStationary = _adfInstance.isStationary();
        }
        return _cachedIsStationary;
    }

    // === Optimized Helper Methods ===

    /**
     * Optimized min index finder - single pass O(n)
     */
    private int findMinIndexOptimized() {
        if (_data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty");
        }
        
        int minIndex = 0;
        double minValue = _data.get(0);
        
        for (int i = 1; i < _data.size(); i++) {
            double current = _data.get(i);
            if (current < minValue) {
                minValue = current;
                minIndex = i;
            }
        }
        return minIndex;
    }

    /**
     * Optimized max index finder - single pass O(n)
     */
    private int findMaxIndexOptimized() {
        if (_data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty");
        }
        
        int maxIndex = 0;
        double maxValue = _data.get(0);
        
        for (int i = 1; i < _data.size(); i++) {
            double current = _data.get(i);
            if (current > maxValue) {
                maxValue = current;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    // === Summary ===

    @Override
    public String toString() {
        return String.format(
                "Collect Summary:\n" +
                        "- Mean: %.4f\n" +
                        "- Std Dev: %.4f\n" +
                        "- Min: %.4f at index %d\n" +
                        "- Max: %.4f at index %d\n" +
                        "- ADF Stat: %.4f (Stationary: %b)",
                getAverage(),
                getStandardDeviation(),
                getMin(), getMinIndex(),
                getMax(), getMaxIndex(),
                getADFStat(), isStationary()
        );
    }
}
