package com.mttk.lowcode.backend.web.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import jakarta.servlet.http.HttpServletRequest;

public class MaxSequenceUtil {

	
	public static ResponseEntity<Document> maxSequence(HttpServletRequest request,MongoTemplate template,String colName) throws Exception {
		String app=request.getParameter("app");
		Assert.notNull(app,"app is empty");
		//
		List<AggregationOperation> aggregations = new ArrayList<>();
		aggregations.add(Aggregation.match(Criteria.where("app").is(app)));
		//
		aggregations.add(Aggregation.group("app").max("sequence").as("sequenceMax"));
		//
		aggregations.add(Aggregation.limit(1));
		AggregationResults<Document> out = template.aggregate(Aggregation.newAggregation(aggregations), colName,
				Document.class);
		//
		Document result= null;
		Iterator<Document> iterator=out.iterator();
		if(iterator.hasNext()) {
			result=out.iterator().next();
		}else {
			result=new Document("_id",app).append("sequenceMax", 0);
		}

		//
		return ResponseEntity.ok(result);
	}
}
