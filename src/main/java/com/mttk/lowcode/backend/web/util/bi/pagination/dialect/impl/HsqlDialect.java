package com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.pagination.Page;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.AbstractDialect;

public class HsqlDialect extends AbstractDialect {

	
    @Override
    public String getPageSql(String sql, Page page) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 20);
        sqlBuilder.append(sql);
        if (page.getSize() > 0) {
            sqlBuilder.append(" LIMIT "+page.getSize()+" ");
        }
        if (page.getOffset() > 0) {
            sqlBuilder.append(" OFFSET "+page.getOffset()+" ");
        }
        return sqlBuilder.toString();
    }
}
