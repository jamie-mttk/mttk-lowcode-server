package com.mttk.lowcode.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mttk.lowcode.backend.web.util.init.InitUtil;

import jakarta.annotation.PostConstruct;

@Component
public class StartInit {
	@Autowired MongoTemplate template;
	 @PostConstruct
	    public void init() throws Exception {
//		 System.out.println("Start init!!!");
		 InitUtil.init(template);
	 }
}
