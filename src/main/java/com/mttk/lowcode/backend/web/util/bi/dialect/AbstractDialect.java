package com.mttk.lowcode.backend.web.util.bi.dialect;

//
public class AbstractDialect {

	//Build pagination SQL
	public  String getPageSql(String sql, Page page){
		return null;
	}
	
	public DateTimeFormat getDateTimeFormat() {
		return null;
	}
}
