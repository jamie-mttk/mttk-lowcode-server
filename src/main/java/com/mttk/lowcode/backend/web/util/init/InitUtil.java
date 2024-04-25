package com.mttk.lowcode.backend.web.util.init;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mttk.lowcode.backend.config.util.IOUtil;
import com.mttk.lowcode.backend.web.util.MongoUtil;
import com.mttk.lowcode.backend.web.util.StringUtil;

public class InitUtil {
	public static void init( MongoTemplate template)  throws Exception{
		if(!needInit(template)) {
			return ;
		}
		System.out.println("Init is called!");
		initInternal(template,false);
		initDone(template);
	}
	//Whether init is needed
	private static boolean needInit( MongoTemplate template) {
		//
		List<Document> list=template.find(new Query(Criteria.where("key").is("initFlag")), Document.class,"config");
		if(list==null||list.size()==0) {
			//not set,need init
			return true;
		}
		//
		return !list.get(0).getBoolean("value", false);
	}
	private static void initDone( MongoTemplate template) {
		Document doc=new Document().append("key", "initFlag").append("value", true);
		template.insert(doc, "config");
	}
	
	private static void initInternal( MongoTemplate template,boolean clear) throws Exception{
		initBatch(template,Arrays.asList("account","accountRole","authority","echartsTheme"),clear);
		
	}
	//Init multiple collection
	private static void initBatch( MongoTemplate template,List<String> names,boolean clear)throws Exception{
		for(String name:names) {
			initSingle(template,name,clear);
		}
	}
	//Init one collection
	private static int initSingle( MongoTemplate template,String name,boolean clear)throws Exception{
		//
		if(clear) {
		template.dropCollection(name);
		}
		//
		String json=null;
		try(InputStream is=InitUtil.class.getResourceAsStream(name+".json")){
			json=new String(IOUtil.toArray(is),"utf-8");
		}
		//Document can only parse json,so build a json first and then get json array
		List<Document> data=Document.parse("{list:"+json+"}").getList("list", Document.class);
		//
		for(Document d:data) {
			String id=MongoUtil.getId(d);
			if(StringUtil.notEmpty(id)) {
				d.append("_id", new ObjectId(id));
			}
			template.save(d,name);
		}		
		//
		return data.size();
	}
}
