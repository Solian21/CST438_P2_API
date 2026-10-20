package edu.csumb.cst438.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class should limit specific API routes to admin only
 * Idk if this will later be replaced with OAuth or something else.
 * But I wanted to make sure users can't call those specific API routes.
 * Also I wanted to test it.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    /**
     * This basically just states what routes can only be called by ADMIN
     *
     * @param http http
     * @return http.build()
     * @throws Exception if there's an exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        AuthenticationEntryPoint entryPoint = (request, response, ex) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            resolver.resolveException(request, response, null, ex);
        };
        AccessDeniedHandler deniedHandler = (request, response, ex) ->
                resolver.resolveException(request, response, null, ex);

        http
                .csrf(csrf -> csrf.disable())
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{userid}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/{userid}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/{userid}").hasRole("ADMIN")
                        // Anyone can browse ingredients.
                        .requestMatchers(HttpMethod.GET,  "/api/v1/ingredients", "/api/v1/ingredients/**").permitAll()
                        //only authenticated users can add ingredients
                        .requestMatchers(HttpMethod.POST, "/api/v1/ingredients").authenticated()

                        .anyRequest().authenticated()

                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                // Adding in OAuth
                .oauth2ResourceServer(oauth2 -> oauth2
                        // using the application's Problem Details response for invalid bearer tokens
                         .authenticationEntryPoint(entryPoint)

                        // Converts valid JWTs and adds the user's database role
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );
        return http.build();
    }

    /**
     * This converts the Google JWT into a Spring authentication object
     * The users email becomes the principal name
     * This lets controllers use principal.getName() to identify the current user
     * it also laods the users database role and converts it into a spring authority
     *
     * @param userRepository repository used to load the authenticated user
     * @return a JWT authentication converter using the email and database role
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(UserRepository userRepository) {
        //Keeping the defualt atuhorities created from the token skope
        JwtGrantedAuthoritiesConverter tokenAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        //Using the google account email as the autehnticiated principal name
        // and now contollers are able to retrieve it using principal.getName()
        converter.setPrincipalClaimName("email");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(tokenAuthoritiesConverter.convert(jwt));
            String email = jwt.getClaimAsString("email");
            //This is what actually loads in the users current role from the data base
            if (email != null) {
                userRepository.findByEmail(email)
                        .map(User::getRole)
                        .filter(role -> role != null && !role.isBlank())
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .ifPresent(authorities::add);
            }
            //return token authoriities and the database role authorities
            return authorities;
        });

        return converter;
    }
}