package com.xinfei.monitor.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * admin 监控 安全配置
 *
 * <p>本机内网监控中心，关闭登录以便 plus-ui iframe 直接打开。
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .headers((header) ->
                header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

}
