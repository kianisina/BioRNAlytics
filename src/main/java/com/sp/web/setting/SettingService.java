package com.sp.web.setting;

import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
public interface SettingService {
    Optional<Setting> getSetting();
    Setting saveSetting(Setting setting);
}