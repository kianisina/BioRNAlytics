package com.sp.web.web;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sp.web.setting.Setting;
import com.sp.web.setting.SettingService;

@RestController
@RequestMapping("/api/settings")
public class SettingController {

    @Autowired
    private SettingService settingService;

    @GetMapping("/corporateDesign")
    public ResponseEntity<?> getCorporateDesign() {
        return settingService.getSetting()
            .map(setting -> ResponseEntity.ok().body(setting))
            .orElseGet(() -> ResponseEntity.status(404).body(null));
    }

    @PostMapping("/saveSettings/{id}")
    public ResponseEntity<?> saveSettings(
        @PathVariable String id,
        @RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam("companyName") String companyName,
        @RequestParam("fontColor") String fontColor,
        @RequestParam("backgroundColor") String backgroundColor,
        @RequestParam("imprintText") String imprintText
    ) throws IOException {
        
    
        Setting setting = settingService.getSetting().orElse(new Setting());
        
        // 2. Update the text fields
        setting.setId(id); 
        setting.setCompanyName(companyName);
        setting.setBackgroundColor(backgroundColor);
        setting.setFontColor(fontColor);
        setting.setImprintText(imprintText);

        if (file != null && !file.isEmpty()) {
            setting.setLogo(file.getBytes());
        }

        settingService.saveSetting(setting);
        
        return ResponseEntity.ok().build();
    }
}