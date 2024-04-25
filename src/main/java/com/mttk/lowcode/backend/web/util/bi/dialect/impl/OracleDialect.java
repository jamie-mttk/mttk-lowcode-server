package com.mttk.lowcode.backend.web.util.bi.dialect.impl;


import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DateTimeFormat;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

public class OracleDialect extends AbstractDialect {
	 @Override
    public String getPageSql(String sql, Page page) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
        sqlBuilder.append("SELECT * FROM ( ");
        sqlBuilder.append(" SELECT TMP_PAGE.*, ROWNUM ROW_ID FROM ( ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" ) TMP_PAGE)");
        sqlBuilder.append(" WHERE ROW_ID <= "+(page.getOffset()+page.getSize())+" AND ROW_ID > "+page.getOffset());
        return sqlBuilder.toString();
    }

    @Override
    public DateTimeFormat getDateTimeFormat() {

       return new DateTimeFormat() {
           public String convert_y(String field) {
               return "EXTRACT(YEAR FROM "+field+")";
           }
           public String convert_yq(String field) {
               return "CONCAT(CONCAT(EXTRACT(YEAR FROM "+field+"),'Q'),FLOOR((EXTRACT(MONTH FROM "+field+") - 1) / 3) + 1)";
           }
           public String convert_ym(String field) {
               return "TO_CHAR("+field+",'YYYY-mm')";
           }
           public String convert_ymd(String field) {
               return "TO_CHAR("+field+",'YYYY-mm-DD')";
           }
           public String convert_h(String field) {
               return "EXTRACT(HOUR FROM "+field+")";
           }
           public String convert_hm(String field) {
               return "TO_CHAR("+field+", 'HH24:MI')";
           }
       };
    }
}
