package com.mttk.lowcode.backend.web.util.auth;

import org.bson.Document;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;

import com.mttk.lowcode.backend.web.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;



public class AccountUtil {
	//Get login token 
	public static String getLoginToken(HttpServletRequest request){
		return request.getHeader("X-Token");
	}
	//Get login info ,return null if not found
	public static Document getLoginInfo(HttpServletRequest request,Cache cache){
		String token=getLoginToken(request);
		if (StringUtil.isEmpty(token)) {
			return null;
		}
		//
		ValueWrapper wrap=cache.get(token);
		if (wrap==null||wrap.get()==null) {
			return null;
		}
		return  Document.parse((String)wrap.get());
	}


	//Get cache
	public static  Cache getCache(CacheManager cacheManager) {
			return cacheManager.getCache("login");
		}
	
}
