package com.niklai.oauth2.jpa.repository;

import com.niklai.oauth2.jpa.entity.AccountInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountInfoRepository extends CrudRepository<AccountInfo, Long> {
    Optional<AccountInfo> findByIdAndDelFlagFalse(Long id);
    Optional<AccountInfo> findByAccountAndPasswordAndDelFlagFalse(String account, String password);
}
