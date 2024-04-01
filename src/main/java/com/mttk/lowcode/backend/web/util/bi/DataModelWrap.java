package com.mttk.lowcode.backend.web.util.bi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.util.Assert;

//Wrap data model to provide useful methods to access dataModel
public class DataModelWrap {
	private Document dataModel;
	//
	private Map<String, Document> entityMap;
	//
	private Map<String, Document> columnMap;
	
	public DataModelWrap(Document dataModel) {
		this.dataModel=dataModel;
		//
		this.entityMap = parseEntityMap();
		this.columnMap=parseColumnMap();
	}

	//Find entity by key, throw exception if not found
	public Document findEntity(String key) {
		Document result=entityMap.get(key);
		Assert.notNull(result, "No entity is found by "+key);
		return result;				
	}
	public Document findColumn(String key) {
		Document result=columnMap.get(key);
		Assert.notNull(result, "No column is found by "+key);
		return result;				
	}
	public List<Document> getRelations(){
		return dataModel.getList("relations", Document.class);
	}
	//Find the root entity
	//The first entity in entity list is the root entity
	public Document getRootEntity() {
		return dataModel.getList("entities", Document.class).get(0);
	}
	
	/***********************************************************
	 * 
	 * Private functions
	 *
	 ************************************************************/
	//
	//
	private  Map<String,Document> parseEntityMap() {
		Map<String,Document> entityMap=new HashMap<>();
		//
		for( Document  entity:dataModel.getList("entities", Document.class)) {
			entityMap.put(entity.getString("key"), entity);
		}
		//
		return entityMap;
	}
	private  Map<String,Document> parseColumnMap() {
		Map<String,Document> map=new HashMap<>();
		for(Document c:dataModel.getList("columns", Document.class)) {
			map.put(c.getString("key"), c);
			List<Document> children=c.getList("children",Document.class);
			if(children==null||children.size()==0) {
				continue;				
			}
			for(Document child:children) {
				map.put(child.getString("key"), child);
			}
		}
		return map;
		
	}
}
