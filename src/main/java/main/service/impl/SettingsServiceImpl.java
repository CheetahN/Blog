package main.service.impl;

import main.api.request.SettingsRequest;
import main.api.response.SettingsResponse;
import main.model.GlobalSetting;
import main.model.User;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;
import main.repository.SettingsRepository;
import main.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class SettingsServiceImpl implements SettingsService {
    private final SettingsRepository settingsRepository;
    private final UserServiceImpl userService;

    @Autowired
    public SettingsServiceImpl(SettingsRepository settingsRepository, UserServiceImpl userService) {
        this.settingsRepository = settingsRepository;
        this.userService = userService;
    }

    @Override
    public SettingsResponse getGlobalSettings() {
        List<GlobalSetting> settings = settingsRepository.findAll();
        SettingsResponse response = new SettingsResponse();

        for (GlobalSetting parameter : settings) {
            if(parameter.getCode() == GlobalSettingCode.MULTIUSER_MODE)
                response.setMultiuserMode(parameter.getValue() == GlobalSettingValue.YES);
            if(parameter.getCode() == GlobalSettingCode.POST_PREMODERATION)
                response.setPostPremoderation(parameter.getValue() == GlobalSettingValue.YES);
            if(parameter.getCode() == GlobalSettingCode.STATISTICS_IS_PUBLIC)
                response.setStatisticsIsPuplic(parameter.getValue() == GlobalSettingValue.YES);
        }
        return response;
    }

    @Override
    public boolean setGlobalSettings(SettingsRequest settingsRequest) {
        User user = userService.getCurrentUser();
        if (user.getIsModerator() == 0)
            return false;
        GlobalSetting globalSetting = settingsRepository.findByCode(GlobalSettingCode.MULTIUSER_MODE);
        globalSetting.setValue(settingsRequest.isMultiuserMode() ? GlobalSettingValue.YES : GlobalSettingValue.NO);
        settingsRepository.save(globalSetting);
        globalSetting = settingsRepository.findByCode(GlobalSettingCode.POST_PREMODERATION);
        globalSetting.setValue(settingsRequest.isPostPremoderation() ? GlobalSettingValue.YES : GlobalSettingValue.NO);
        settingsRepository.save(globalSetting);
        globalSetting = settingsRepository.findByCode(GlobalSettingCode.STATISTICS_IS_PUBLIC);
        globalSetting.setValue(settingsRequest.isStatisticsPuplic() ? GlobalSettingValue.YES : GlobalSettingValue.NO);
        settingsRepository.saveAndFlush(globalSetting);
        return true;
    }
}
