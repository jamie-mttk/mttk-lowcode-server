package com.mttk.lowcode.backend.web.util.bi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mttk.lowcode.backend.web.util.StringUtil;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;




public class DataModelUtil {

	public static List<Document> loadFieldsInternal(MongoTemplate template, Document model, Document entity)
			throws Exception {

		//
		String connection = model.getString("jdbcConnection");
		try (Connection conn = BiMiscUtil.loadAndBuild(template, connection)) {
			if ("SQL".equalsIgnoreCase(entity.getString("type"))) {
				//
				return loadFieldsSQL(conn, entity);
			} else {
				return loadFieldsTable(conn, entity);
			}
		}

	}

	private static List<Document> loadFieldsTable(Connection conn, Document entity) throws Exception {
		List<Document> fieldList = new ArrayList<>(10);
		//
		try (ResultSet rs = conn.getMetaData().getColumns(entity.getString("catalog"), entity.getString("schema"),
				entity.getString("table"), null)) {
			while (rs.next()) {
				Document field = new Document();
				fieldList.add(field);
				//
				field.put("key", rs.getString("COLUMN_NAME"));
				field.put("dataType", convertType(rs.getInt("DATA_TYPE")));
				String label = rs.getString("REMARKS");
				if (StringUtil.isEmpty(label)) {
					label = rs.getString("COLUMN_NAME");
				}
				field.put("label", label);
			}
		}
		//
		return fieldList;
	}

	private static List<Document> loadFieldsSQL(Connection conn, Document entity) throws Exception {
		List<Document> fieldList = new ArrayList<>(10);
		//
		String sql = entity.getString("sql");
		if(StringUtil.isEmpty(sql)) {
			return fieldList;
		}
		// Add 1=2 condition to avoid return too many rows which is not necessary
		sql=addConditionNegative(sql);
		System.out.println(sql);
		//
		try (PreparedStatement st = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet rs = st.executeQuery()) {
				int columnCount = rs.getMetaData().getColumnCount();
				ResultSetMetaData meta = rs.getMetaData();

				for (int i = 1; i <= columnCount; i++) {
					Document field = new Document();
					fieldList.add(field);
					// Please note here we use column label, since the parameter of rs.get is
					// columnLabel(NOT column name)
					// If there is column alias , the colum label is the alias;otherwise it is the
					// column raw name
					field.put("key", meta.getColumnLabel(i));
					field.put("label", meta.getColumnLabel(i));
					field.put("dataType", convertType(meta.getColumnType(i)));

				}
			}
		}
		//
		return fieldList;
	}

	
	//Add 0=1 condition to SQL
	private static String addConditionNegative(String sql) throws Exception{
		net.sf.jsqlparser.statement.Statement stmt = CCJSqlParserUtil.parse(sql);	
		if (!(stmt instanceof PlainSelect)) {
			throw new Exception("SQL must be a SELECT statement:"+sql);
		}
		//
		PlainSelect select = (PlainSelect) stmt;
//		System.out.println(stmt.getClass()+"~~~"+stmt);

		Expression where= select.getWhere();
		
		Expression whereNegative=new EqualsTo().withLeftExpression(new LongValue(0)).withRightExpression(new LongValue(1));

		
		if(where==null) {
			//There is no Where in SQL
			where=whereNegative;
		}else {
			where=new AndExpression(where,whereNegative);
		}
		//Replace original where
		select.setWhere(where);
		//
		return select.toString();
	}
	
	// type is the value from java.sql.Types
	private static String convertType(int type) {
		// BOOLEAN consider as integer
		if (type == Types.BIT || type == Types.TINYINT || type == Types.SMALLINT || type == Types.INTEGER
				|| type == Types.BIGINT || type == Types.BOOLEAN) {
			return "integer";
		} else if (type == Types.FLOAT || type == Types.REAL || type == Types.DOUBLE || type == Types.NUMERIC
				|| type == Types.DECIMAL) {
			return "number";
			// ROW ID is considered as string
		} else if (type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGVARCHAR || type == Types.ROWID
				|| type == Types.NCHAR || type == Types.NVARCHAR || type == Types.LONGNVARCHAR) {
			return "string";
		} else if (type == Types.DATE || type == Types.TIMESTAMP || type == Types.TIMESTAMP_WITH_TIMEZONE) {
			return "datetime";
		} else if (type == Types.TIME || type == Types.TIME_WITH_TIMEZONE) {
			return "time";
		} else {
			// BINARY VARBINARY LONGVARBINARY NULL OTHER JAVA_OBJECT DISTINCT STRUCT ARRAY
			// BLOB CLOB REF DATALINK NCLOB SQLXML
			// Not supported
			return null;
		}

	}
}
