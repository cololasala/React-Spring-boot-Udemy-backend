package com.luciano.backend.usersapp.auth;

import com.luciano.backend.usersapp.auth.filters.JwtAuthenticationFilter;
import com.luciano.backend.usersapp.auth.filters.JwtValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration  //hace que la clase se convierte en una clase de configuración de Spring que define y registra beans en el contenedor de Spring. Estos beans podran ser inyectados en cualquier parte de la app.
public class SpringSecurityConfig {

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Bean           //necesitamos encriptar la password
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean       //al poner bean hacemos que el SecurityFilterChain de spring sea customizado por nosotros
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/api/v1/users", "api/v1/users/{page}").permitAll() //permito las solicitudes get a users sin estar autenticado
                .requestMatchers(HttpMethod.GET, "api/v1/user/{id}").hasAnyRole("ADMIN", "USER") // en esta ruta podran acceder los dos roles (aca se omite el "ROLE_")
                .requestMatchers(HttpMethod.POST, "api/v1/user").hasRole("ADMIN") // para post,delete y put solo podra hacerlo el admin, otra manera de hacer esto, jutamos las 3 ->  .requestMatchers("api/v1/user/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "api/v1/user/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "api/v1/user/{id}").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilter(new JwtAuthenticationFilter(authenticationConfiguration.getAuthenticationManager())) // agregamoos nuestro filter de token
                .addFilter(new JwtValidationFilter(authenticationConfiguration.getAuthenticationManager())) // agregamoos nuestro filter de validacion de token
                .csrf(config -> config.disable()) // csrf sirve para evitar vulnerabilidades en formularios usando JSP o thymeleaf (como no usaremos eso lo deshabilitamos)
                .sessionManagement(managment -> managment.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // hacemos que la sesion no tenga estado ya que la manejaremos desde el front a travez de los tokens
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) //seteamos la config del cors
                .build();
    }

    @Bean   //realizamos la configuracion de cors
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5173")); //origins es mas para cuando "conocemos la ruta"
        config.setAllowedOriginPatterns(Arrays.asList("*"));        //originsPatters es mas para cuando "no conocemos" la ruta
        config.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);     // la cofig se aplicaran en todas las rutas
        return source;
    }

    @Bean   //este filter registra el cors config y se le da alta prioridad
    FilterRegistrationBean<CorsFilter> corsFilter () {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
