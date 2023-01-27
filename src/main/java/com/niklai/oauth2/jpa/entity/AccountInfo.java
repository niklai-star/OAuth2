package com.niklai.oauth2.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "account_info")
public class AccountInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account", columnDefinition = "varchar(200)", nullable = false)
    private String account;

    @Column(name = "password", columnDefinition = "varchar(200)", nullable = false)
    private String password;
}
