package com.mttk.lowcode.backend;

import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.Decoder;
import org.bson.codecs.DocumentCodec;

import com.mttk.lowcode.backend.config.util.MyCodecRegistry;

public class Test1 {
	 static final Decoder<Document> CODEC=new DocumentCodec(new MyCodecRegistry(),new BsonTypeClassMap());
	public static void main(String[] args) {
		String str="{\"sequence\":0,\"apis\":[],\"computed\":[],\"data\":[],\"lifecycle\":[],\"methods\":[],\"ui\":[],\"name\":\"aaa\"}";
		Document doc=Document.parse(str,CODEC);
		System.out.println(doc);

	}

}
