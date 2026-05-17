package com.gongchae.gongchae_coming.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(
					"/",
					"/login",
					"/signup",
					"/signup-complete",
					"/css/**",
					"/js/**",
					"/h2-console/**",
					"/api/members/**",
					"/api/recruitments/**"
				).permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(formLogin -> formLogin
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.usernameParameter("username")
				.passwordParameter("password")
				.defaultSuccessUrl("/recruitments", true)
				.failureUrl("/login?error")
				.permitAll()
			)
			.logout(logout -> logout
				.logoutSuccessUrl("/login?logout")
				.permitAll()
			)
			.build();
	}
}
