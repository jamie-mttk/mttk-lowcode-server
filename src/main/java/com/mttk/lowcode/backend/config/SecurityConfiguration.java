package com.mttk.lowcode.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mttk.lowcode.backend.web.util.auth.BCryptPasswordEncoder;

@Configuration
public class SecurityConfiguration {

//
//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		http.csrf().disable()
//				// 如果不去掉CSRF会导致POST出现403错误，参考
//				// http://blog.csdn.net/sinat_28454173/article/details/52251004
//
//				.authorizeRequests().anyRequest().fullyAuthenticated().and().formLogin().loginPage("/login")
//				.failureUrl("/login?error").permitAll().and().logout().permitAll();
//		return http.build();
//	}
//
//	@Bean
//	public WebSecurityCustomizer webSecurityCustomizer() {
//		return (web) -> web.ignoring().antMatchers("/resources/**");
//	}


	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	public CustomUserDetailsService customUserDetailsService() {
//		return new CustomUserDetailsService();
//	}
}
