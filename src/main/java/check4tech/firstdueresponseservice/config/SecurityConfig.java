package check4tech.firstdueresponseservice.config;

import check4tech.firstdueresponseservice.Respository.EmployeeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final EmployeeRepository employeeRepo;

    public SecurityConfig(EmployeeRepository employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    // 1️⃣ Load user by username from Employee entity
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> employeeRepo.findByUsername(username)
                .map(emp -> org.springframework.security.core.userdetails.User
                        .withUsername(emp.getUsername())
                        .password(emp.getPassword())
                        .roles(emp.getRole().name()) // ADMIN or ROLE
                        .build())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    // 2️⃣ Password encoder
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    // 4️⃣ Modern SecurityFilterChain using lambdas
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // new way to disable CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .defaultSuccessUrl("/admin/dashboard", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}

