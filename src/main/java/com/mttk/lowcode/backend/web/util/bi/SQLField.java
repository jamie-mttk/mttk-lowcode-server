package com.mttk.lowcode.backend.web.util.bi;

import java.sql.Connection;

import org.bson.Document;

import com.mttk.lowcode.backend.web.util.StringUtil;
import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DialectBuilder;

//A parsed SQL field
public class SQLField {
	//
	private DataModelWrap dataModelWrap;
	// The key of dimension or metric key
	private String section;
	// Field config in report
	private Document config;
	// Column definition in data model
	private Document define;
	//
	private Connection connection;
	//
	public SQLField(DataModelWrap dataModelWrap,Document config, String section,Connection connection) {
		this.dataModelWrap=dataModelWrap;
		this.config = config;
		this.section = section;
		this.connection=connection;
		//column definie in data model
		this.define=dataModelWrap.findColumn(config.getString("key"));
		
	}

	public String getSection() {
		return section;
	}

	public Document getConfig() {
		return config;
	}

	public Document getDefine() {
		return define;
	}



	// SELECT ALIAS
	public String getAlias() {
		// key,get directly
		return config.getString("id");
	}

	//
	public String getExpression() throws Exception{
		String expression = null;
		if ("expression".equals(define.getString("type"))) {
			// Expression type field, get expression directly
			expression = define.getString("expression");
		} else if ("field".equals(define.getString("type"))) {
			Document entity=dataModelWrap.findEntity(define.getString("entity"));

			expression = entity.getString("alias") + "." + define.getString("column");
		} else {
			throw new RuntimeException("Unknown field type:" + define.getString("type"));
		}
		//
		expression=tryFormat(expression);
		//
		return tryAggregation(expression);
	}
	//So far only need to generate datetime related SQL
	private String tryFormat(String expression) throws Exception{
		String _format_datetime=config.getString("_format_datetime");
		if(StringUtil.isEmpty(_format_datetime)||"none".equals(_format_datetime)) {
			//if not set,do no create dialect since creating dialect may cause error
			return expression;
		}
		//
		AbstractDialect dialet = new DialectBuilder().findDialect(connection);
		//
		return dialet.getDateTimeFormat().convert(expression, _format_datetime);
	}
	
	// Try possible aggregation
	private String tryAggregation(String expression) {

		//
		String _aggregation = config.getString("_aggregation");
		String _aggregation_customize = config.getString("_aggregation_customize");
		if (StringUtil.isEmpty(_aggregation) || "_NONE".equals(_aggregation)) {
			// No aggregation
			return expression;
		} else if (!_aggregation.startsWith("_")) {
			return _aggregation + "(" + expression + ")";
		} else if ("_CUSTOMIZE".equals(_aggregation)) {
			if (StringUtil.isEmpty(_aggregation_customize)) {
				// Here is to avoid exception raising
				return expression;
			} else {
				return _aggregation_customize;
			}
		} else if ("_COUNT_DISTINCT".equals(_aggregation)) {
			return "COUNT(DISTINCT " + expression + ")";

		} else {
			throw new RuntimeException("Unknown aggregation:" + _aggregation);
		}

	}

	@Override
	public String toString() {
		try {
		return "SQLField [getAlias()=" + getAlias() + ", getExpression()=" + getExpression() + "]";
		}catch(Exception e) {
			return super.toString();
		}
	}

}
