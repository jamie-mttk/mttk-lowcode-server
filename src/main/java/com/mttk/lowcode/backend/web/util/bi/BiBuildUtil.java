package com.mttk.lowcode.backend.web.util.bi;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mttk.lowcode.backend.web.util.StringUtil;
import com.mttk.lowcode.backend.web.util.ThrowableUtil;
import com.mttk.lowcode.backend.web.util.bi.BiMiscUtil.DUMP_MODE;
import com.mttk.lowcode.backend.web.util.bi.dialect.AbstractDialect;
import com.mttk.lowcode.backend.web.util.bi.dialect.DialectBuilder;
import com.mttk.lowcode.backend.web.util.bi.dialect.Page;

public class BiBuildUtil {

	public static Document build(MongoTemplate template, Document dataModel, Document body) throws Exception {
		Document jdbcConnection = BiMiscUtil.loadJdbcConnection(template, dataModel.getString("jdbcConnection"));
		//
		long buildStart = System.currentTimeMillis();
		//
		try (Connection connection = BiMiscUtil.buildConnection(jdbcConnection)) {
			//
			return buildInternal(connection, dataModel, body).append("timecost",
					(System.currentTimeMillis() - buildStart));
		}catch(Throwable t) {
			Document error=new Document();
			error.append("error",true);
			error.append("message", t.getMessage());
			error.append("detail", ThrowableUtil.dump2String(t));
			if(t instanceof BiSQLException) {
				error.append("sql", ((BiSQLException)t).getSql());
			}
			return error;
		}
	}

	//
	private static Document buildInternal(Connection connection, Document dataModel, Document body) throws Exception {
		//
		//
		DataModelWrap dataModelWrap = new DataModelWrap(dataModel);
		BiBodyWrap biBodyWrap = new BiBodyWrap(body, dataModelWrap, connection);

		//
		List<Object> result = new ArrayList<>();
		if (biBodyWrap.getDumpMode() == DUMP_MODE.ARRAY) {
			// add field to first row
			List<Object> row = new ArrayList<>();
			//
			result.add(row);
			for (SQLField sqlField : biBodyWrap.getDimensions()) {
				row.add(sqlField.getConfig().getString("id"));
			}
			for (SQLField sqlField : biBodyWrap.getMetrics()) {
				row.add(sqlField.getConfig().getString("id"));
			}
		}
//		//
////		System.out.println(dimensions);
		List<Document> sqlList = new ArrayList<>(2);
		// build sql
		String sql = buildSQL(biBodyWrap, connection);

//		//

		Document pagination = null;
		Document paginationConfig = biBodyWrap.getPagination();

		//
		Integer rowLimit = biBodyWrap.getRowLimit();
		// Check pagination
		if ("SERVER".equals(paginationConfig.getString("mode"))) {
			//
			AbstractDialect dialet = new DialectBuilder().findDialect(connection);
			//
			String sqlCount = sql;
			if (rowLimit > 0) {
				// sql count to support row limit
				sqlCount = dialet.getPageSql(sql, new Page(1, rowLimit));
			}
			sqlCount = BiCountUtil.buildSQLCountSQL(sqlCount);
			System.out.println(sqlCount);
			sqlList.add(new Document().append("type", "count").append("sql", sqlCount));

			Long total = 0l;
			try {
			total=BiCountUtil.calCount(connection, sqlCount);
			} catch (Exception e) {
				throw new BiSQLException(sqlCount, e.getMessage(), e);
			}
			// Build page object
			Page page = new Page(paginationConfig.getInteger("page", 1), paginationConfig.getInteger("size", 10));

			// Amend sql to support pagination
			sql = dialet.getPageSql(sql, page);

			// Set pagination information
			pagination = new Document().append("page", page.getPage()).append("size", page.getSize()).append("total",
					total);
		} else if (rowLimit > 0) {
			// Row limit only works if no pagination is set
			AbstractDialect dialet = new DialectBuilder().findDialect(connection);
			sql = dialet.getPageSql(sql, new Page(1, rowLimit));

		}
		//
		sqlList.add(new Document().append("type", "main").append("sql", sql));
		// Final SQL
		System.out.println(sql);
		//
		try {
			BiMiscUtil.dumpData(result, connection, sql, biBodyWrap);
		} catch (Exception e) {
			throw new BiSQLException(sql, e.getMessage(), e);
		}
		//
		Document responseBody = new Document().append("data", result).append("sqls", sqlList);
		if (pagination != null) {
			responseBody.append("pagination", pagination);
		}
		return responseBody;
	}

