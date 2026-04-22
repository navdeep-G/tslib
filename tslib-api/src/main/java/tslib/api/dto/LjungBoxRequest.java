package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class LjungBoxRequest {
    @NotEmpty(message = "residuals must not be empty")
    private List<Double> residuals;

    @Min(1) private int lags = 10;
    @Min(0) private int degreesOfFreedomAdjustment = 0;

    public List<Double> getResiduals() { return residuals; }
    public void setResiduals(List<Double> residuals) { this.residuals = residuals; }
    public int getLags() { return lags; }
    public void setLags(int lags) { this.lags = lags; }
    public int getDegreesOfFreedomAdjustment() { return degreesOfFreedomAdjustment; }
    public void setDegreesOfFreedomAdjustment(int degreesOfFreedomAdjustment) { this.degreesOfFreedomAdjustment = degreesOfFreedomAdjustment; }
}
