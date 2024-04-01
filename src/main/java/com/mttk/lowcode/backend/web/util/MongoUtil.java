package com.mttk.lowcode.backend.web.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.json.Converter;
import org.bson.json.JsonWriterSettings;
import org.bson.json.StrictJsonWriter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

public class MongoUtil {
	/**
	 * 判断两个文档_id是否相等
	 * 
	 * @param document1
	 * @param document2
	 * @return
	 */
	public static boolean isSame(Document document1, Document document2) {
		if (document1 == null || document2 == null) {
			return false;
		}
		return document1.get("_id").equals(document2.get("_id"));
	}

	// 得到主键
	public static String getId(Document document) {
		if (document == null) {
			return null;
		}
		if (document.containsKey("_id")) {
			Object objectId = document.get("_id");
			if (objectId instanceof ObjectId) {
				return ((ObjectId) objectId).toString();
			} else if (objectId instanceof String) {
				return (String) objectId;
			} else {
				return null;
			}

		}
		//
		return null;
	}

	//
	public static JsonWriterSettings createDefaultSettings() {
		JsonWriterSettings settings = JsonWriterSettings.builder().dateTimeConverter(new DateTimeConverter())
				.objectIdConverter(new ObjectIdConverter()).build();
		//
		return settings;
	}

	//
	public static List<Document> executeAggreates(MongoTemplate mongoTemplate, String colName,
			List<AggregationOperation> aggregates) {
		//
		if (aggregates.size() == 0) {
			return mongoTemplate.findAll(Document.class, colName);
		} else {
			Aggregation aggregation = Aggregation.newAggregation(aggregates);

			//
			return mongoTemplate.aggregate(aggregation, colName, Document.class).getMappedResults();
		}
	}
}

//Convert datetime format to yyyy/MM/dd HH:mm:ss or other customized format
class DateTimeConverter implements Converter<Long> {
	private String format;

	public DateTimeConverter() {
		this("yyyy/MM/dd HH:mm:ss");
	}

	public DateTimeConverter(String format) {
		this.format = format;
	}

	public void convert(Long value, StrictJsonWriter writer) {
		Date date = new Date(value);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		writer.writeNumber(sdf.format(date));
	}
}

//把ObjectID转换成字符串
class ObjectIdConverter implements Converter<ObjectId> {
	public void convert(ObjectId value, StrictJsonWriter writer) {
		writer.writeString(value.toString());
	}
}