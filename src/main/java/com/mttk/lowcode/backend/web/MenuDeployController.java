package com.mttk.lowcode.backend.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentController;

@RestController
@RequestMapping("/menuDeploy")
public class MenuDeployController extends AbstractPersistentController {
	@Override
	protected String getColName() {
		return "userMenuDeploy";
	}

	
}
