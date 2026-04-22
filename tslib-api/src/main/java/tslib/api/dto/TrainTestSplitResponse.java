package tslib.api.dto;

import java.util.List;

public class TrainTestSplitResponse {
    private List<Double> train;
    private List<Double> test;
    private int trainSize;
    private int testSize;

    public List<Double> getTrain() { return train; }
    public void setTrain(List<Double> train) { this.train = train; }
    public List<Double> getTest() { return test; }
    public void setTest(List<Double> test) { this.test = test; }
    public int getTrainSize() { return trainSize; }
    public void setTrainSize(int trainSize) { this.trainSize = trainSize; }
    public int getTestSize() { return testSize; }
    public void setTestSize(int testSize) { this.testSize = testSize; }
}
