package com.mttk.lowcode.backend.web.util.bi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mttk.lowcode.backend.web.util.StringUtil;
import com.mttk.lowcode.backend.web.util.bi.dialect.DialectBuilder;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

public class BiColumnValuesUtil {
	public static Document columnValues(Connection connection, DataModelWrap dataModelWrap, Document column,
			String filter) throws Exception{
		//
		SQLField sqlField = new SQLField(dataModelWrap, column, "filter",connection);
		//
		String sql=buildSQL(dataModelWrap, sqlField, filter);
		//Limit first 100 lines
//		sql=new DialectBuilder().findDialect(connection).getPageSql(sql, new Page(1,100));
		System.out.println(sql);
		//
		 List<Object> result= execute(connection,sql);
		//
		return new Document("data",result);

	}

	private static String buildSQL(DataModelWrap dataModelWrap, SQLField sqlField, String filter) throws Exception {
		//
		StringBuilder sb = new StringBuilder(128);
		//
		sb.append("SELECT DISTINCT ");
		//
		BiSQLUtil.applyFields(sb, Arrays.asList(sqlField), false);

		//
		buildSQLFrom(sb,dataModelWrap,sqlField);
		//Filter
		if(StringUtil.notEmpty(filter)) {
			sb.append(" WHERE ").append(sqlField.getExpression()).append(" LIKE '%").append(filter).append("%'");	
		}
		//
		return sb.toString();
	}

	private static void buildSQLFrom(StringBuilder sb, DataModelWrap dataModelWrap,SQLField sqlField) {
		String columnType = sqlField.getDefine().getString("type");
		List<String> entityList=null;
		
		if ("field".equals(columnType)) {
			entityList=new ArrayList<>();
			entityList.add(sqlField.getDefine().getString("entity"));
		} else if ("expression".equals(columnType)) {
			entityList=sqlField.getDefine().getList("entities",String.class);
		} else {
			throw new RuntimeException("Unkown column type:" + columnType);
		}
		//
		BiSQLUtil.buildFrom(sb, dataModelWrap,entityList );
	}

	private static List<Object> execute(Connection connection,String sql) throws SQLException{
		List<Object> result=new ArrayList<>();
		//
		try (PreparedStatement st = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet rs = st.executeQuery()) {
				while(rs.next()) {
					result.add(rs.getObject(1));
				}
			}
		}
		//
		return result;
	}

}
