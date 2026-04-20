package tslib.evaluation;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BenchmarkMarkdownTest {

    @Test
    public void rendersMarkdownTableWithEscapedModelNames() {
        List<BenchmarkSummary> rows = List.of(
                new BenchmarkSummary("Model A|B", 1.0, 2.0, 3.0, 4.0, 5.0),
                new BenchmarkSummary("Model C", 1.5, 2.5, 3.5, 4.5, 5.5));

        String markdown = BenchmarkMarkdown.toMarkdown(rows);

        assertTrue(markdown.contains("| Rank | Model | MAE | RMSE | MAPE | sMAPE | MASE |"));
        assertTrue(markdown.contains("Model A\\|B"));
        assertTrue(markdown.contains("1.0000"));
        assertTrue(markdown.contains("5.5000"));
    }
}
