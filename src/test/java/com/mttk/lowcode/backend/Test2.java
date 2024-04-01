package com.mttk.lowcode.backend;

import java.io.File;
import java.util.List;

import org.bson.Document;

import com.mttk.lowcode.backend.web.util.FileHelper;

public class Test2 {
//	private static PasswordEncoder passwordEncoder=PasswordEncoderFactories.createDelegatingPasswordEncoder();

	public static void main(String[] args) throws Exception{
		byte[] data=FileHelper.readFile(new File("d:/data.json"));
		String str=new String(data,"utf-8");
		Document doc=Document.parse(str);
		List<Document> list=doc.getList("list", Document.class);
		StringBuilder sb=new StringBuilder(2048);
		for(Document d:list) {
			sb.append(d.getString("name")).append(",");
			sb.append(d.get("x")).append(",");
			sb.append(d.get("y")).append(",");
			sb.append(d.get("v")).append("\r\n");
		}
		//
		System.out.println(sb);
		//
		FileHelper.writeFile(sb.toString().getBytes("utf-8"), new File("d:/data.csv"), false);
	}

}

