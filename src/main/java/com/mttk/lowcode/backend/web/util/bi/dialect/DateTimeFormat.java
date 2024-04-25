package com.mttk.lowcode.backend.web.util.bi.dialect;

import com.mttk.lowcode.backend.web.util.StringUtil;

//Since the date to string is quite different for different DB, so create a dialect
public interface DateTimeFormat {
public default String convert(String field,String format) {
	if(StringUtil.isEmpty(format) ||"none".equals(format)) {
		return field;
	}else if ("y".equals(format)){
		return convert_y(field);
	}else if ("yq".equals(format)) {
		return convert_yq(field);
	}else if ("ym".equals(format)) {
		return convert_ym(field);
	}else if ("ymd".equals(format)) {
		return convert_ymd(field);
	}else if ("h".equals(format)) {
		return convert_h(field);
	}else if ("hm".equals(format)) {
		return convert_hm(field);	
	}else {
		throw new RuntimeException("Unsuported format:"+format);
	}
}
public String convert_y(String field) ;
public String convert_yq(String field) ;
public String convert_ym(String field) ;
public String convert_ymd(String field) ;
public String convert_h(String field) ;
public String convert_hm(String field) ;
}
