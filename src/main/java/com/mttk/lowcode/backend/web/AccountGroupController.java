package com.mttk.lowcode.backend.web;

import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAuthController;
import com.mttk.lowcode.backend.web.util.MaxSequenceUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/accountGroup")
public class AccountGroupController extends AbstractPersistentWithAuthController {
	@Override
	protected String getColName() {
		return "accountGroup";
	}

	@Override
	protected void preQuery(List<AggregationOperation> aggregations) {
		// Sort
		aggregations.add(Aggregation.sort(Direction.ASC, "sequence"));
	}

	@Override
	@PostMapping(value = "/delete")
	public ResponseEntity<Document> delete(String id) throws Exception {
		ResponseEntity<Document> result = super.delete(id);
		if (result.getStatusCode().isError()) {
			return result;
		}
		// Delete from all the account
		// please note:where/is can match the groups array which has id
		List<Document> accounts = template.find(new Query(Criteria.where("groups").is(id)), Document.class, "account");
		for (Document account : accounts) {
			List<String> groups = account.getList("groups", String.class);	
			if (groups != null) {
				if (groups.remove(id)) {
					//
					template.save(account, "account");
				}
			}
		}
		//
		return result;
	}

}
