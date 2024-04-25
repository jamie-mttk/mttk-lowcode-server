package com.mttk.lowcode.backend.web.util.bi.dialect.impl;


import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DateTimeFormat;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

public class PgSqlDialect extends AbstractDialect {
    @Override
    public String getPageSql(String sql, Page page) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 32);
        sqlBuilder.append(sql).append(" OFFSET "+page.getOffset()+" LIMIT "+page.getSize()+" ");      
        return sqlBuilder.toString();
    }

    public DateTimeFormat getDateTimeFormat(){
        return new DateTimeFormat() {
            public String convert_y(String field) {
                return " EXTRACT(YEAR FROM"+field+") ";
            }
            public String convert_yq(String field) {
                return " concat(EXTRACT(YEAR FROM "+ field +") ,'Q', EXTRACT(QUARTER FROM "+field+")) ";
            }
            public String convert_ym(String field) {
                return " TO_CHAR("+field+", 'YYYY-MM') ";
            }
            public String convert_ymd(String field) {
                return " TO_CHAR("+field+", 'YYYY-MM-DD') ";
            }
            public String convert_h(String field) {
                return " EXTRACT(HOUR FROM "+ field +") ";
            }
            public String convert_hm(String field) {
                return " TO_CHAR("+field+", 'HH24:MI') ";
            }
        };
    }
}
