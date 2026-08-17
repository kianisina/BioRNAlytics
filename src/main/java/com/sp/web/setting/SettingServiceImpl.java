package com.sp.web.setting;

import com.sp.web.security.JwtAuthorizationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class SettingServiceImpl implements SettingService {
    private final SettingRepository settingRepository;
    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthorizationFilter.class);
    @Autowired
    public SettingServiceImpl(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }
    @Override
    public Optional<Setting> getSetting() {
        return settingRepository.findById("m-map");
    }
    @Override
    public Setting saveSetting(Setting setting) {
        Optional<Setting> settingOptional = settingRepository.findById("m-map");
        if (settingOptional.isPresent()) {
            Setting existingSetting = settingOptional.get();
            // Only update the logo if a new one is provided
            if (setting.getLogo() == null) {
                setting.setLogo(existingSetting.getLogo());
            }
        }
        return settingRepository.save(setting);
    }
}