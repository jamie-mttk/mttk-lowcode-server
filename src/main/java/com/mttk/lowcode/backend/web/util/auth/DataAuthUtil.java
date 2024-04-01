package com.mttk.lowcode.backend.web.util.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.ConcatArrays;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.In;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Reduce;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Gt;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Cond;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.IfNull;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Switch;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Switch.CaseOperator;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.SetOperators;
import org.springframework.data.mongodb.core.aggregation.SetOperators.SetUnion;
import org.springframework.data.mongodb.core.aggregation.UnsetOperation;
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Let.ExpressionVariable;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.mttk.lowcode.backend.web.util.MongoUtil;

//Data authority related functions
//The logic below is quite complex, refer to dataAuth.js which has the raw mongodb command 
public class DataAuthUtil {
	private final List<String> operationsAll = Arrays.asList("access", "edit", "del", "auth");
	private final List<String> operationsEmpty = Arrays.asList();
	// From parameters
	private MongoTemplate mongoTemplate;
	private String resource;
	private List<AggregationOperation> aggregates;
	private List<AggregationOperation> aggregatesPost;
	private Document loginInfo;
	private Pageable pageable;
	private boolean suppressData;
	//
	private List<String> accountOperations;
	private List<String> accountGroups;

	// Apply data auth related to
	public List<Document> query(MongoTemplate mongoTemplate, String resource, List<AggregationOperation> aggregates,
			List<AggregationOperation> aggregatesPost, Document loginInfo, Pageable pageable, boolean suppressData) {
		this.mongoTemplate = mongoTemplate;
		this.resource = resource;
		this.aggregates = aggregates;
		this.aggregatesPost = aggregatesPost;
		this.loginInfo = loginInfo;
		this.pageable = pageable;
		this.suppressData = suppressData;

		// Only need to check if account does not have full access to resource
		// loginInfo!=null should be used carefully, normally it is only happen if
		// environment lowcode.suppress.data=true
		if (loginInfo != null && !suppressData) {
			//
			accountGroups = loginInfo.getList("groups", String.class, Arrays.asList());
			accountOperations = getAccountOperations();
			if (accountOperations == null || accountOperations.size() == 0) {
				// If the user does not have the operation of the given resource,return empty
				// list
				return Arrays.asList();
			}
			if (!hasAllAuth()) {
				//
				handleOwners();
				handleOwnerGroups();
				handleUserAndGroup();
				handleFinalize();
			} else {
				applyAll(aggregates);
			}
		} else {
			applyAll(aggregates);
		}
		//
		handlePageable();
		//
		if (aggregatesPost != null && aggregatesPost.size() > 0) {
			for (AggregationOperation ao : aggregatesPost) {
				aggregates.add(ao);
			}
		}
		//
		if (aggregates.size() == 0) {
			return mongoTemplate.findAll(Document.class, resource);
		} else {
			Aggregation aggregation = Aggregation.newAggregation(aggregates);

			//
			return mongoTemplate.aggregate(aggregation, resource, Document.class).getMappedResults();
		}
	}

	private void applyAll(List<AggregationOperation> aggregates) {
		// add all to _operationsAll
		aggregates.add(AddFieldsOperation.addField("_operationsAll").withValue(operationsAll).build());
	}

	// Get loginInfo operations of the given resource
	// return null if no operations
	private List<String> getAccountOperations() {
		Document authorities = loginInfo.get("authorities", Document.class);
		if (authorities == null) {
			return null;
		}
		return authorities.getList(resource, String.class);
	}

	// check whether account has all operation for the resource
	private boolean hasAllAuth() {
		if (accountOperations == null || accountOperations.size() == 0) {
			return false;
		}
		//
		return accountOperations.contains("all");
	}

