package com.niklai.oauth2.ddd.entity;

import lombok.Data;

@Data
public class Account {
    private Long id;

    private String account;

    public Account(Long id, String account) {
        this.id = id;
        this.account = account;
    }
}
