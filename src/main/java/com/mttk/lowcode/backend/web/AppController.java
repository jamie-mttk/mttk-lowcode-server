package com.mttk.lowcode.backend.web;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAuthController;

@RestController
@RequestMapping("/app")
public class AppController extends AbstractPersistentWithAuthController {
	@Override
	protected String getColName() {
		return "app";
	}

	@PostMapping(value = "/deploy")
	// Deploy a single page
	// deploy success, return {"result":true,list:[]}
	// deploy fail,return {"result":false,"list":[{"type":"menu/page","id":"menu or
	// page id","msg":"Error message"}]}
	public ResponseEntity<Document> deploy(String id) throws Exception {
		// List<Document> errorList=new ArrayList<>();
		//
		Query query = new Query(Criteria.where("app").is(id));
		// remove all menus of the app
		template.remove(query, "menuDeploy");
		// Find and deploy all the menus
		List<Document> menuList = template.find(query, Document.class, "menu");
		for (Document doc : menuList) {
			template.save(doc, "menuDeploy");
		}
		// remove all pages
		template.remove(query, "pageDeploy");
		// Find and deploy all the pages
		List<Document> pageList = template.find(query, Document.class, "page");
		for (Document doc : pageList) {
			template.save(doc, "pageDeploy");
		}
		//
		return ResponseEntity.ok(new Document("result", true));

	}

	// delete all the pages and menus include deployed or not deployed
	@Override
	@PostMapping(value = "/delete")
	public ResponseEntity<Document> delete(String id) throws Exception {
		ResponseEntity<Document> result=super.delete(id);
		if(result.getStatusCode().isError()) {
			return result;
		}
		Criteria criteria = Criteria.where("app").is(id);
		Query query = new Query(criteria);
		// menus
		template.remove(query, "menu");
		// menus deployed
		template.remove(query, "menuDeploy");
		// pages
		template.remove(query, "page");
		// pages deployed
		template.remove(query, "pageDeploy");
		//
		return result;
	}
}
