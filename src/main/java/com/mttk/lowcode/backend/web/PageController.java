package com.mttk.lowcode.backend.web;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.result.DeleteResult;
import com.mttk.lowcode.backend.web.util.AbstractPersistentController;
import com.mttk.lowcode.backend.web.util.StringUtil;

@RestController
@RequestMapping("/page")
public class PageController extends AbstractPersistentController {
	@Autowired PageDeployController pageDeployController;
	@Override
	protected String getColName() {
		return "userPage";
	}
	
	@Override
	protected void preQuery(List<AggregationOperation> aggregations) {
		// Sort
		aggregations.add(Aggregation.sort(Direction.ASC, "sequence"));
	}
	@Override
	protected Fields getQueryFields() {
		return Fields.fields("_id","name","description","menu","sequence","icon","_updateTime");
	}
	@Override
	protected void postQuery(List<Document> result) {
		//Add menu name
		for(Document d:result) {
			String menu=d.getString("menu");
			if (StringUtil.notEmpty(menu)) {
				//load 
				Document menuDoc = template.findById(menu, Document.class, "userMenu");
				if(menuDoc!=null) {
					d.append("menu_NAME", menuDoc.getString("name"));
				}
			}
		}
	}
	//
	@PostMapping(value = "/saveInfo")
	public ResponseEntity<Document> saveInfo(@RequestBody Document body) throws Exception {
		//System.out.println(body);
		Document result =null;
		if(body.containsKey("_id")) {
			//this is to update report info
			result=template.findById(body.get("_id"), Document.class, getColName());
			result.put("name", body.getString("name"));
			result.put("description", body.getString("description"));		
			result.put("menu", body.getString("menu"));	
			result.put("icon", body.getString("icon"));	
			result.put("sequence", body.getInteger("sequence",0));	
			result.put("_updateTime", new Date());
			result = template.save(result, getColName());
			//			
			return ResponseEntity.ok(result);
		}else {
			//this is a new report
			return save(body);
		}
	
	}
	
	@GetMapping(value = "/loadByName")
	public ResponseEntity<Document> loadByName(String app,String name) throws Exception {
		
		Criteria criteria = new Criteria().andOperator(Criteria.where("app").is(app),Criteria.where("name").is(name));
		Document result = template.findOne(new Query(criteria), Document.class, getColName());
		if (result == null) {
			return ResponseEntity.ok(new Document());
		} else {
			return ResponseEntity.ok(result);
		}
	}
	
	
	@PostMapping(value = "/copy")
	public ResponseEntity<Document> copy(String id) throws Exception {
	
		Document page = template.findById(id, Document.class, getColName());
		if(page==null) {
			throw new RuntimeException("No page is found by id:"+id);
		}
		//remove _id
		page.remove("_id");
		page.append("name", "Copy of "+page.getString("name"));
		Document result=template.save(page, getColName());

		return ResponseEntity.ok(result);
	}
	//
	//
//	@PostMapping(value = "/deploy")
//	//Deploy a single page
//	//deploy success, return {"result":true}
//	//deploy fail,return {"result":false,"msg":"Fail reason"}
//	public ResponseEntity<Document> deploy(String id) throws Exception {
//		Document doc=load(id).getBody();
//		if(!doc.containsKey("_id")) {
//			//no page is loaded
//			return ResponseEntity.ok(new Document().append("result", false).append("msg", "No page is found by id:"+id));
//		}
//		//Just copy to page deploy collection
//		pageDeployController.save(doc);
//		//
//		return ResponseEntity.ok(new Document("result", true));
//		
//	}
	
}
