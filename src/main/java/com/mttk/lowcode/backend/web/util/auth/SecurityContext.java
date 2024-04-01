package com.mttk.lowcode.backend.web.util.auth;

import org.bson.Document;

/**
 
 *
 */
public class SecurityContext {
	protected static Class<? extends SecurityContext> contextClass = SecurityContext.class;
	//
	private Document authentication;
	//
	protected static final ThreadLocal<? extends SecurityContext> threadLocal = new ThreadLocal<SecurityContext>() {
		//
		@Override
		protected SecurityContext initialValue() {
			try {
				return contextClass.newInstance();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	};

	public SecurityContext() {
		super();
	}

	public static SecurityContext getCurrentContext() {
		SecurityContext context = threadLocal.get();
		return context;
	}

	//
	public Document getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Document authentication) {
		this.authentication = authentication;
	}
}
