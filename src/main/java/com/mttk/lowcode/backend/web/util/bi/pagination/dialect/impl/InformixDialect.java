package com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.pagination.Page;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.AbstractDialect;

public class InformixDialect extends AbstractDialect {

	 @Override
	    public String getPageSql(String sql, Page page) {
	        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 40);
	        sqlBuilder.append("SELECT ");
	        if (page.getOffset() > 0) {
	            sqlBuilder.append(" SKIP "+page.getOffset()+" ");
	        }
	        if (page.getSize() > 0) {
	            sqlBuilder.append(" FIRST "+page.getSize()+" ");
	        }
	        sqlBuilder.append(" * FROM ( ");
	        sqlBuilder.append(sql);
	        sqlBuilder.append(" ) TEMP_T ");
	        return sqlBuilder.toString();
	    }

}
