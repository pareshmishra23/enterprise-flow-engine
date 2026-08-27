package com.efe.traderecon.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class EfeSecurityConfiguration {

    /**
     * Security-disabled path (default). When efe.security.enabled=false the local
     * platform must remain fully open so the REST/gRPC/GraphQL/JMX/GUI demo works
     * without credentials. Without this explicit permissive chain, Spring Security's
     * default auto-configuration locks every endpoint behind random HTTP Basic auth.
     */
    @Bean
    @ConditionalOnProperty(prefix = "efe.security", name = "enabled", havingValue = "false", matchIfMissing = true)
    SecurityFilterChain efePermissiveSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Security-enabled path. Activate only when efe.security.enabled=true and an
     * OAuth2/JWT resource server is configured (issuer-uri + audience).
     */
    @Bean
    @ConditionalOnProperty(prefix = "efe.security", name = "enabled", havingValue = "true")
    SecurityFilterChain efeSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/health", "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**", "/jmx/**").hasAuthority("SCOPE_efe.admin")
                .requestMatchers("/api/**", "/graphql", "/grpc/**").authenticated()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
            java.util.List<String> scopes = jwt.getClaimAsStringList("scope");
            if (scopes != null) {
                scopes.forEach(scope -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope)));
            }
            return authorities;
        });
        return converter;
    }
}
