package com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl;


import com.mttk.lowcode.backend.web.util.bi.pagination.Page;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.AbstractDialect;

public class PgSqlDialect extends AbstractDialect {
    @Override
    public String getPageSql(String sql, Page page) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 32);
        sqlBuilder.append(sql).append(" OFFSET "+page.getOffset()+" LIMIT "+page.getSize()+" ");      
        return sqlBuilder.toString();
    }
}
