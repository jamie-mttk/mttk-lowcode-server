package com.mttk.lowcode.backend.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentController;
import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAuthController;

@RestController
@RequestMapping("/echartsTheme")
public class EchartsThemeController extends AbstractPersistentController {
	@Override
	protected String getColName() {
		return "echartsTheme";
	}

}
