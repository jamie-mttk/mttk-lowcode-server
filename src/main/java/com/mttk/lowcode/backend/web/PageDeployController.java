package com.mttk.lowcode.backend.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentController;

@RestController
@RequestMapping("/pageDeploy")
public class PageDeployController extends AbstractPersistentController {
	@Override
	protected String getColName() {
		return "userPageDeploy";
	}

	
}
