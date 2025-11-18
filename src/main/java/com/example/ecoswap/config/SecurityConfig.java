package com.example.ecoswap.config;

import com.example.ecoswap.security.CustomUserDetailsService;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CartMergeAuthenticationSuccessHandler cartMergeSuccessHandler;

    public SecurityConfig(CustomUserDetailsService uds, CartMergeAuthenticationSuccessHandler sh) {
        this.userDetailsService = uds;
        this.cartMergeSuccessHandler = sh;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/adminlte/**", "/images/**", "/uploads/**", "/register**", "/login", "/", "/error", "/about", "/contactus", "/faq", "/shop", "/shop/**", "/product/**").permitAll()
                .requestMatchers("/cart", "/cart/add", "/cart/add-ajax", "/cart/count").permitAll() // Allow guest cart
                .requestMatchers("/cart/checkout", "/cart/place-order").authenticated() // Require login for checkout
                .requestMatchers("/cart/**").authenticated() // Other cart operations need login
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/seller/**").hasRole("SELLER")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(cartMergeSuccessHandler)
                .permitAll()
            )
            .logout(logout -> logout.logoutUrl("/logout").permitAll())
            .csrf(csrf -> csrf.disable());

        http.authenticationProvider(authProvider());
        return http.build();
    }
}
