package com.mttk.lowcode.backend.web.util.auth;

public interface PasswordEncoder {


	String encode(CharSequence rawPassword);


	boolean matches(CharSequence rawPassword, String encodedPassword);

}
