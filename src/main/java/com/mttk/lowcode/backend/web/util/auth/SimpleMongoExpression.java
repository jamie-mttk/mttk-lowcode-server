package com.mttk.lowcode.backend.web.util.auth;

import org.bson.Document;
import org.springframework.data.mongodb.MongoExpression;

public class SimpleMongoExpression implements MongoExpression {
	private Document doc;

	public SimpleMongoExpression(Document doc) {
		this.doc = doc;
	}

	@Override
	public Document toDocument() {

		return doc;
	}

	@Override
	public String toString() {
		return doc == null ? "" : doc.toJson();
	}
}
