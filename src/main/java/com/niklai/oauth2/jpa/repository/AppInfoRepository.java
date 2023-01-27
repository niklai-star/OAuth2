package com.niklai.oauth2.jpa.repository;

import com.niklai.oauth2.jpa.entity.AppInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppInfoRepository extends CrudRepository<AppInfo, Long> {
    Optional<AppInfo> findByAppKeyAndDelFlagFalse(String appKey);
    Optional<AppInfo> findByAppKeyAndAppSecretAndRedirectUrlAndDelFlagFalse(String appKey, String appSecret, String redirectUrl);
    Optional<AppInfo> findByAppKeyAndRedirectUrlAndDelFlagFalse(String appKey, String redirectUrl);
}
