package com.mttk.lowcode.backend.config.util;

import java.time.LocalDateTime;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MyCodecProvider implements CodecProvider {

	@Override
	@SuppressWarnings("unchecked")
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
//		System.out.println(clazz+"~~~"+clazz.equals(LocalDateTime.class));
		if (clazz.equals(LocalDateTime.class)) {
			//
			return (Codec<T>) new LocalDateTimeCodec();
		}
		//
		return null;
	}
}
