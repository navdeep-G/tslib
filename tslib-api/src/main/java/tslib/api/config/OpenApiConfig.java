package tslib.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tslibOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("tslib REST API")
                        .description("Time series analysis and forecasting REST API. "
                                + "Covers ARIMA/SARIMA/ARIMAX/VAR, exponential smoothing (ETS), "
                                + "state-space models, AutoARIMA, AutoETS, STL decomposition, "
                                + "stationarity tests, diagnostics, transforms, and more.")
                        .version("1.0.0")
                        .license(new License().name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
