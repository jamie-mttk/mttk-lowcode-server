package com.mttk.lowcode.backend.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAuthController;
import com.mttk.lowcode.backend.web.util.auth.DataAuthUtil;
import com.mttk.lowcode.backend.web.util.auth.SecurityContext;
import com.mttk.lowcode.backend.web.util.init.InitUtil;

import jakarta.servlet.http.HttpServletRequest;

//@RestController
@RequestMapping("/test")
public class TestController extends AbstractPersistentWithAuthController {
	@Override
	protected String getColName() {
		return "app";
	}

	@Autowired
	protected MongoTemplate template;

	@GetMapping(value = "/test1")
	public ResponseEntity<Document> test1(HttpServletRequest request, Pageable pageable) throws Exception {
		Document loginInfo = SecurityContext.getCurrentContext().getAuthentication();
		if (loginInfo == null) {
			// If not login,return empty list
			// normally it will not reach here
			return ResponseEntity.ok(new Document("list", Arrays.asList()));
		}
		//
		List<AggregationOperation> aggregations = new ArrayList<>();
		Criteria criteria = parseCriteria(request);
		if (criteria != null) {
			aggregations.add(Aggregation.match(criteria));
		}
		Fields fields = getQueryFields();
		if (fields != null) {
			throw new Exception("ERROR HAPPEND");
		}
		if (fields != null && fields.size() != 0) {
			aggregations.add(Aggregation.project(fields));
		}
		//
		preQuery(aggregations);
		//
		List<Document> result = new DataAuthUtil().query(template, getColName(), aggregations, null, loginInfo,
				pageable, true);
		//
		postQuery(result);
		//
		return ResponseEntity.ok(new Document("list", result));
	}

	@GetMapping(value = "/test2")
	public ResponseEntity<Document> test2() throws Exception {
		
		InitUtil.init(template);
		//
		return ResponseEntity.ok(new Document("ok", true));
	}

	
}
