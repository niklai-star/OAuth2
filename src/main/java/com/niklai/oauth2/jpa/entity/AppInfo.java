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
@Table(name = "app_info")
public class AppInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "varchar(20)", nullable = false)
    private String name;

    @Column(name = "app_key", columnDefinition = "varchar(32)", nullable = false)
    private String appKey;

    @Column(name = "app_secret", columnDefinition = "varchar(32)", nullable = false)
    private String appSecret;

    @Column(name = "redirect_url", columnDefinition = "varchar(200)", nullable = false)
    private String redirectUrl;
}
