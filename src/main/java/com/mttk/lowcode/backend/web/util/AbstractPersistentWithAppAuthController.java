package com.mttk.lowcode.backend.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mongodb.client.AggregateIterable;
import com.mttk.lowcode.backend.web.AppController;

import jakarta.servlet.http.HttpServletRequest;

//Check the user has authorized to access the app, the data content should have a field named app
//For example user A can access all the data models(dataModel:all ) but can only access app A
//So it should be avoid user to access data model under app B,C,D
public abstract class AbstractPersistentWithAppAuthController extends AbstractPersistentWithAuthController {

	@Autowired
	private AppController appController;

	@Override
	public ResponseEntity<Document> load(String id) throws Exception {
		ResponseEntity<Document> result = super.load(id);
		if (canAccessApp(result.getBody())) {
			return result;
		} else {
			return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
		}
	}

	@Override
	public ResponseEntity<Document> save(@RequestBody Document body) throws Exception {
		if (canAccessApp(body)) {
			//
			if (!StringUtil.isEmpty(MongoUtil.getId(body))) {
				// If it is update, we need to check app is not changed
				// to avoid user to update resource from old app(not authorized) to new app
				// (authorized)
				Document docOld = template.findById(MongoUtil.getId(body), Document.class, getColName());
				if (docOld != null) {
					String oldApp = docOld.getString("app");
					String newApp = body.getString("app");
					if (StringUtil.notEmpty(oldApp) && StringUtil.notEmpty(newApp) && !oldApp.equals(newApp)) {
						throw new Exception("app id can not be changed!");
					}
				}
			}
			return super.save(body);
		} else {
			return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
		}

	}

	@Override
	public ResponseEntity<Document> delete(String id) throws Exception {
		// load first
		Document result = template.findById(id, Document.class, getColName());
		if (result == null) {
			// not found, return negative result
			return ResponseEntity.ok(new Document("result", 0));
		}
		//
		if (canAccessApp(result)) {
			return super.delete(id);
		} else {
			return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
		}
	}

	// Query is a little different by adding a match
	// public ResponseEntity<Document> query(HttpServletRequest request) throws
	// Exception {
	@Override
	protected void preQuery(List<AggregationOperation> aggregations) throws Exception {
		// Find all the authorized
		ResponseEntity<Document> result = appController.query(null);
		if (!result.getStatusCode().is2xxSuccessful()) {
			// user has no access to app? add 1=2 to avoid data to be loaded
			aggregations.add(Aggregation.match(Criteria.where("1").is("2")));
		} else {
			List<Document> appDocs = result.getBody().getList("list", Document.class, Arrays.asList());
			List<String> apps = new ArrayList<>(appDocs.size());
			for (Document d : appDocs) {
				apps.add(MongoUtil.getId(d));
			}
			aggregations.add(Aggregation.match(Criteria.where("app").in(apps)));
		}
	}

	// Whether account can access the given record
	protected boolean canAccessApp(String resource,String id) {
		Document doc=template.findById(id, Document.class, StringUtil.isEmpty(resource)?getColName():resource);
		if(doc==null) {
			return false;
		}
		return canAccessApp(doc);
	}
	protected boolean canAccessApp(Document doc) {
		String app = doc.getString("app");
		if (StringUtil.isEmpty(app)) {
			// Do not have app field,return false directly
			return false;
		}
		//
		return this.checkDataAuthSingle("app", app, null);
	}
}
