package com.mttk.lowcode.backend.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.MongoUtil;

//API to show 
@RestController
@RequestMapping("/deployed")
public class DeployedController {
	@Autowired
	protected MongoTemplate template;
	
	//Return all the menus and pages
	//Later only the authorized will be returned
	@GetMapping(value = "/menus")
	public ResponseEntity<Document> menus(String id) throws Exception {
		//All menus
		List<Document> menus = template.find(new Query(Criteria.where("app").is(id)).with(Sort.by(Direction.ASC, "sequence")),Document.class, "userMenuDeploy");
		//All pages - use aggregate to 
		List<Document> pages=findPages(id);
//		System.out.println(menus);
//		System.out.println(pages);
		//
		List<Document> list=new ArrayList<>(menus.size());
		for(Document menu:menus) {
			List<Document> filtered=filterPages(pages,MongoUtil.getId(menu));
		
			if(filtered.size()==0) {
				//If no page under menu,ignore
				continue;
			}
			//Here, we reuse menu document
			menu.append("pages", filtered);
			list.add(menu);
		}
		//
		return ResponseEntity.ok(new Document("list", list));
	}
	
	//Load a deployed page
	//Please note,so far the page id and deployed page id is same
	@GetMapping(value = "/load")
	public ResponseEntity<Document> load(String id) throws Exception {

		Document result = template.findById(id, Document.class, "userPageDeploy");
		if (result == null) {
			return ResponseEntity.ok(new Document());
		} else {
			return ResponseEntity.ok(result);
		}
	}
	//Find all page of the given app
	private List<Document> findPages(String id){
		List<AggregationOperation> aggregations = new ArrayList<>();
		//
		aggregations.add(Aggregation.match(Criteria.where("app").is(id)));
		//Project fields
		aggregations.add(Aggregation.project(Fields.fields("_id","name","description","menu","updateTime","sequence","icon")));
		// Sort
		aggregations.add(Aggregation.sort(Direction.ASC, "sequence"));
		//
		AggregationResults<Document> out = template.aggregate(Aggregation.newAggregation(aggregations), "userPageDeploy",
				Document.class);
		//
		List<Document> result = new ArrayList<>();
		for (Iterator<Document> iterator = out.iterator(); iterator.hasNext();) {
			result.add(iterator.next());
		}
		//
		return result;
	}
	//filter pages by menu
	private List<Document> filterPages(List<Document> pages,String menu){
		return pages.stream().filter(page->menu.equals(page.getString("menu"))).collect(Collectors.toList());
	}
}
