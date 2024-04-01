package com.mttk.lowcode.backend.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAuthController;

@RestController
@RequestMapping("/pageDeploy")
public class PageDeployController extends AbstractPersistentWithAuthController {
	@Override
	protected String getColName() {
		return "pageDeploy";
	}

	
}