	// Refer to 1.0
	private void handleOwners() {
		IfNull owners = ConditionalOperators.ifNull("$_owners").then(Arrays.asList());
		In in = ArrayOperators.In.arrayOf(owners).containsValue(MongoUtil.getId(loginInfo));
		Cond cond = ConditionalOperators.when(in).then(operationsAll).otherwise(operationsEmpty);
		aggregates.add(AddFieldsOperation.addField("_operationsOwners").withValue(cond).build());
	}

	// Refer to 2.0
	private void handleOwnerGroups() {

		if (accountGroups.size() > 0) {
			// only need to check if user has groups
			handleOwnerGroupsLookup();
			handleOwnerGroupsAddField();
		} else {
			// Set a default value to avoid error
			aggregates.add(
					AddFieldsOperation.addField("_operationsOwnerGroups").withValue(new ArrayList<String>()).build());
		}
	}

	// 2.1
	private void handleOwnerGroupsLookup() {
		AggregationPipeline pipeline = new AggregationPipeline();
		// The below command does not work well which throws a null exception (Maybe it
		// is a spring data bug)
		// SO use SimpleMongoExpression instead
		// ArrayOperators.In.arrayOf("$$my_owners").containsValue(ConvertOperators.ToString.toString("$_id"))

//		IfNull owners=ConditionalOperators.ifNull("$$my_owners").then(Arrays.asList());
//		new Document("$ifNull",Arrays.asList("$$my_owners",Arrays.asList()));
		//
		SimpleMongoExpression inExpression = new SimpleMongoExpression(
				new Document("$in", Arrays.asList(new Document("$toString", "$_id"),
						new Document("$ifNull", Arrays.asList("$$my_owners", Arrays.asList())))));
		pipeline.add(Aggregation.match(Criteria.expr(inExpression)));
		//
		pipeline.add(Aggregation.project("groups", "username").andExclude("_id"));
		// Here we also expose first groups,the reason maybe it is a bug: the next step
		// match does not work well on _id
		pipeline.add(Aggregation.unwind("$groups"));
		pipeline.add(Aggregation.group("$groups").first("groups").as("groups"));
		// { "_id": { "$in": ["65d8089ef164234e3b72b4e6q", "65d8089ef164234e3b72b4e5",
		// "AABBCC"] } }

		//
		pipeline.add(Aggregation.match(Criteria.where("groups").in(accountGroups)));

		//
		LookupOperation lookupOperation = LookupOperation.newLookup().from("account")
				.let(ExpressionVariable.newVariable("my_owners").forField("_owners")).pipeline(pipeline)
				.as("_owner_groups");
		aggregates.add(lookupOperation);
	}

	// 2.2
	private void handleOwnerGroupsAddField() {
		// 2.2.1
		// Get ownerGroup operations from _authorities with type "ownerGroup"
		// So far _operationsOwnerGroups is an array,normally 1 or 0 length
		Filter filter = ArrayOperators.Filter.filter(ConditionalOperators.ifNull("$_authorities").then(Arrays.asList()))
				.as("a").by(ComparisonOperators.Eq.valueOf("$$a.type").equalToValue("ownerGroup"));
		aggregates.add(AddFieldsOperation.addField("_operationsOwnerGroups").withValue(filter).build());
		// 2.2.2
		// If _operationsOwnerGroups is not empty ,get first one;otherwise set an empty
		// list to it
		Gt gt = ComparisonOperators.Gt
				.valueOf(ArrayOperators.Size
						.lengthOfArray(ConditionalOperators.ifNull("$_operationsOwnerGroups").then(Arrays.asList())))
				.greaterThanValue(0);

		Cond cond = ConditionalOperators.when(gt)
				.then(ArrayOperators.ArrayElemAt.arrayOf("$_operationsOwnerGroups").elementAt(0))
				.otherwise(new Document("operations", new ArrayList<String>()));
		aggregates.add(AddFieldsOperation.addField("_operationsOwnerGroups").withValue(cond).build());
		// 2.2.3
		aggregates.add(AddFieldsOperation.addField("_operationsOwnerGroups")
				.withValue("$_operationsOwnerGroups.operations").build());
		// 2.2.4
		Gt hasAuth = ComparisonOperators.Gt
				.valueOf(ArrayOperators.Size
						.lengthOfArray(ConditionalOperators.ifNull("$_owner_groups").then(Arrays.asList())))
				.greaterThanValue(0);
		Cond cond2 = ConditionalOperators.when(hasAuth).then("$_operationsOwnerGroups")
				.otherwise(new ArrayList<String>());
		aggregates.add(AddFieldsOperation.addField("_operationsOwnerGroups").withValue(cond2).build());
		// 2.2.5
		aggregates.add(UnsetOperation.unset("_owner_groups"));
	}

