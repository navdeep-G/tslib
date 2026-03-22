package tslib.model;

import java.util.List;

public class LocalLevelModel extends tslib.model.statespace.LocalLevelModel {
    @Override
    public LocalLevelModel fit(List<Double> observations) {
        super.fit(observations);
        return this;
    }
}
