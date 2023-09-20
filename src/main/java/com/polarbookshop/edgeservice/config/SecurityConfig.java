package com.polarbookshop.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository rcrp) {
        return http
                .authorizeExchange(exchange -> exchange.anyExchange().authenticated())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(
                                new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
                        ))
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler(
                        oidcLogoutSuccessHandler(rcrp)
                ))
                .csrf(csrf -> csrf.csrfTokenRepository(
                        CookieServerCsrfTokenRepository.withHttpOnlyFalse()
                ))
                .build();
    }
    @Bean
    public WebFilter csrWebFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
                Mono<CsrfToken> csrfToken =
                        exchange.getAttribute(CsrfToken.class.getName());
                return csrfToken != null ? csrfToken.then() : Mono.empty();
            }));
            return chain.filter(exchange);
        };
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository rcrp) {
        var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(rcrp);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return  oidcLogoutSuccessHandler;
    }
}
