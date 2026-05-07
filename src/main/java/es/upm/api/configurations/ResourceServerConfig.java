package es.upm.api.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static es.upm.api.adapter.in.resources.SystemResource.SYSTEM;
import static es.upm.api.adapter.in.resources.SystemResource.VERSION_BADGE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {  // validate tokens y security APIs con SCOPE_*.
    public static final String ROLES_NAME = "roles";
    public static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

    @Bean
    @Order(1)
    public SecurityFilterChain systemEndpointsSecurityConfig(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(SYSTEM + "/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SYSTEM, SYSTEM + VERSION_BADGE).permitAll()
                        .anyRequest().denyAll()
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityConfig(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new RolesClaimConverter());
        return converter;
    }

    /**
     * Converts JWT claims to GrantedAuthority collection.
     * - Reads the "scp" claim for OAuth2 scopes (prefixed with SCOPE_).
     * - Reads the "roles" claim for application roles (prefixed with ROLE_).
     * - "roles" values are normalized to UPPERCASE so that "customer" and
     *   "CUSTOMER" both yield ROLE_CUSTOMER, matching @PreAuthorize("hasRole('CUSTOMER')").
     */
    private static class RolesClaimConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // 1. OAuth2 scopes (scp claim) — default Spring Security handling
            JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
            authorities.addAll(scopeConverter.convert(jwt));

            // 2. Application roles (roles claim) — normalized to uppercase
            Object rolesObj = jwt.getClaim(ROLES_NAME);
            if (rolesObj != null) {
                Collection<String> roles;
                if (rolesObj instanceof String str) {
                    roles = List.of(str.split("\\s+"));
                } else if (rolesObj instanceof Collection<?> col) {
                    roles = col.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList();
                } else {
                    roles = List.of();
                }
                authorities.addAll(
                        roles.stream()
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(String::toUpperCase)
                                .map(r -> new SimpleGrantedAuthority(ROLE_AUTHORITY_PREFIX + r))
                                .toList()
                );
            }

            return authorities;
        }
    }

}
