package com.mttk.lowcode.backend.web;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.StringUtil;

//A demo class used to build demo
//@RestController
@RequestMapping("/demo")
public class DemoController{	
	@GetMapping(value = "/query")
	public Document query(@Nullable String name,@Nullable String city) throws Exception {
		//
		List<Document> listFiltered=null;
		if(StringUtil.isEmpty(name)&&StringUtil.isEmpty(city)) {
			listFiltered=list;
		}else {
			listFiltered=list.stream().filter((item)->{
				//name - fuzzy matching 
				if(StringUtil.notEmpty(name)) {
					String itemName=item.getString("name");
					if(StringUtil.isEmpty(itemName)||(itemName.indexOf(name)<0)) {
					return false;
					}
				}
				//City - strict matching
				if(StringUtil.notEmpty(city)) {
					String itemCity=item.getString("city");
					if(!city.equals(itemCity)) {
						return false;
					}
					
				}
				//
				return true;
			}).collect(Collectors.toList());
		}
		//
		return new Document("list",listFiltered);	
	}
	
	@PostMapping(value = "/save")
	public  Document  save(@RequestBody Document body) throws Exception {
		//Try to find exisitng
		String id=body.getString("id");

		if(StringUtil.isEmpty(id)) {
			//
			body.append("id", StringUtil.getUUID());
			//Assume it is a new one
			list.add(body);
			//
			return body;
		}else {
			int index=findById(id);
			if(index<0) {
				throw new Exception("The given id is not existed");
			}
			//
			Document existing=list.get(index);
			//Copy
			existing.append("name", body.get("name"));
			existing.append("city", body.get("city"));
			existing.append("age", body.get("age"));
			
			//
			return existing;
		}
		
	}

	//Find the given id from list
	private int findById(String id) {	
		
		for(int i=0;i<list.size();i++) {
			Document d=list.get(i);
			if(id.equals(d.getString("id"))) {
				return i;
			}
		}
		//
		return -1;
	}
//Demo data
//Demo only ,synchronized access may cause error
private static List<Document> list=new ArrayList<>();
static {
	list.add(new Document().append("id", "1").append("name", "Jamie").append("city", "Shanghai").append("age", 48));
	list.add(new Document().append("id", "2").append("name", "Selinda").append("city", "Shanghai").append("age", 14));
	list.add(new Document().append("id", "3").append("name", "Belinda").append("city", "Beijing").append("age", 40));
	list.add(new Document().append("id", "4").append("name", "Who").append("city", "Nanjing").append("age", 42));
}
}