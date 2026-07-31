package com.smartenergy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(TdengineProperties.class)
@ConditionalOnProperty(prefix = "tdengine", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TdengineConfig {

    @Bean(name = "tdengineDataSource")
    public DataSource tdengineDataSource(TdengineProperties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName(properties.getDriverClassName());
        config.setPoolName("tdengine-pool");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setInitializationFailTimeout(0);
        return new HikariDataSource(config);
    }
}
