package guru.springframework.spring6restmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringSecurityConfig {

    //Adding a spring security filter chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //We can disable csrf completely,
        //But for our application, we want to disable csrf for different endpoints for the APIs
        //Here we override the default configuration, so we also need to setup HTTP Basic authentication
        http.authorizeHttpRequests()
                .anyRequest().authenticated()
                .and().httpBasic(Customizer.withDefaults()) //Setup HTTP Basic authentication
                .csrf().ignoringRequestMatchers("/api/**"); //Ignore csrf for everyting that begins with /api/
        return http.build();
    }
}
