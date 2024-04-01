package com.mttk.lowcode.backend.web.util.bi.pagination.dialect;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.mttk.lowcode.backend.web.util.StringUtil;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl.Db2Dialect;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl.HsqlDialect;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl.InformixDialect;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl.MySqlDialect;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl.OracleDialect;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl.PgSqlDialect;
import com.mttk.lowcode.backend.web.util.bi.pagination.dialect.impl.SqlServer2012Dialect;

public class DialectBuilder {

	private static Map<String, Class<? extends AbstractDialect>> dialectAliasMap = new HashMap<>();

	public static void registerDialectAlias(String alias, Class<? extends AbstractDialect> dialectClass) {
		dialectAliasMap.put(alias, dialectClass);
	}

	static {
		// 
		registerDialectAlias("hsqldb", HsqlDialect.class);
		registerDialectAlias("h2", HsqlDialect.class);
		registerDialectAlias("postgresql", HsqlDialect.class);
		registerDialectAlias("phoenix", HsqlDialect.class);

		registerDialectAlias("mysql", MySqlDialect.class);
		registerDialectAlias("mariadb", MySqlDialect.class);
		registerDialectAlias("sqlite", MySqlDialect.class);

		registerDialectAlias("oracle", OracleDialect.class);
		registerDialectAlias("db2", Db2Dialect.class);
		registerDialectAlias("informix", InformixDialect.class);
		// 解决 informix-sqli #129，仍然保留上面的
		registerDialectAlias("informix-sqli", InformixDialect.class);

		registerDialectAlias("sqlserver2012", SqlServer2012Dialect.class);

		registerDialectAlias("derby", SqlServer2012Dialect.class);
		// 达梦数据库,https://github.com/mybatis-book/book/issues/43
		registerDialectAlias("dm", OracleDialect.class);
		// 阿里云PPAS数据库,https://github.com/pagehelper/Mybatis-PageHelper/issues/281
		registerDialectAlias("edb", OracleDialect.class);
		// 神通数据库
		registerDialectAlias("oscar", MySqlDialect.class);
		// PG SQL
		registerDialectAlias("postgresql", PgSqlDialect.class);
	}

	/**
	 * 根据connection获取Dialect
	 * 
	 * @param connection
	 * @return
	 */
	public AbstractDialect findDialect(Connection connection) throws Exception{
		String url=connection.getMetaData().getURL();
		if (StringUtil.notEmpty(url)) {
			  String alias = fromJdbcUrl(url);
			  if (StringUtil.notEmpty(alias)) {
				  return findDialect(alias);
			  }
		}
		throw new RuntimeException("Can not find dialet from connection:"+connection);
	}
	/**
	 * 根据别名找dialect
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	public AbstractDialect findDialect(String alias) throws Exception{
		  Class<? extends AbstractDialect> dialectClass=dialectAliasMap.get(alias);
	      if (dialectClass==null) {
	    	  throw new RuntimeException("No dialect is found for "+alias);
	      }
	        return dialectClass.newInstance();
	}
	/**
	 * 缺省的Dialect实现
	 * @return
	 */
	public AbstractDialect defaultDialect() {
		return new AbstractDialect() {};
	}
	
	 private String fromJdbcUrl(String jdbcUrl) {
	        for (String dialect : dialectAliasMap.keySet()) {
	            if (jdbcUrl.indexOf(":" + dialect + ":") != -1) {
	                return dialect;
	            }
	        }
	        return null;
	    }
	
}
