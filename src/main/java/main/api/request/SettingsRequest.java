package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SettingsRequest {
    @JsonProperty(value = "MULTIUSER_MODE")
    private boolean multiuserMode;

    @JsonProperty(value = "POST_PREMODERATION")
    private boolean postPremoderation;

    @JsonProperty(value = "STATISTICS_IS_PUBLIC")
    private boolean statisticsPuplic;

}
