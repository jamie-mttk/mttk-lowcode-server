package com.mttk.lowcode.backend.web.util.bi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BiCountUtil {
	public static Long calCount(Connection connection,String sqlCount) throws SQLException{
		try (PreparedStatement st = connection.prepareStatement(sqlCount, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet rs = st.executeQuery()) {
				if(rs.next()) {
					return rs.getLong(1);
				}
			}
		}
		//It should not come here!
		return 0l;
	}
	public static String buildSQLCountSQL(String sql) {
		StringBuilder sb=new StringBuilder();
		//
		sb.append("SELECT COUNT(1) FROM (").append(sql).append(") AS BI_COUNT_VT");
		//
		return sb.toString();
	}
}
