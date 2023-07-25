package com.mttk.lowcode.backend.web;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentController;
import com.mttk.lowcode.backend.web.util.StringUtil;

@RestController
@RequestMapping("/pageWidget")
public class PageWidgetController extends AbstractPersistentController{
	@Autowired PageController pageController;
	@Override
	protected String getColName() {
		return "userPageWidget";
	}

	
	//Find all the page widgets of the given ids
	//And update the last raw page to it
	
	@PostMapping(value = "/updateRawPage")
	public ResponseEntity<Document> updateRawPage(@RequestBody List<String> ids) throws Exception {
		System.out.println(ids);
	
		List<Document> result=new ArrayList<>(ids.size());
		for(String id:ids) {
			result.add(updateRawPageSingle(id));
		}
		return ResponseEntity.ok(new Document("list",result));
	}

	//
	private Document updateRawPageSingle(String id) throws Exception{
		
		Document r=new Document();
		r.append("id", id);
		r.append("code", 0);
		//Find by id
		Document widget=load(id).getBody();
		if(widget==null) {
			r.append("code", 1);
			return r;
		}
		r.append("name",widget.get("sys", Document.class).get("name"));
		//Page id
		String pageID=widget.get("sys", Document.class).getString("raw_page_id");
		if(StringUtil.isEmpty(pageID)) {
			r.append("code", 2);
			return r;
		}
		//Find raw page
		Document page=pageController.load(pageID).getBody();
		if(page==null) {
			r.append("code", 3);
			return r;
		}
		//
		widget.append("rawPage", page);
		save(widget);
		//
		return r;
	}
}
