package com.mttk.lowcode.backend.web.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mongodb.client.result.DeleteResult;

public abstract class AbstractPersistentController {
	@Autowired
	protected MongoTemplate template;

	//
	protected abstract String getColName();

	// Parse criteria from request,return null if no criteria is found
	protected Criteria parseCriteria(HttpServletRequest request) throws Exception {
		List<Criteria> criterias = new ArrayList<>();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			criterias.add(Criteria.where(name).is(request.getParameter(name)));
		}
		//
		if (criterias.size() == 0) {
			return null;
		} else if (criterias.size() == 1) {
			return criterias.get(0);
		} else {
			return new Criteria().andOperator(criterias);
		}
	}
	//
	protected void preQuery(List<AggregationOperation> aggregations) {
		//
	}
	// Get fields to query
	protected Fields getQueryFields() {
		return null;
	}
	//You can do what you want to do with result
	protected void postQuery(List<Document> result) {
		
	}
	@GetMapping(value = "/findAll")
	public ResponseEntity<Document> findAll() throws Exception {
		List<Document> list = template.findAll(Document.class, getColName());
		return ResponseEntity.ok(new Document("list", list));
	}

	@GetMapping(value = "/load")
	public ResponseEntity<Document> load(String id) throws Exception {

		Document result = template.findById(id, Document.class, getColName());
		if (result == null) {
			return ResponseEntity.ok(new Document());
		} else {
			return ResponseEntity.ok(result);
		}
	}

	@PostMapping(value = "/save")
	public ResponseEntity<Document> save(@RequestBody Document body) throws Exception {
		handleUpdateTime(body);
		//
		Document result = template.save(body, getColName());
		return ResponseEntity.ok(result);
	}

	@PostMapping(value = "/delete")
	public ResponseEntity<Document> delete(String id) throws Exception {
		Criteria criteria = Criteria.where("_id").is(id);
		DeleteResult result = template.remove(new Query(criteria), getColName());
		return ResponseEntity.ok(new Document("result", result.getDeletedCount() != 0));
	}

	@GetMapping(value = "/query")
	public ResponseEntity<Document> query(HttpServletRequest request) throws Exception {
		List<AggregationOperation> aggregations = new ArrayList<>();
		Criteria criteria = parseCriteria(request);
		if (criteria != null) {
			aggregations.add(Aggregation.match(criteria));
		}
		Fields fields = getQueryFields();
		if (fields != null && fields.size() != 0) {
			aggregations.add(Aggregation.project(fields));
		}
		//
		preQuery( aggregations);
		// This is to avoid empty aggregation error
		aggregations.add(Aggregation.limit(Long.MAX_VALUE));
		AggregationResults<Document> out = template.aggregate(Aggregation.newAggregation(aggregations), getColName(),
				Document.class);
		//
		List<Document> result = new ArrayList<>();
		for (Iterator<Document> iterator = out.iterator(); iterator.hasNext();) {
			result.add(iterator.next());
		}
		//
		postQuery(result);
		//
		return ResponseEntity.ok(new Document("list", result));
	}
	//
	protected void handleUpdateTime(Document body) {
		//
		if(!body.containsKey("_insertTime")) {
			body.append("_insertTime", new Date());
		}
		body.append("_iupdateTime", new Date());
	}
}
