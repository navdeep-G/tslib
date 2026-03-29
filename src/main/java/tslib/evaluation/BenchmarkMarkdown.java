package tslib.evaluation;

import java.util.List;
import java.util.Locale;

/**
 * Renders benchmark summaries as a markdown table suitable for README/docs usage.
 */
public final class BenchmarkMarkdown {

    private BenchmarkMarkdown() {}

    public static String toMarkdown(List<BenchmarkSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            throw new IllegalArgumentException("At least one benchmark summary is required");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("| Rank | Model | MAE | RMSE | MAPE | sMAPE | MASE |\n");
        builder.append("| --- | --- | ---: | ---: | ---: | ---: | ---: |\n");
        for (int i = 0; i < summaries.size(); i++) {
            BenchmarkSummary row = summaries.get(i);
            builder.append("| ")
                    .append(i + 1)
                    .append(" | ")
                    .append(escape(row.getModelName()))
                    .append(" | ")
                    .append(fmt(row.getMae()))
                    .append(" | ")
                    .append(fmt(row.getRmse()))
                    .append(" | ")
                    .append(fmt(row.getMape()))
                    .append(" | ")
                    .append(fmt(row.getSmape()))
                    .append(" | ")
                    .append(fmt(row.getMase()))
                    .append(" |\n");
        }
        return builder.toString();
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private static String escape(String text) {
        return text.replace("|", "\\|");
    }
}
