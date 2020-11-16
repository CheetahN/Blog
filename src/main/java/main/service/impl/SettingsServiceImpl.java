package main.service.impl;

import main.api.request.SettingsRequest;
import main.api.response.SettingsResponse;
import main.model.GlobalSetting;
import main.model.User;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;
import main.repository.SessionRepository;
import main.repository.SettingsRepository;
import main.repository.UserRepository;
import main.service.SettingsService;
import main.service.exceptions.NoUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class SettingsServiceImpl implements SettingsService {
    private final SettingsRepository settingsRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Autowired
    public SettingsServiceImpl(SettingsRepository settingsRepository, SessionRepository sessionRepository, UserRepository userRepository) {
        this.settingsRepository = settingsRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
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
    public boolean setGlobalSettings(String sessionID, SettingsRequest settingsRequest) {
        Integer userId = sessionRepository.getUserId(sessionID);
        if (userId == null)
            return false;
        User user = userRepository.findById(userId).orElseThrow(() -> new NoUserException(userId));
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
