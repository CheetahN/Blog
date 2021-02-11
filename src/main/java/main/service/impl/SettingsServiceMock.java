package main.service.impl;

import main.api.request.SettingsRequest;
import main.api.response.SettingsResponse;
import main.service.SettingsService;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceMock implements SettingsService {

    @Override
    public SettingsResponse getGlobalSettings() {
        SettingsResponse settingsResponse = new SettingsResponse();
        settingsResponse.setMultiuserMode(true);
        settingsResponse.setStatisticsIsPuplic(true);
        return settingsResponse;
    }

    @Override
    public boolean setGlobalSettings(SettingsRequest settingsRequest) {
        return false;
    }

}
