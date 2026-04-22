package tslib.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class TrainTestSplitRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    private Integer trainSize;
    private Double trainRatio;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public Integer getTrainSize() { return trainSize; }
    public void setTrainSize(Integer trainSize) { this.trainSize = trainSize; }
    public Double getTrainRatio() { return trainRatio; }
    public void setTrainRatio(Double trainRatio) { this.trainRatio = trainRatio; }
}
