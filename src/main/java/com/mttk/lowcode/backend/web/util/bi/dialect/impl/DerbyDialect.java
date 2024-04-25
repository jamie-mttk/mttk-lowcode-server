package com.mttk.lowcode.backend.web.util.bi.dialect.impl;

import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DateTimeFormat;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

/**
 * 项目名: pagination
 * 文件名: DerbyDialect
 * 模块说明:
 * 修改历史:
 * 2024/04/08 - HeXinPeng - 创建
 */
public class DerbyDialect extends AbstractDialect {
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
                return " EXTRACT(YEAR FROM "+field+") ";
            }
            public String convert_yq(String field) {
                return " CONCAT(CAST(EXTRACT(YEAR FROM "+field+") AS VARCHAR),'Q',CAST(EXTRACT(QUARTER FROM "+field+") AS VARCHAR)) ";
            }
            public String convert_ym(String field) {
                return " CONCAT(EXTRACT(YEAR FROM "+field+"), '-', LPAD(CAST(EXTRACT(MONTH FROM "+field+") AS VARCHAR(2)), 2, '0')) ";
            }
            public String convert_ymd(String field) {
                return " TO_CHAR("+field+", 'YYYY-MM-DD') ";
            }
            public String convert_h(String field) {
                return " EXTRACT(HOUR FROM "+field+") ";
            }
            public String convert_hm(String field) {
                return " CONCAT(LPAD(CAST(EXTRACT(HOUR FROM "+field+") AS VARCHAR(2)), 2, '0'), ':', LPAD(CAST(EXTRACT(MINUTE FROM "+field+") AS VARCHAR(2)), 2, '0')) ";
            }
        };
    }
}
