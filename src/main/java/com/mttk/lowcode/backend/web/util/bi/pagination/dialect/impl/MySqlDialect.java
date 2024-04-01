package com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl;


import com.mttk.lowcode.backend.web.util.bi.pagination.Page;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.DateTimeFormat;

public class MySqlDialect extends AbstractDialect {
    @Override
    public String getPageSql(String sql, Page page) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 32);
        sqlBuilder.append(sql).append(" LIMIT "+page.getOffset()+", "+page.getSize()+" ");      
        return sqlBuilder.toString();
    }
    @Override
    public DateTimeFormat getDateTimeFormat() {
    	return new DateTimeFormat() {
    		public String convert_y(String field) {
    			return "DATE_FORMAT ("+field+",'%Y')";
    		}
    		public String convert_yq(String field) {
    			return "concat(year("+field+"),'Q',quarter("+field+"))";
    		}
    		public String convert_ym(String field) {
    			return "DATE_FORMAT ("+field+",'%Y-%m')";
    		}
    		public String convert_ymd(String field) {
    			return "DATE_FORMAT ("+field+",'%Y-%m-%d')";
    		}
    		public String convert_h(String field) {
    			return "DATE_FORMAT ("+field+",'%H')";
    		}
    		public String convert_hm(String field) {
    			return "DATE_FORMAT ("+field+",'%H:%i')";
    		}
    	};
    }
}
