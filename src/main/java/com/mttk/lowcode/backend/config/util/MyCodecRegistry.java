package com.mttk.lowcode.backend.config.util;

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import java.util.Date;

import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MyCodecRegistry implements CodecRegistry {
	
	
	private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(	
			asList(new MyCodecProvider(),new ValueCodecProvider(), new BsonValueCodecProvider(), new DocumentCodecProvider(),new IterableCodecProvider()));

//	private static final CodecRegistry DEFAULT_REGISTRY =CodecRegistries.fromRegistries(  CodecRegistries.fromCodecs(new LocalDateTimeCodec()),
//			fromProviders(	
//			asList(new MyCodecProvider(),new ValueCodecProvider(), new BsonValueCodecProvider(), new DocumentCodecProvider(),new IterableCodecProvider())));
	private static final Codec MY_STRING_CODEC = new MyStringCodec();
	private static final Codec MY_DATE_CODEC = new MyDateCodec();
	//
	
	@Override
	public <T> Codec<T> get(Class<T> clazz) {
		
//		 System.out.println("@@"+clazz+"==>"+DEFAULT_REGISTRY.get(clazz));
		if (clazz.equals(String.class)) {
			// System.out.println("~~~~"+clazz);
			return MY_STRING_CODEC;
		} else if (clazz.equals(Date.class)) {
			// System.out.println("~~~~"+clazz);
			return MY_DATE_CODEC;
		}
		//
		return DEFAULT_REGISTRY.get(clazz);
	}

	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		return get(clazz);
	}
}