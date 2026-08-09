package com.smartenergy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 * <p>
 * 从 application.yml 读取 secret 和 expiration，支持环境变量覆盖。
 * secret 仅用于开发环境，生产环境必须通过 JWT_SECRET 环境变量注入。
 *
 * @author smart-energy
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** JWT 签名密钥（HS256）。开发环境默认值，仅限本地开发使用。 */
    private String secret = "smart-energy-dev-secret-key-2026-do-not-use-in-production";

    /** Token 过期时间（秒），默认 2 小时 */
    private long expiration = 7200;
}
