package com.mttk.lowcode.backend.web.util.bi.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DateTimeFormat;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

public class SqlServer2012Dialect extends AbstractDialect {
	@Override
    public String getPageSql(String sql, Page page) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 64);
        sqlBuilder.append(sql);
        sqlBuilder.append(" OFFSET "+page.getOffset()+" ROWS FETCH NEXT "+page.getSize()+" ROWS ONLY ");
      
        return sqlBuilder.toString();
    }

    @Override
    public DateTimeFormat getDateTimeFormat() {

        return new DateTimeFormat() {
            public String convert_y(String field) {
                return " DATEPART(year, "+field+") ";
            }
            public String convert_yq(String field) {
                return " CONCAT(CAST(DATEPART(year, "+field+") AS VARCHAR),'Q',CAST(DATEPART(quarter, "+field+") AS VARCHAR)) ";
            }
            public String convert_ym(String field) {
                return " CONCAT(DATEPART(year, "+field+"), '-', RIGHT('0' + CONVERT(VARCHAR, DATEPART(month, "+field+")), 2)) ";
            }
            public String convert_ymd(String field) {
                return " CONVERT(VARCHAR, "+field+", 120) ";
            }
            public String convert_h(String field) {
                return " DATEPART(hour, "+field+") ";
            }
            public String convert_hm(String field) {
                return " CONCAT(RIGHT('0' + CONVERT(VARCHAR, DATEPART(hour, "+field+")), 2), ':', RIGHT('0' + CONVERT(VARCHAR, DATEPART(minute, "+field+")), 2)) ";
            }
        };
    }

}
