package com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.pagination.Page;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.AbstractDialect;

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
}
