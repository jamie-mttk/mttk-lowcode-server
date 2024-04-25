package com.mttk.lowcode.backend.web.util.bi;

public class BiSQLException extends Exception {

	private static final long serialVersionUID = 4919488027834021045L;
	//
	private String sql;
	public BiSQLException(String sql,String message, Throwable cause) {
		super(message,cause);
		//
		this.sql=sql;	
	}
	//
	public String getSql() {
		return sql;
	}
	

}
