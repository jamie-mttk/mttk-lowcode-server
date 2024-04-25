package com.mttk.lowcode.backend.web.util.bi.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DateTimeFormat;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

public class Db2Dialect extends AbstractDialect {
	 @Override
	    public String getPageSql(String sql, Page page) {
	        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 140);
	        sqlBuilder.append("SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( ");
	        sqlBuilder.append(sql);
	        sqlBuilder.append(" ) AS TMP_PAGE) TMP_PAGE WHERE ROW_ID BETWEEN "+(page.getOffset()+1)+" AND "
	        +(page.getOffset()+page.getSize()));
	        return sqlBuilder.toString();
	    }

	@Override
	public DateTimeFormat getDateTimeFormat() {

		return new DateTimeFormat() {
			@Override
			public String convert_y(String field) {
				return " YEAR("+field+") ";
			}

			@Override
			public String convert_yq(String field) {
				return " CONCAT(VARCHAR_FORMAT("+field+", 'YYYY'),'Q',VARCHAR_FORMAT("+field+", 'Q'))  ";
			}

			@Override
			public String convert_ym(String field) {
				return " VARCHAR_FORMAT("+field+", 'YYYYMM') ";
			}

			@Override
			public String convert_ymd(String field) {
				return " VARCHAR_FORMAT("+field+", 'YYYYMMDD') ";
			}

			@Override
			public String convert_h(String field) {
				return " HOUR("+field+") ";
			}

			@Override
			public String convert_hm(String field) {
				return " VARCHAR_FORMAT("+field+", 'HH24:MI') ";
			}
		};
	}
}
