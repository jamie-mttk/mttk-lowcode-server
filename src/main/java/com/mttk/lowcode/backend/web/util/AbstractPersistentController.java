package com.mttk.lowcode.backend.web.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mongodb.client.result.DeleteResult;
import com.mttk.lowcode.backend.web.util.auth.SecurityContext;

import jakarta.servlet.http.HttpServletRequest;

public abstract class AbstractPersistentController {
	@Autowired
	protected MongoTemplate template;

	//
	protected abstract String getColName();

	// Parse criteria from request,return null if no criteria is found
	protected Criteria parseCriteria(HttpServletRequest request) throws Exception {
		List<Criteria> criterias = new ArrayList<>();
		if (request != null) {
			Enumeration<String> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				// Here the return value of getParameter is always String
				// So it is a bug if the value in fact is a numeric
				criterias.add(Criteria.where(name).is(request.getParameter(name)));
			}
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
	protected void preQuery(List<AggregationOperation> aggregations) throws Exception {
		//
	}

	// Get fields to query
	protected Fields getQueryFields() throws Exception {
		return null;
	}

	// You can do what you want to do with result
	protected void postQuery(List<Document> result) throws Exception {

	}

	@GetMapping(value = "/load")
	public ResponseEntity<Document> load(String id) throws Exception {

		//
		Document result = template.findById(id, Document.class, getColName());
		if (result == null) {
			return ResponseEntity.ok(new Document());
		} else {
			return ResponseEntity.ok(result);
		}
	}

	@PostMapping(value = "/save")
	public ResponseEntity<Document> save(@RequestBody Document body) throws Exception {

		//
		preSave(body);
		//
		Document result = template.save(body, getColName());
		return ResponseEntity.ok(result);
	}

	@PostMapping(value = "/delete")
	public ResponseEntity<Document> delete(String id) throws Exception {

		//
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
		preQuery(aggregations);
		// This is to avoid empty aggregation error
		aggregations.add(Aggregation.limit(Long.MAX_VALUE));
		List<Document> result = template
				.aggregate(Aggregation.newAggregation(aggregations), getColName(), Document.class).getMappedResults();

		//
		postQuery(result);
		//
		return ResponseEntity.ok(new Document("list", result));
	}

	@PostMapping(value = "/copy")
	public ResponseEntity<Document> copy(String id) throws Exception {

		ResponseEntity<Document> result = load(id);
		if (result.getStatusCode().isError()) {
			return result;
		}

		// remove _id
		result.getBody().remove("_id");
		result.getBody().append("name", "Copy of " + result.getBody().getString("name"));
		//
		return save(result.getBody());
	}

	//

	protected void preSave(Document body) {
		//
		Document loginInfo = SecurityContext.getCurrentContext().getAuthentication();
		//
		if (!body.containsKey("_insertTime")) {
			body.append("_insertTime", new Date());
			if (loginInfo != null) {
				body.append("_insertBy", MongoUtil.getId(loginInfo));
			}
		}
		body.append("_updateTime", new Date());
		if (loginInfo != null) {
			body.append("_updateBy", MongoUtil.getId(loginInfo));
		}
	}

}