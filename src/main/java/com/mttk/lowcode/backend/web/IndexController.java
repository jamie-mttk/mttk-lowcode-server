package com.mttk.lowcode.backend.web;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/")
public class IndexController {
	@RequestMapping("/")
	public String index(HttpServletResponse response) throws Exception{
		   response.sendRedirect("/index.html");
		    return null; 
//		System.out.println("@@@@@@@@@@@@");
//		return "forward:index.html";
	}
}
