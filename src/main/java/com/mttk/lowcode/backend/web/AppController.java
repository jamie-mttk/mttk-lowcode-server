package com.mttk.lowcode.backend.web;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.result.DeleteResult;
import com.mttk.lowcode.backend.web.util.AbstractPersistentController;

@RestController
@RequestMapping("/app")
public class AppController extends AbstractPersistentController{
	@Override
	protected String getColName() {
		return "userApp";
	}
	@PostMapping(value = "/deploy")
	//Deploy a single page
	//deploy success, return {"result":true,list:[]}
	//deploy fail,return {"result":false,"list":[{"type":"menu/page","id":"menu or page id","msg":"Error message"}]}
	public ResponseEntity<Document> deploy(String id) throws Exception {
		//List<Document> errorList=new ArrayList<>();
		//
		Query query=new Query(Criteria.where("app").is(id));
		//remove all menus of the app
		template.remove(query, "userMenuDeploy");
		//Find and deploy all the menus
		List<Document> menuList=template.find(query, Document.class, "userMenu");
		for(Document doc:menuList) {
			template.save(doc, "userMenuDeploy");
		}
		//remove all pages
		template.remove(query, "userPageDeploy");
		//Find and deploy all the pages
		List<Document> pageList=template.find(query, Document.class, "userPage");
		for(Document doc:pageList) {
			template.save(doc, "userPageDeploy");
		}
		//
		return ResponseEntity.ok(new Document("result", true));
		
	}
	
	//delete all the pages and menus include deployed or not deployed
	@Override
	@PostMapping(value = "/delete")
	public ResponseEntity<Document> delete(String id) throws Exception {
		Criteria criteria = Criteria.where("app").is(id);
		Query query=new Query(criteria);
		//menus
		template.remove(query, "userMenu");
		//menus deployed
		template.remove(query, "userMenuDeploy");
		//pages
		template.remove(query, "userPage");
		//pages deployed
		template.remove(query, "userPageDeploy");
		//
		return super.delete(id);
	}
}