	// select area,sum(qty) from test_order where area!='West'
	private static String buildSQL(BiBodyWrap biBodyWrap, Connection connection) throws Exception {

		StringBuilder sb = new StringBuilder(512);
		//
		sb.append("SELECT ");
		//
		BiSQLUtil.applyFields(sb, biBodyWrap.getDimensions(), false);
		//
		BiSQLUtil.applyFields(sb, biBodyWrap.getMetrics(), biBodyWrap.getDimensions().size() > 0);

		// This is all the entity we need add after FROM
		List<String> relatedEntityKeys = BiSQLUtil.parseEntitiesRelated(biBodyWrap);
		//
		BiSQLUtil.buildFrom(sb, biBodyWrap.getDataModelWrap(), relatedEntityKeys);

		//
		handleFilter(sb, biBodyWrap, relatedEntityKeys, connection);
		// GROUP by dimension
		// metrics.size means if there is no metric, no GROUP BY is needed
		if (biBodyWrap.getDimensions().size() > 0 && biBodyWrap.getMetrics().size() > 0) {
			sb.append(" GROUP BY ");
			for (int i = 0; i < biBodyWrap.getDimensions().size(); i++) {
				SQLField sqlField = biBodyWrap.getDimensions().get(i);
				if (i != 0) {
					sb.append(",");
				}
				sb.append(sqlField.getAlias());
			}
		}

//			// Order by
		handleSort(sb, biBodyWrap, relatedEntityKeys);

		//
		return sb.toString();
	}

	private static void handleFilter(StringBuilder sb, BiBodyWrap biBodyWrap, List<String> relatedEntityKeys,
			Connection connection) throws Exception {
		// Internal filter
		boolean hasWhere = false;
		// Here we should use the entities in FROM list.
		for (String key : relatedEntityKeys) {
			Document entity = biBodyWrap.getDataModelWrap().findEntity(key);
			String filter = entity.getString("filter");
			if (StringUtil.notEmpty(filter)) {
				if (!hasWhere) {
					hasWhere = true;
					sb.append(" WHERE ");
				} else {
					sb.append(" AND ");
				}
				sb.append(filter);
			}
		}

		// External filter - for example, from filter component
		List<Document> filters = biBodyWrap.getFilters();
		if (filters == null || filters.size() == 0) {
			return;
		}
		for (Document filter : filters) {
			//
			SQLField sqlField = BiMiscUtil.parseFieldSingle(biBodyWrap.getDataModelWrap(), filter, "filter",
					connection);
			//
			String operation = filter.getString("operation");
			Object value = filter.get("value");

			if ("LIKE".equalsIgnoreCase(operation)) {
				// Like operation,automatically add ' and %
				value = "%" + value + "%";
			} else if ("NOT_LIKE".equalsIgnoreCase(operation)) {
				operation = "NOT LIKE";
				// Like operation,automatically add ' and %
				value = "%" + value + "%";
			} else if ("START_WITH".equalsIgnoreCase(operation)) {
				operation = "LIKE";
				// Like operation,automatically add ' and %
				value = value + "%";
			} else if ("END_WITH".equalsIgnoreCase(operation)) {
				operation = "LIKE";
				// Like operation,automatically add ' and %
				value = "%" + value;
			} else if ("IS_NULL".equalsIgnoreCase(operation)) {
				operation = "IS NULL";
				value = null;
			} else if ("NOT_NULL".equalsIgnoreCase(operation)) {
				operation = "IS NOT NULL";
				value = null;
			} else if ("EMPTY".equalsIgnoreCase(operation)) {
				operation = "=";
				value = "";
			} else if ("NOT_EMPTY".equalsIgnoreCase(operation)) {
				operation = "!=";
				value = "";
			} else if ("IN".equalsIgnoreCase(operation)) {
				value = buildIn(value, isStringType(sqlField.getDefine().getString("dataType")));
			}
			//
			if (value instanceof String && StringUtil.notEmpty(value) && !"IN".equalsIgnoreCase(operation)) {
				// try to handle the unexpected characters
				value = tryClean((String) value);
			}
			//
			if (value != null && !"IN".equalsIgnoreCase(operation)
					&& isStringType(sqlField.getDefine().getString("dataType"))) {
				// add '
				value = "'" + value + "'";
			}
			if (!hasWhere) {
				hasWhere = true;
				sb.append(" WHERE ");
			} else {
				sb.append(" AND ");
			}
			//
			sb.append(sqlField.getExpression()).append(" ").append(operation);
			if (value != null) {
				sb.append(" ").append(value);
			}

		}
	}

