package com.mttk.lowcode.backend.web.util.bi.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DateTimeFormat;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

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

    @Override
    public DateTimeFormat getDateTimeFormat() {

        return new DateTimeFormat() {
            @Override
            public String convert_y(String field) {
                return " YEAR("+field+") ";
            }

            @Override
            public String convert_yq(String field) {
                return " CAST(YEAR("+field+") AS VARCHAR) || 'Q' || ((EXTRACT(MONTH FROM "+field+") - 1) DIV 3) + 1 ";
            }

            @Override
            public String convert_ym(String field) {
                return " TO_CHAR("+field+", 'YYYY-MM') ";
            }

            @Override
            public String convert_ymd(String field) {
                return " "+field+"::DATE ";
            }

            @Override
            public String convert_h(String field) {
                return " EXTRACT(HOUR FROM "+field+") ";
            }

            @Override
            public String convert_hm(String field) {
                return " TO_CHAR("+field+", 'HH24:MI') ";
            }
        };
    }
}
