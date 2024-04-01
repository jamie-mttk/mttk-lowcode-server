package com.mttk.lowcode.backend.web.util.bi;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.util.Assert;

import com.mttk.lowcode.backend.web.util.StringUtil;
import com.mttk.lowcode.backend.web.util.bi.BiMiscUtil.DUMP_MODE;

//Wrap the request body of BI build method
public class BiBodyWrap {
	private Document body;
	private DataModelWrap dataModelWrap;

	//body config part
	private Document config;
	//combine all the dimensions and metrics
	List<SQLField> dimensions = new ArrayList<>(10);
	List<SQLField> metrics = new ArrayList<>(10);
	
	public BiBodyWrap(Document body,DataModelWrap dataModelWrap,Connection connection) {
		this.body=body;
		this.dataModelWrap=dataModelWrap;
		
		//
		config=body.get("config", Document.class);
		Assert.notNull(config, "config in BI request body is null");
		//

		//
		for (String key : config.keySet()) {
			if (key.startsWith("dimension")) {
				BiMiscUtil.praseFields(dimensions, dataModelWrap, config, key,connection);
			} else if (key.startsWith("metric")) {
				BiMiscUtil.praseFields(metrics, dataModelWrap, config, key,connection);
			}
		}
	}
	//
	public DUMP_MODE getDumpMode() {
		if (StringUtil.notEmpty(config.getString("dumpMode"))) {
			return  DUMP_MODE.valueOf(config.getString("dumpMode"));
		}else {
			return DUMP_MODE.ARRAY;
		}
	}
	//Max rows return,return 0 if not set
	public Integer getRowLimit() {
		return config.getInteger("rowLimit", 0);
	}
	//
	public DataModelWrap getDataModelWrap() {
		return dataModelWrap;
	}
	//
	public List<SQLField> getDimensions() {
		return dimensions;
	}

	public List<SQLField> getMetrics() {
		return metrics;
	}
	//
	public List<Document> getFilters(){
		return body.getList("filters",Document.class,new ArrayList<Document>());
	}
	//
	public List<Document> getSorts(){
		return body.getList("sorts",Document.class,new ArrayList<Document>());
	}
	//Pagination configuration
	public Document getPagination() {
		return config.get("pagination", new Document());
	}
}
