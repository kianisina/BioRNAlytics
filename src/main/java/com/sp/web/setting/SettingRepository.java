package com.sp.web.setting;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SettingRepository extends CrudRepository<Setting, String> {
}
