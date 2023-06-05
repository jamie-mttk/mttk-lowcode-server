package com.mttk.lowcode.backend.web;

import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentController;
import com.mttk.lowcode.backend.web.util.MongoUtil;

@RestController
@RequestMapping("/menu")
public class MenuController extends AbstractPersistentController{
	@Override
	protected String getColName() {
		return "userMenu";
	}
	@Override
	protected void preQuery(List<AggregationOperation> aggregations) {
		//Sort
				aggregations.add(Aggregation.sort(Direction.ASC,"sequence"));
	}
	@Override
	@PostMapping(value = "/delete")
	public ResponseEntity<Document> delete(String id) throws Exception {
		//Find and unlink all the pages
		List<Document> pages=template.find(new Query(Criteria.where("menu").is(id)), Document.class, "userPage");
		for(Document page:pages) {
			page.put("menu", "");
			template.save(page, "userPage");
		}
		//
		return super.delete(id);
	}


//	@Override
//	protected void postQuery(List<Document> result) {
//		//Add menu name
//		for(Document d:result) {
//			//calculate page count under the menu
//			Criteria criteria = Criteria.where("menu").is(MongoUtil.getId(d));
//			d.append("countPages",template.count(new Query(criteria), "userPage"));
//		}
//	}

}
