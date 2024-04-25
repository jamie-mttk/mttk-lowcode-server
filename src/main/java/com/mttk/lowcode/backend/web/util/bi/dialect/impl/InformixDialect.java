package com.mttk.lowcode.backend.web.util.bi.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DateTimeFormat;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

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

	@Override
	public DateTimeFormat getDateTimeFormat() {

		return new DateTimeFormat() {
			public String convert_y(String field) {
				return " EXTRACT(YEAR FROM "+field+") ";
			}

			public String convert_yq(String field) {
				//Informix 没有 quarter 函数，需要根据月度计算
				return " EXTRACT(YEAR FROM "+field+")::CHAR(4) || 'Q' || LPAD(FLOOR((EXTRACT(MONTH FROM "+field+") - 1) / 3) + 1, 1, '0') ";
			}

			public String convert_ym(String field) {
				return " TO_CHAR("+field+", '%Y-%m') ";
			}

			public String convert_ymd(String field) {
				return " TO_CHAR("+field+", '%Y-%m-%d') ";
			}

			public String convert_h(String field) {
				return " EXTRACT(HOUR FROM "+field+") ";
			}

			public String convert_hm(String field) {
				return " TO_CHAR(order_date, '%H:%M') ";
			}
		};
	}

}
