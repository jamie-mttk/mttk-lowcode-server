package com.mttk.lowcode.backend.config;


import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.mttk.lowcode.backend.web.AccountController;
import com.mttk.lowcode.backend.web.AppController;
import com.mttk.lowcode.backend.web.util.StringUtil;
import com.mttk.lowcode.backend.web.util.auth.AccountUtil;
import com.mttk.lowcode.backend.web.util.auth.EnviromentUtil;
import com.mttk.lowcode.backend.web.util.auth.SecurityContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
	@Autowired
	private Environment environment;
	@Autowired
	private CacheManager cacheManager;
	//test
	@Autowired AccountController accountController;
	// These URLs does not need to check                                               
	private static final String[] ignoreURIs = new String[] { "POST:/account/login", "POST:/account/logout", "GET:/user/info","GET:/echartsTheme/query"};
	// 
//	private Map<String, Set<Role>> uriRoles = null;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
        Document loginInfo = AccountUtil.getLoginInfo(request, AccountUtil.getCache(cacheManager));
        //Below is for quick test,remove in production
//        if(loginInfo==null && "demo".equals(AccountUtil.getLoginToken(request))){
//        	Document d=accountController.loginInternal("jamie","123456").getBody();
//        	loginInfo=accountController.infoInternal(d.getString("token")).getBody();
//        }
		//
//		if (user != null) {
			SecurityContext.getCurrentContext().setAuthentication(loginInfo);
//		}
		//Do not check auth
		if (EnviromentUtil.getSuppressAuth(environment)) {
			return true;
		}
		//
//		init();
		// 
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}
//        if("e10f01b49b90432ca5a85c2ffc1cf777".equals(request.getHeader("X-Token"))){
//		    return true;
//        }
        //
		String uri = request.getServletPath();
		if(uri.startsWith("/assets")) {
			//These are static resource such as JS/CSS
			return true;
		}
		if (StringUtil.isEmpty(uri)) {
			return true;
		}
//		System.out.println(uri);
		String resource = request.getMethod() + ":" + uri;
		// 
//		System.out.println(isIgnoreUri(resource)+"######:"+resource+"@@@"+loginInfo);
		if (isIgnoreUri(resource)) {
			return true;
		}
		
		//
		if (loginInfo == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		//
//		if (!checkUserAccessible(loginInfo, resource)) {
//			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//			return false;
//		}
		//
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// 
		SecurityContext.getCurrentContext().setAuthentication(null);
	}

	// 是否是忽略的URL
	private boolean isIgnoreUri(String uri) {
//		System.out.println(uri);
		for (String s : ignoreURIs) {
			if (s.equalsIgnoreCase(uri)) {
				return true;
			}
		}
		//
		return false;
	}

	// 
	public void reset() {
//		uriRoles = null;
	}


//	private void init() {
//		//
//		if (uriRoles != null) {
//			return;
//		}
//		//
//		Map<String, Set<Role>> map = new HashMap<>();
//		//
//
//		List<Role> roles = roleRepository.findAll();
//		List<Operation> operations = operationRepository.findAll();
//		for (Operation o : operations) {
//			if (o.getOperationUrls() == null) {
//				continue;
//			}
//			//
//			for (OperationUrl u : o.getOperationUrls()) {
//				String key = u.getMethod() + ":" + u.getUrl();
//				Set<Role> s = map.get(key);
//				if (s == null) {
//					s = new HashSet<>();
//					map.put(key, s);
//				}
//
//				// 查找出所有符合条件的role加入到set里
//				for (Role role : roles) {
//					if (roleHasOperation(role, u.getOperation())) {
//						s.add(role);
//					}
//				}
//
//			}
//
//		}
//
//		uriRoles = map;
//	}
//
//	// 判断给定角色是否有指定的操作
//	private boolean roleHasOperation(Role role, Operation operation) {
//		if (role.getOperations() == null) {
//			return false;
//		}
//		for (Operation o : role.getOperations()) {
//			if (o.equals(operation)) {
//				return true;
//			}
//		}
//		//
//		return false;
//	}
//
//	// 判断给定用户是否能访问URI
//	private Boolean checkUserAccessible(User user, String resource) {
//		if (user.getRoles() == null || user.getRoles().size() == 0) {
//			return false;
//		}
//		//
//		Set<Role> roles = uriRoles.get(resource);
//		if (roles == null || roles.size() == 0) {
//			return false;
//		}
//		//
//		for (Role roleUser : user.getRoles()) {
//			for (Role role : roles) {
//				if (roleUser.equals(role)) {
//					// 用户只要有任意一个角色则代表允许访问
//					return true;
//				}
//			}
//		}
//		//
//		return false;
//	}
}
