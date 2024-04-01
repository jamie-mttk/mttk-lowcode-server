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

import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAuthController;
import com.mttk.lowcode.backend.web.util.MaxSequenceUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/menu")
public class MenuController extends AbstractPersistentWithAuthController{
	@Override
	protected String getColName() {
		return "menu";
	}
	@Override
	protected void preQuery(List<AggregationOperation> aggregations) {
		//Sort
		aggregations.add(Aggregation.sort(Direction.ASC,"sequence"));
	}
	@Override
	@PostMapping(value = "/delete")
	public ResponseEntity<Document> delete(String id) throws Exception {
		ResponseEntity<Document> result=super.delete(id);
		if(result.getStatusCode().isError()) {
			return result;
		}
		//Find and unlink all the pages
		List<Document> pages=template.find(new Query(Criteria.where("menu").is(id)), Document.class, "page");
		for(Document page:pages) {
			page.put("menu", "");
			template.save(page, "page");
		}
		//
		return result;
	}

	//Get max sequence of the given app
	@GetMapping(value = "/maxSequence")
	public ResponseEntity<Document> maxSequence(HttpServletRequest request) throws Exception {
		return MaxSequenceUtil.maxSequence(request, template, getColName());
	}

//	@Override
//	protected void postQuery(List<Document> result) {
//		//Add menu name
//		for(Document d:result) {
//			//calculate page count under the menu
//			Criteria criteria = Criteria.where("menu").is(MongoUtil.getId(d));
//			d.append("countPages",template.count(new Query(criteria), "page"));
//		}
//	}

}
