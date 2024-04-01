package com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.pagination.Page;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.AbstractDialect;

public class SqlServer2012Dialect extends AbstractDialect {
	@Override
    public String getPageSql(String sql, Page page) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 64);
        sqlBuilder.append(sql);
        sqlBuilder.append(" OFFSET "+page.getOffset()+" ROWS FETCH NEXT "+page.getSize()+" ROWS ONLY ");
      
        return sqlBuilder.toString();
    }

}
