package com.mttk.lowcode.backend.web.util.bi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.Assert;

import com.mttk.lowcode.backend.web.util.StringUtil;

public class BiMiscUtil {
	// Format of SQL result dump,default mode is ARRAY
	// ARRARY: Each row is an array withtout field name, the first row is the column
	// labels, used for echarts
	// JSON: Each row is a JSON
	public enum DUMP_MODE {
		ARRAY, JSON
	};

	//
	// Load JDBC connection document from DB by id
	public static Document loadJdbcConnection(MongoTemplate template, String connection) {
		Assert.notNull(connection, "Connection ID is null");
		//
		Document result = template.findById(connection, Document.class, "jdbcConnection");
		Assert.notNull(result, "No JDBC connection is found by " + connection);
		//
		return result;
	}

	// build JDBC connection from connection definition
	public static Connection buildConnection(Document doc) throws ClassNotFoundException, SQLException {
		Class.forName(doc.getString("driverClass"));

		Connection connection = DriverManager.getConnection(doc.getString("url"), doc.getString("user"),
				doc.getString("password"));
		//
		if (doc.getBoolean("readonly", true)) {
			connection.setReadOnly(true);
		}

		//
		return connection;
	}

	public static Connection loadAndBuild(MongoTemplate template, String connection)
			throws ClassNotFoundException, SQLException {
		return buildConnection(loadJdbcConnection(template, connection));
	}

	//
	public static void praseFields(List<SQLField> fieldList, DataModelWrap dataModelWrap, Document config,
			String section,Connection connection) {
		//
		for (Document fieldConfig : config.getList(section, Document.class)) {
			// Parse sql field
			fieldList.add(parseFieldSingle(dataModelWrap, fieldConfig, section,connection));
		}
	}

	public static SQLField parseFieldSingle(DataModelWrap dataModelWrap, Document config, String section,Connection connection) {

		SQLField sqlField = new SQLField(dataModelWrap, config, section,connection);

		//
		return sqlField;
	}

	// TBD,find the entities needed,include the entities in field or entities used
	// for relation
	public static Set<Document> findEntitiesNeeded(Document dataModel, Map<String, Document> entityMap,
			List<SQLField> dimensions, List<SQLField> metrics) {
		Set<Document> entities = new HashSet<>();
		// First find all the entites
		for (SQLField d : dimensions) {
			// This is for type=field
			String str = d.getConfig().getString("entity");
			if (StringUtil.notEmpty(str)) {
				Document entity = entityMap.get(str);
				if (entity == null) {
					throw new RuntimeException("No entity is found by " + str);
				}
				entities.add(entity);
			}
			// This is for type=expression
			List<String> strs = d.getDefine().getList("entities", String.class);
			if (strs == null || strs.size() == 0) {
				continue;
			}
			for (String s : strs) {
				Document entity = entityMap.get(s);
				if (entity == null) {
					throw new RuntimeException("No entity is found by " + s);
				}
				entities.add(entity);
			}
		}
		// TBD,here we need to check whether it is enough...
		//
		return entities;
	}

	// execute SQL and dump
	public static void dumpData(List<Object> result, Connection connection, String sql, BiBodyWrap biBodyWrap)
			throws SQLException {

		try (PreparedStatement st = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY)) {
			try (ResultSet rs = st.executeQuery()) {
				int columnCount = rs.getMetaData().getColumnCount();

				while (rs.next()) {
					switch (biBodyWrap.getDumpMode()) {
					case ARRAY:
						List<Object> rowArray = new ArrayList<>(columnCount);
						result.add(rowArray);
						for (int i = 1; i <= columnCount; i++) {
							rowArray.add(formatValue(rs.getObject(i)));
						}
						break;
					case JSON:
						ResultSetMetaData meta = rs.getMetaData();
						Map<String, Object> rowJson = new HashMap<>();
						result.add(rowJson);
						for (int i = 1; i <= columnCount; i++) {
//							System.out.println(i+"~~~"+meta.getColumnName(i)+"==>"+meta.getColumnLabel(i));
							// Here getColumnLabel is used to get the label after AS
							rowJson.put(meta.getColumnLabel(i), formatValue(rs.getObject(i)));
						}
					}
				}
			}
		}
	}

	//
	private static Object formatValue(Object value) {
		if (value == null) {
			return null;
		}
		//
		if (value instanceof LocalDateTime) {
			LocalDateTime ldt = (LocalDateTime) value;
			Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
			return sdf.format(date);
		}else if (value instanceof java.util.Date) {
			//java.util.Date is more generic than java.sql.Date
			Date date = (Date)value;
			SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
			return sdf.format(date);
		}
		
		
		//
		return value;
	}

	//
	private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

//	// Try to find the datetime format defined during echarts design
//	// index is start from 1
//	private static String findDateTimeFormat(BiBodyWrap biBodyWrap, int index) {
//		SQLField sqlField=findSqlField(biBodyWrap,index);
//		if(sqlField==null) {
//			//Not found?I think it is impossible!
//			return DEFAULT_DATETIME_FORMAT;
//		}
//		//
//		String  format_datetime=sqlField.getConfig().get("_format_datetime",DEFAULT_DATETIME_FORMAT);	
//		if("_CUSTOMIZE".equals(format_datetime)) {
//			format_datetime=sqlField.getConfig().get("_format_datetime_customize",DEFAULT_DATETIME_FORMAT);	
//		}
//		//
//		return format_datetime;
//	}
//
//	// Find the SQLField by index,whether it is in dimension or metric
//	// return null if not found
//	private static SQLField findSqlField(BiBodyWrap biBodyWrap, int index) {
//		if (biBodyWrap.getDimensions().size() <= index) {
//			// it is in dimension
//			return biBodyWrap.getDimensions().get(index - 1);
//		}
//		int indexRemainder = index - biBodyWrap.getDimensions().size();
//		if (biBodyWrap.getMetrics().size() <= indexRemainder) {
//			return biBodyWrap.getMetrics().get(indexRemainder - 1);
//		}
//		//
//		return null;
//	}

}
