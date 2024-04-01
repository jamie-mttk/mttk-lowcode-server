package com.mttk.lowcode.backend.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.UnsetOperation;
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Let.ExpressionVariable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAppAuthController;
import com.mttk.lowcode.backend.web.util.MongoUtil;
import com.mttk.lowcode.backend.web.util.auth.SimpleMongoExpression;
import com.mttk.lowcode.backend.web.util.bi.BiBuildUtil;
import com.mttk.lowcode.backend.web.util.bi.DataModelUtil;

@RestController
@RequestMapping("/bi/dataModel")
public class DataModelController extends AbstractPersistentWithAppAuthController {

	@Override
	protected String getColName() {
		return "dataModel";
	}
	@Override
	protected void preQuery(List<AggregationOperation> aggregations) {
		// Sort
		aggregations.add(Aggregation.sort(Direction.DESC, "_updateTime"));
		//
		handleAddConnection(aggregations);		
	}
	//Add ——connectionName
//	db.dataModel.aggregate([{
//	    $lookup:
//	        {
//	            from: "jdbcConnection",
//	            let: { conn: "$jdbcConnection" },
//	            pipeline: [{
//	                $match:
//	                    {
//	                        $expr: { $eq: [{ $toString: "$_id" }, "$$conn"] },
//
//	                    }
//	            },
//	            { $project: { _id: 0, name: 1 } },
//
//	            ],
//	            as: "_connection"
//	        },
//
//	},
//	{ $unwind: "$_connection" },
//	{
//	    $addFields: {
//	        "_connectionName": "$_connection.name"
//	    }
//	},
//	{ $unset: ["_connection"] }
//	])
	private void handleAddConnection(List<AggregationOperation> aggregations) {
		//1.0 lookup
		AggregationPipeline pipeline = new AggregationPipeline();
		SimpleMongoExpression eqExpression = new SimpleMongoExpression(
				new Document("$eq", Arrays.asList(new Document("$toString", "$_id"),
						"$$conn")));
		pipeline.add(Aggregation.match(Criteria.expr(eqExpression)));
		//
		pipeline.add(Aggregation.project("name").andExclude("_id"));


		//
		LookupOperation lookupOperation = LookupOperation.newLookup().from("jdbcConnection")
				.let(ExpressionVariable.newVariable("conn").forField("jdbcConnection")).pipeline(pipeline)
				.as("_connection");
		aggregations.add(lookupOperation);
		//2.0 unwind
		aggregations.add(Aggregation.unwind("$_connection"));
		//3.0 addfield 
		aggregations.add(AddFieldsOperation.addField("_connectionName").withValue("$_connection.name").build());
		//4.0 unset
		aggregations.add(UnsetOperation.unset("_connection"));
	}
	
	@PostMapping(value = "/loadPreviewData")
	public ResponseEntity<Document> loadPreviewData(Integer maxRows,@RequestBody Document body) throws Exception {
		if(!checkDataAuthSingle(null,MongoUtil.getId(body),null)) {
			return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
		}
		if(!canAccessApp(body)) {
			return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
		}
		//body is data model
//		Document body=load("65d464efd6772f681fe98c3e").getBody();
//		int maxRows=20;
		if(maxRows==null) {
			maxRows=-1;
		}
		
		//Build a dummy body to call the build function
		Document bodyDummyConfig=new Document();
		Document bodyDummy=new Document("config",bodyDummyConfig);
		//
		bodyDummyConfig.append("dumpMode", "JSON");
		List<Document> dimension=new ArrayList<>();
		bodyDummyConfig.append("dimension", dimension);
		//Copy all the model columns to dimension
		for(Document column:body.getList("columns", Document.class)) {
			appendColumn(dimension,column);
			//Check possible children
			if(column.getList("children",Document.class)!=null) {
				for(Document child:column.getList("children", Document.class)) {
					appendColumn(dimension,child);
				}
			}
		}
		//
		if(dimension.size()==0) {
			//If no column,return empty list instead of error
			return  ResponseEntity.ok(new Document("data",new ArrayList<Document>()));
		}
		//Add page to get first n rows
		if(maxRows>0) {
			bodyDummyConfig.append("rowLimit", maxRows);
//		bodyDummyConfig.append("pagination", new Document().append("enabled", true).append("page", 1).append("size",maxRows));
		}
		//
//		System.out.println(bodyDummyConfig.toJson());
		//
		return ResponseEntity.ok(BiBuildUtil.build(template, body, bodyDummy));
	}
	
	
	@PostMapping(value = "/loadEntityFields")//
	public ResponseEntity<Document> loadEntityFields(String model,@RequestBody Document entity) throws Exception {
		//
		Document modelDoc=load(model).getBody();
//		Document entity=model.getList("entities", Document.class).get(2);
		//
		return ResponseEntity.ok(new Document("data", DataModelUtil.loadFieldsInternal(template,modelDoc,entity)));
	
	}

	private void appendColumn(List<Document> dimension,Document column) {
		if("hierarchy".equalsIgnoreCase(column.getString("type"))) {
			return;
		}
		//
		Document d=new Document();
		dimension.add(d);
		//Use key as ID since each column will only apear once
		d.append("id", column.getString("key"));
		//Copy all from column
		for(String k:column.keySet()) {
			d.append(k, column.get(k));
		}
	
	}
	
}
