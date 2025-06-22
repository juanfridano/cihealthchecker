package com.juanfridano.cihealthchecker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "github")
@Getter
@Setter
public class GitHubProperties {
    private String token;
    private String owner;
    private String repo;
}
