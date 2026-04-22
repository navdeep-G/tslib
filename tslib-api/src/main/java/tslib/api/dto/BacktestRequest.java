package tslib.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BacktestRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @NotNull @Valid
    private ModelSpec modelSpec;

    @Min(1) private int minTrainSize = 20;
    @Min(1) private int horizon = 1;
    @Min(1) private int stepSize = 1;
    @Min(1) private int seasonalPeriod = 1;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public ModelSpec getModelSpec() { return modelSpec; }
    public void setModelSpec(ModelSpec modelSpec) { this.modelSpec = modelSpec; }
    public int getMinTrainSize() { return minTrainSize; }
    public void setMinTrainSize(int minTrainSize) { this.minTrainSize = minTrainSize; }
    public int getHorizon() { return horizon; }
    public void setHorizon(int horizon) { this.horizon = horizon; }
    public int getStepSize() { return stepSize; }
    public void setStepSize(int stepSize) { this.stepSize = stepSize; }
    public int getSeasonalPeriod() { return seasonalPeriod; }
    public void setSeasonalPeriod(int seasonalPeriod) { this.seasonalPeriod = seasonalPeriod; }
}
