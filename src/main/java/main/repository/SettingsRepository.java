package main.repository;

import main.model.GlobalSetting;
import main.model.enums.GlobalSettingCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<GlobalSetting, Integer> {

    public GlobalSetting findByCode(GlobalSettingCode code);
}
