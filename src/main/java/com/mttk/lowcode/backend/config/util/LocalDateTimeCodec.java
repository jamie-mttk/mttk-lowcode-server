package com.mttk.lowcode.backend.config.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class LocalDateTimeCodec implements Codec<LocalDateTime> {
	
	@Override
	public void encode(BsonWriter writer, LocalDateTime value, EncoderContext encoderContext) {

		writer.writeDateTime(value.toInstant(ZoneOffset.UTC).toEpochMilli());
	}

	@Override
	public LocalDateTime decode(BsonReader reader, DecoderContext decoderContext) {

		return Instant.ofEpochMilli(reader.readDateTime()).atOffset(ZoneOffset.UTC).toLocalDateTime();
	}

	@Override
	public Class<LocalDateTime> getEncoderClass() {
		return LocalDateTime.class;
	}
}