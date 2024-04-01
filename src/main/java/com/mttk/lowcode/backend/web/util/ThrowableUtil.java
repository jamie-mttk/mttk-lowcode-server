package com.mttk.lowcode.backend.web.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;


public class ThrowableUtil {

	public static String dump2String(Throwable t) {
		try {
			return new String(dump(t), "utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static byte[] dump(Throwable t) {

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = null;
		try {
			ps = new PrintStream(os, true, "utf-8");
		} catch (UnsupportedEncodingException ignore) {
		}

		t.printStackTrace(ps);
		//
		ps.close();
		//
		return os.toByteArray();
	}

	//
	public static String dumpSimple(Throwable t) {
		StringBuffer sb = new StringBuffer(1024);
		//
		while (t != null) {
			if (sb.length() > 0) {
				sb.append(" caused by ");
			}
			sb.append(dumpSimpleSingle(t));
			//
			sb.append("\n");
			//
			t = t.getCause();
		}
		//
		return sb.toString();
	}

	private static String dumpSimpleSingle(Throwable t) {
		StringBuffer sb = new StringBuffer(512);
		sb.append(t.getClass().getCanonicalName());
		if (StringUtil.notEmpty(t.getMessage())) {
			sb.append(": ").append(t.getMessage());
		}
		if (t.getStackTrace().length > 0) {
//				StackTraceElement e=t.getStackTrace()[0];
//				sb.append(" at ").append(e.getFileName()).append(":").append(e.getLineNumber());
			sb.append(" at ").append(t.getStackTrace()[0]);
		}
		//
		return sb.toString();

	}

	public static String errorInfo(Throwable t) {
		return StringUtil.isEmpty(t.getMessage()) ? t.getClass().toString() : t.getMessage();

	}
	
}
