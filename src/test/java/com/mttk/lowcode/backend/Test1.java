package com.mttk.lowcode.backend;




import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mttk.lowcode.backend.web.util.auth.DataAuthUtil;

public class Test1 {
	
	public static void main(String[] args) throws Exception{

		try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "lowcode");
			//
			test(mongoTemplate);
			//
		}
	}

	public static void test(MongoTemplate mongoTemplate) {
		 List<AggregationOperation> aggregates=new ArrayList<>();
		//
//		    Criteria docCri = Filters.eq("name", "First APP");
		Criteria criteriaRaw = Criteria.where("name").is("First APP");
	
		Pageable pageable= PageRequest.of(0,10);
		criteriaRaw=null;
		// if criteria is not null,add into aggregate list
		if (criteriaRaw != null) {
			aggregates.add(Aggregation.match(criteriaRaw));
		}
		List<Document> result = new DataAuthUtil().query(mongoTemplate, "app", aggregates,null,loadLoginInfo(mongoTemplate),pageable,false);
		for (Document d : result) {
			System.out.println(d.toJson());
//			System.out.println(d.get("_operationCount"));
//			System.out.println(d.get("_operationsOwnerGroups"));
//			Document ddd=((List<Document>)d.get("_owner_groups")).get(0);
//			System.out.println(ddd.get("_id").getClass());
		}
	}
	
	private static Document loadLoginInfo(MongoTemplate mongoTemplate) {
		//
		String jamie="65d80958f164234e3b72b4e8";
		String sa="65d814a9f164234e3b72b52c";
		String none="65ddba5d66d93728774ae9b1";
	
		Document account=mongoTemplate.findById(jamie, Document.class, "account");
		account.append("authorities", findAuthoritiesByAccount(account,mongoTemplate));
		//
		return account;
	}
	
	
	private static Document findAuthoritiesByAccount(Document account,MongoTemplate mongoTemplate){
		Document authorities=new Document();
		for(String roleId:account.getList("roles", String.class,new ArrayList<>(0))){
			Document role=mongoTemplate.findById(roleId, Document.class, "accountRole");
			if(role==null) {
				continue;
			}
			//
			mergetAuthorities(authorities,role.getList("authorities", Document.class,new ArrayList<>()));
		}
		//
		return authorities;
	}
	private static void mergetAuthorities(Document authorities,List<Document> authoritiesToMerge) {
		for(Document merge:authoritiesToMerge) {
			String module=merge.getString("module");
			List<String> operationsMerge=merge.getList("operations", String.class,new ArrayList<>());
			List<String> operations=authorities.getList(module, String.class);
			if(operations==null) {
				//This module is NOT set before, use the operations under this authority directly
				authorities.append(module,operationsMerge);
			}else {
				//Merge
				mergeSingle(operations,operationsMerge);
			}
		}
	}

	//Merge all operations in toMerge to authority
	private  static void mergeSingle(List<String> operations,List<String> operationsMerge) {

		for(String operation:operationsMerge) {
			if(!operations.contains(operation)) {
				operations.add(operation);
			}
		}
	}
}
