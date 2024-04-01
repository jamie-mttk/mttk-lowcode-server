package com.mttk.lowcode.backend.web.util;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mongodb.client.result.DeleteResult;
import com.mttk.lowcode.backend.web.util.auth.DataAuthUtil;
import com.mttk.lowcode.backend.web.util.auth.EnviromentUtil;
import com.mttk.lowcode.backend.web.util.auth.SecurityContext;

import jakarta.servlet.http.HttpServletRequest;

//Check resource access 
public abstract class AbstractPersistentWithAuthController extends AbstractPersistentController {
	protected static Fields FIELDS_APPEND = Fields.fields("_operationsAll","_owners","_authorities");
	@Autowired
	private Environment environment;

	@Override
	public ResponseEntity<Document> load(String id) throws Exception {
		if (!checkDataAuthSingle(null, id, "access")) {
			return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
		}
		//
		return super.load(id);
	}

	@Override
	public ResponseEntity<Document> save(@RequestBody Document body) throws Exception {
		if (StringUtil.isEmpty(MongoUtil.getId(body))) {
			// This is add
			if (!checkAuth(null, "add")) {
				return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
			}
		} else {
			// This is edit
			if (!checkDataAuthSingle(null, MongoUtil.getId(body), "edit")) {
				return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
			}
		}

		//
		return super.save(body);
	}

	@Override
	public ResponseEntity<Document> delete(String id) throws Exception {
		if (!checkDataAuthSingle(null, id, "del")) {
			return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
		}
		//
		return super.delete(id);
	}

	@PostMapping(value = "/saveDataAuth")
	public ResponseEntity<Document> saveDataAuth(@RequestBody Document body) throws Exception {
		//
		if (!checkDataAuthSingle(null, MongoUtil.getId(body), "auth")) {
			return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
		}
		//
		Document existDoc=template.findById(MongoUtil.getId(body),Document.class, getColName());
		if(existDoc==null) {
			return ResponseEntity.status(HttpStatusCode.valueOf(404)).build();
		}
		//
		existDoc.append("_owners", body.get("_owners"));
		existDoc.append("_authorities", body.get("_authorities"));
		//
		Document result = template.save(existDoc, getColName());
		return ResponseEntity.ok(result);
	}
	
	@Override
	public ResponseEntity<Document> query(HttpServletRequest request) throws Exception {
		Document loginInfo = SecurityContext.getCurrentContext().getAuthentication();
		boolean suppressData = EnviromentUtil.getSuppressData(environment);
		if (loginInfo == null && !suppressData) {
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
		//
		List<AggregationOperation> aggregationsPost = null;
		Fields fields = getQueryFields();
		if (fields != null && fields.size() != 0) {
			aggregationsPost = new ArrayList<>();
			aggregationsPost.add(Aggregation.project(fields));
		}
		//
		preQuery(aggregations);
		//
		List<Document> result = new DataAuthUtil().query(template, getColName(), aggregations, aggregationsPost,
				loginInfo, null, suppressData);
		//
		postQuery(result);
		//
		return ResponseEntity.ok(new Document("list", result));
	}

	// check auth
	protected boolean checkAuth(String resource, String operation) {
		if (StringUtil.isEmpty(resource)) {
			resource = getColName();
		}
		if (EnviromentUtil.getSuppressAuth(environment)) {
			// If auth is suppressed, return true directly
			return true;
		}
		Document loginInfo = SecurityContext.getCurrentContext().getAuthentication();
		if (loginInfo == null) {
			// user is not login but suppress auth is false
			return false;
		}
		List<String> operations = loginInfo.get("authorities", new Document()).getList(resource, String.class);
		if (operations == null || operations.size() == 0) {
			return false;
		}
		if (operation == null) {
			// means any auth is OK
			return true;
		}
		//
		return operations.contains(operation);

	}

	/**
	 * Check data auth for single
	 * 
	 * @param resource  resource name,same as collection name
	 * @param id        id of the resource
	 * @param operation operation to check,null means any operation is OK
	 * @return
	 */
	protected boolean checkDataAuthSingle(String resource, String id, String operation) {
		if (StringUtil.isEmpty(resource)) {
			resource = getColName();
		}
		if (StringUtil.isEmpty(id)) {
			return true;
		}
		if (EnviromentUtil.getSuppressData(environment)) {
			// If data auth is suppressed, return true directly
			return true;
		}
		Document loginInfo = SecurityContext.getCurrentContext().getAuthentication();
		if (loginInfo == null) {
			// Normally it is because suppress auth is set to true
			return true;
		}
		// Try to find the record first
		List<AggregationOperation> aggregations = new ArrayList<>();
		aggregations.add(Aggregation.match(Criteria.where("_id").is(id)));
		List<Document> result = new DataAuthUtil().query(template, resource, aggregations, null, loginInfo, null,
				false);
		if (result.size() == 0) {
			// Not found, that means it is not authorized
			return false;
		}
		//
		List<String> _operationsAll = result.get(0).getList("_operationsAll", String.class);
		if (_operationsAll == null) {
			// Normally it will NOT come here
			return false;
		}
		//
		if (StringUtil.isEmpty(operation)) {
			return _operationsAll.size() > 0;
		} else {
			return _operationsAll.contains(operation);
		}
	}

	@Override
	protected void preSave(Document body) {
		//
		if (StringUtil.isEmpty(MongoUtil.getId(body))) {
			List<String> owners = new ArrayList<>();
			body.append("_owners", owners);
			Document ownerGroup=new Document("type","ownerGroup").append("operations", Arrays.asList("access","edit","del"));
			body.append("_authorities", Arrays.asList(ownerGroup));
			// Add
			Document loginInfo = SecurityContext.getCurrentContext().getAuthentication();
			if (loginInfo != null) {
				owners.add(MongoUtil.getId(loginInfo));
			}
		} else {
			// Edit
			body.remove("_operationsAll");
			//
			// save can not change the _owners and _authorities,so copy from existing one
			Document existDoc=template.findById(MongoUtil.getId(body),Document.class, getColName());
			if(existDoc!=null) {
				body.append("_owners", existDoc.get("_owners"));
				body.append("_authorities", existDoc.get("_authorities"));
			}
		}
		//
		super.preSave(body);

	}
}