	// 3.0
	private void handleUserAndGroup() {
		handleUserAndGroup1();
		handleUserAndGroup2();
		handleUserAndGroup3();
	}

	// 3.1
	private void handleUserAndGroup1() {
		CaseOperator caseGroup = CaseOperator.when(ComparisonOperators.Eq.valueOf("$$a.type").equalToValue("group"))
				.then(ArrayOperators.In.arrayOf(accountGroups).containsValue("$$a.id"));
		CaseOperator caseUser = CaseOperator.when(ComparisonOperators.Eq.valueOf("$$a.type").equalToValue("user"))
				.then(ComparisonOperators.Eq.valueOf("$$a.id").equalToValue(MongoUtil.getId(loginInfo)));

		Switch sw = ConditionalOperators.switchCases(caseGroup, caseUser).defaultTo(false);

		Filter filter = ArrayOperators.Filter.filter(ConditionalOperators.ifNull("$_authorities").then(Arrays.asList()))
				.as("a").by(sw);

		aggregates.add(AddFieldsOperation.addField("_operationsOther").withValue(filter).build());
	}

	// 3.2
	private void handleUserAndGroup2() {
		ConcatArrays c = ArrayOperators.ConcatArrays.arrayOf("$_operationsOther.operations");
		aggregates.add(AddFieldsOperation.addField("_operationsOther").withValue(c).build());
	}

	// 3.3
	private void handleUserAndGroup3() {
		SetUnion s = SetOperators.SetUnion.arrayAsSet("$$this").union("$$value");
		Reduce r = ArrayOperators.Reduce.arrayOf("$_operationsOther").withInitialValue(new ArrayList<String>())
				.reduce(s);
		aggregates.add(AddFieldsOperation.addField("_operationsOther").withValue(r).build());
	}

	// 4.0
	private void handleFinalize() {
		// 4.1
		SetUnion s = SetOperators.SetUnion.arrayAsSet("$_operationsOwners").union("$_operationsOwnerGroups",
				"$_operationsOther");
		aggregates.add(AddFieldsOperation.addField("_operationsAll").withValue(s).build());
		// 4.2
		Filter filter = ArrayOperators.Filter.filter("$_operationsAll").as("o")
				.by(ArrayOperators.In.arrayOf(accountOperations).containsValue("$$o"));
		aggregates.add(AddFieldsOperation.addField("_operationsAll").withValue(filter).build());
		// 4.3
		aggregates
				.add(AddFieldsOperation.addField("_operationCount")
						.withValue(ArrayOperators.Size
								.lengthOfArray(ConditionalOperators.ifNull("$_operationsAll").then(Arrays.asList())))
						.build());
		// 4.4
		aggregates.add(Aggregation.match(Criteria.where("_operationCount").gt(0)));
		// 4.5
		aggregates.add(UnsetOperation.unset("_operationsOwners", "_operationsOwnerGroups", "_operationsOther",
				"_operationCount"));
	}

	// Handle pageable
	private void handlePageable() {
		if (pageable == null || pageable.isUnpaged()) {
			return;
		}
		//
		if (pageable.getOffset() > 0) {
			aggregates.add(new SkipOperation(pageable.getOffset()));
		}
		if (pageable.getPageSize() > 0) {
			aggregates.add(new LimitOperation(pageable.getPageSize()));
		}

	}
}
