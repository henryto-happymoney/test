package com.happymoney.cookiecutterservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true, //enables @Secured annotation
        jsr250Enabled = true, //enables @RolesAllowed annotation
        prePostEnabled = true //enables @PreAuthorize, @PostAuthorize, @PreFilter, @PostFilter annotations
)
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http.cors().and().csrf().disable()

                // Set session management to stateless
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // Set unauthorized requests exception handler
                .exceptionHandling()
                .authenticationEntryPoint(
                        (request, response, ex) -> {
                            response.sendError(
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    ex.getMessage()
                                              );
                        }
                                         )
                .and()

                // Set permissions on endpoints
                .mvcMatcher("/api/**").authorizeRequests()
                .mvcMatchers("/actuator/**", // actuator endpoints
                             "/swagger-ui/**", // swagger UI
                             "/swagger-ui.html", // swagger UI
                             "/webjars/**", // swagger resources
                             "/v2/**", // swagger resources
                             "/swagger-resources/**", // swagger resources
                             "/example/**" // example resource
                            ).permitAll() // endpoints don't need auth
                .anyRequest().authenticated()
                .and()

                // JWT OAuth2 authentication
                .oauth2ResourceServer()
                .jwt().decoder(JwtDecoders.fromIssuerLocation(issuer));

    }

    // Used by spring security if CORS is enabled.
    // Uncomment this part and adjust Allowed Origins, and other settings
    /*
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    */
}
