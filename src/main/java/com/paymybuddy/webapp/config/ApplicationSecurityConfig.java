package com.paymybuddy.webapp.config;

import com.paymybuddy.webapp.service.PMBUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final PMBUserDetailsService pmbUserDetailsService;

    @Autowired
    public ApplicationSecurityConfig(PasswordEncoder passwordEncoder,
                                     PMBUserDetailsService pmbUserDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.pmbUserDetailsService = pmbUserDetailsService;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //pages et éléments ne nécessitant pas d'être connecté pour y accéder
                .authorizeRequests()
                .antMatchers("/",
                        "/login",
                        "/registerUser",
                        "/resetPassword",
                        "/underConstruction",
                        "/img/*",
                        "/css/*")
                .permitAll()
                .anyRequest().authenticated()

                //le formulaire de login est /login et une fois logué l'utilisateur est redirigé vers /homeUser
                .and().formLogin()
                .loginPage("/login").permitAll()
                .usernameParameter("email")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")

                //si la case remember-me est cochée l'utilisateur reste connecté pour une duré d'1j
                .and().rememberMe().tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(1))

                // à la déconnexion l'utilisateur est redirigé vers la page de login si tout s'est bien passé
                // + fermeture session, suppression cookies, etc

                .and().logout()
                .logoutUrl("/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .logoutSuccessUrl("/login");
    }


    /**
     * Prépare authenticationProvider en positionnant:
     * le service qui récupère l'utilisateur dans la DB
     * et l'encodeur de mot de passe
     *
     * @return un DaoAuthenticationProvider avec l'encodeur de mdp et le service d'accès à l'utilisateur
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(pmbUserDetailsService);
        return provider;
    }

}
