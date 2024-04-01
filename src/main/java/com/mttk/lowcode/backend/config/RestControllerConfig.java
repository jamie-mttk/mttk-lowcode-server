package com.mttk.lowcode.backend.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

import com.mttk.lowcode.backend.web.util.StringUtil;
import com.mttk.lowcode.backend.web.util.ThrowableUtil;



@Configuration
public class RestControllerConfig {
	@ControllerAdvice
	public class SpringExceptionHandler {
		private Logger logger=LoggerFactory.getLogger(SpringExceptionHandler.class);
	
		@ExceptionHandler(value = { Exception.class })
		public ResponseEntity<Object> handleOtherExceptions(final Exception exception) {
			//exception.printStackTrace();
			//
			Map<String, Object> map = new HashMap<String, Object>();
		  	String code="999";
        	String cause=exception.getClass().getSimpleName();
        	String error=exception.getMessage();
        	if (StringUtil.isEmpty(error)) {
        		error=exception.getClass().toString();
        	}
        	String detail=null;
        	if (exception instanceof HttpStatusCodeException) {
        		HttpStatusCodeException e=(HttpStatusCodeException)exception;
        		code=""+e.getStatusCode().value();
        		detail=e.getResponseBodyAsString();
        	}else {        		
        		detail=ThrowableUtil.dump2String(exception);
        	}
        	
	        //
        	map.put("result", false);
        	map.put("code", code);
        	map.put("cause", cause);
        	map.put("error", error);
        	map.put("detail", detail);
			//
			logger.error("Controller invoke error with code:"+code,exception);
			//
			return new ResponseEntity<Object>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
