package com.mttk.lowcode.backend.config.util;

import java.io.IOException;

import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.Decoder;
import org.bson.codecs.DocumentCodec;
import org.bson.json.JsonWriterSettings;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class DocumentMessageConverter extends AbstractHttpMessageConverter<Document> {
	private static final Decoder<Document> CODEC=new DocumentCodec(new MyCodecRegistry(),new BsonTypeClassMap());
    public DocumentMessageConverter() {
        // 
        super( MediaType.APPLICATION_JSON);
    }
    @Override
    protected boolean supports(Class<?> clazz) {
     	//System.out.println("#############@"+clazz);
        // 表明只处理Document类型的参数    	
        return Document.class.isAssignableFrom(clazz);
    }
    

    @Override
    public Document readInternal(Class<? extends Document> clazz,
            HttpInputMessage inputMessage) throws IOException,
            HttpMessageNotReadableException {    	
        String temp = new String(IOUtil.toArray(inputMessage.getBody()),"UTF-8");

        return Document.parse(temp,CODEC);

    }


    @Override
    public void writeInternal(Document doc,
            HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {    
    	//System.out.println("@@@@@@@@@@@@@@");
        JsonWriterSettings settings= JsonWriterSettings.builder()
        		.objectIdConverter(new ObjectIdConverter())
        		.dateTimeConverter(new DateTimeConverter())//必须自己转换,否则系统自动转换成了UTC时间导致差8个小时
        		.build();
       // System.out.println("@@Sent:@@:"+doc.toJson(settings));
        outputMessage.getBody().write(doc.toJson(settings).getBytes("utf-8"));
    }

}
