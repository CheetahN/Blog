package main.service;

import main.api.request.SettingsRequest;
import main.api.response.SettingsResponse;

public interface SettingsService {
    public SettingsResponse getGlobalSettings();

    public boolean setGlobalSettings(SettingsRequest settingsRequest);
}
