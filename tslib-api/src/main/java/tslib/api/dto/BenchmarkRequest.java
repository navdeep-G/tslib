package tslib.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BenchmarkRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @NotEmpty(message = "models must not be empty")
    @Valid
    private List<NamedModel> models;

    @Min(1) private int minTrainSize = 20;
    @Min(1) private int horizon = 1;
    @Min(1) private int stepSize = 1;
    @Min(1) private int seasonalPeriod = 1;

    public static class NamedModel {
        @NotEmpty private String name;
        @Valid private ModelSpec spec;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public ModelSpec getSpec() { return spec; }
        public void setSpec(ModelSpec spec) { this.spec = spec; }
    }

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public List<NamedModel> getModels() { return models; }
    public void setModels(List<NamedModel> models) { this.models = models; }
    public int getMinTrainSize() { return minTrainSize; }
    public void setMinTrainSize(int minTrainSize) { this.minTrainSize = minTrainSize; }
    public int getHorizon() { return horizon; }
    public void setHorizon(int horizon) { this.horizon = horizon; }
    public int getStepSize() { return stepSize; }
    public void setStepSize(int stepSize) { this.stepSize = stepSize; }
    public int getSeasonalPeriod() { return seasonalPeriod; }
    public void setSeasonalPeriod(int seasonalPeriod) { this.seasonalPeriod = seasonalPeriod; }
}