	private static String tryClean(String value) {
		// replace ' to ''
		 return value.replaceAll("'", "''");
		//return value;
	}

	private static boolean isStringType(String dataType) {
		return "string".equalsIgnoreCase(dataType) || "datetime".equalsIgnoreCase(dataType)
				|| "time".equalsIgnoreCase(dataType);
	}

	private static String buildIn(Object value, boolean isString) {

		if (value instanceof List) {
			List<Object> vv = (List<Object>) value;
			//
			StringBuilder sb = new StringBuilder();
			sb.append('(');
			for (int i = 0; i < vv.size(); i++) {
				Object v = vv.get(i);
				if (i != 0) {
					sb.append(',');
				}
				if (isString) {
					sb.append("'");
				}
				sb.append(v);
				if (isString) {
					sb.append("'");
				}
			}
			sb.append(')');
			//
			return sb.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append('(');
			if (isString) {
				sb.append("'");
			}
			sb.append(value);
			if (isString) {
				sb.append("'");
			}
			sb.append(')');
			//
			return sb.toString();
		}
	}

	private static void handleSort(StringBuilder sb, BiBodyWrap biBodyWrap, List<String> relatedEntityKeys) {
		// Internal filter
		boolean hasOrderBy = false;
		// Here we should use the entities in FROM list.
		// Below is to handle all sorts in model
		for (String key : relatedEntityKeys) {
			Document entity = biBodyWrap.getDataModelWrap().findEntity(key);
			String sort = entity.getString("sort");
			if (StringUtil.notEmpty(sort)) {
				if (!hasOrderBy) {
					hasOrderBy = true;
					sb.append(" ORDER BY ");
				} else {
					sb.append(" , ");
				}
				sb.append(sort);
			}
		}
		// sort in metrics and dimensions
		// These are filters set inside metrics or dimensions
		for (SQLField sqlField : biBodyWrap.getDimensions()) {
			handleSortSingle(hasOrderBy, sb, sqlField);

		}
		for (SQLField sqlField : biBodyWrap.getMetrics()) {
			handleSortSingle(hasOrderBy, sb, sqlField);
		}

		// External sort - not used so far
//		List<Document> sorts = biBodyWrap.getSorts();
//		if (sorts == null || sorts.size() == 0) {
//			return;
//		}
//		for (Document sort : sorts) {
//			//
//			SQLField sqlField = BiMiscUtil.parseFieldSingle(biBodyWrap.getDataModelWrap(), sort, "sort");
//			//
//			String direction = sort.get("direction", "ASC");
//
//			if (!hasOrderBy) {
//				hasOrderBy = true;
//				sb.append(" ORDER BY ");
//			} else {
//				sb.append(" , ");
//			}
//
//			//
//			sb.append(sqlField.getExpression()).append(" ").append(direction);
//
//		}
	}

	private static boolean handleSortSingle(boolean hasOrderBy, StringBuilder sb, SQLField sqlField) {
		String sort = sqlField.getConfig().getString("_sort");
		if (StringUtil.notEmpty(sort) && !"NONE".equals(sort)) {
			if (!hasOrderBy) {
				hasOrderBy = true;
				sb.append(" ORDER BY ");
			} else {
				sb.append(" , ");
			}
			//
			sb.append(sqlField.getAlias()).append(" ").append(sort);
		}
		//
		return hasOrderBy;
	}
}
