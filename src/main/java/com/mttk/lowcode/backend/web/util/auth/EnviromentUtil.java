package com.mttk.lowcode.backend.web.util.auth;

import org.springframework.core.env.Environment;


import com.mttk.lowcode.backend.web.util.StringUtil;

public class EnviromentUtil {

	// 
	//Whether auth is suppressed, for test
	public static boolean getSuppressAuth(Environment environment) {
		String val = environment.getProperty("lowcode.suppress.auth");
		if (StringUtil.isEmpty(val)) {
			return false;
		}
		//
		return "true".equalsIgnoreCase(val);
	}

	// 
	//Whether data auth is suppressed, for test
	public static boolean getSuppressData(Environment environment) {
		String val = environment.getProperty("lowcode.suppress.data");
		if (StringUtil.isEmpty(val)) {
			return false;
		}
		//
		return "true".equalsIgnoreCase(val);
	}

}
