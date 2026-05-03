package org.citycare.adminmonitor;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class AdminSecurityConfig {

    private final String adminContextPath;

    public AdminSecurityConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + "/");

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    adminContextPath + "/assets/**",
                    adminContextPath + "/login",
                    adminContextPath + "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(adminContextPath + "/login")
                .successHandler(successHandler)
            )
            .logout(logout -> logout
                .logoutUrl(adminContextPath + "/logout")
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher(adminContextPath + "/instances"),
                    new AntPathRequestMatcher(adminContextPath + "/instances/*"),
                    new AntPathRequestMatcher(adminContextPath + "/actuator/**")
                )
            );

        return http.build();
    }
}
