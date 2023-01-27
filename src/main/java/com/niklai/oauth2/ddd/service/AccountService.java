package com.niklai.oauth2.ddd.service;

import com.niklai.oauth2.ddd.entity.Account;
import com.niklai.oauth2.jpa.entity.AccountInfo;
import com.niklai.oauth2.jpa.repository.AccountInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    public Optional<Account> getById(long id) {
        Optional<AccountInfo> accountInfoOptional = accountInfoRepository.findByIdAndDelFlagFalse(id);
        if (accountInfoOptional.isEmpty()) {
            return Optional.empty();
        }

        AccountInfo accountInfo = accountInfoOptional.get();
        return Optional.of(new Account(accountInfo.getId(), accountInfo.getAccount()));
    }
}
