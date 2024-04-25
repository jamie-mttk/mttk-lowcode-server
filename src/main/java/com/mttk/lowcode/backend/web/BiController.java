package com.mttk.lowcode.backend.web;

import java.sql.Connection;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.bi.BiBuildUtil;
import com.mttk.lowcode.backend.web.util.bi.BiColumnValuesUtil;
import com.mttk.lowcode.backend.web.util.bi.BiMiscUtil;
import com.mttk.lowcode.backend.web.util.bi.DataModelWrap;

@RestController
@RequestMapping("/bi")
public class BiController {
	@Autowired
	protected MongoTemplate template;
	//dataModelController 
	@Autowired
	protected DataModelController dataModelController;

	@RequestMapping(value = "/build")
	//
	public ResponseEntity<Document> build(@RequestBody @NonNull Document body) throws Exception {
		//
//		Thread.sleep(5*1000);
		
		// Load data model and check authorities
		ResponseEntity<Document> dataModel = loadDataModel(body);
//		System.out.println("STATUS CODE:"+dataModel.getStatusCode());
		if(dataModel.getStatusCode().isError()) {
			return dataModel;
		}
		//
		 return ResponseEntity.ok(BiBuildUtil.build(template, dataModel.getBody(), body));
	}

	/**
	 * Load first n distinct value,
	 * 
	 * @param dataMode data model
	 * @param column   column key
	 * @param filter   only support if column is string type
	 * @return
	 */
	@RequestMapping(value = "/columnValues")
	public ResponseEntity<Document> columnValues(@RequestBody @NonNull Document body) throws Exception{
//		Document body=loadBody("sample_column_values.json");
		ResponseEntity<Document> result=loadDataModelInternal(body.getString("dataModel"));
		if(result.getStatusCode().isError()) {
			return result;
		}
		Document dataModeDoc = result.getBody();
		Document jdbcConnection = BiMiscUtil.loadJdbcConnection(template, dataModeDoc.getString("jdbcConnection"));
		//
		try (Connection connection = BiMiscUtil.buildConnection(jdbcConnection)) {
			//
			DataModelWrap dataModelWrap = new DataModelWrap(dataModeDoc);
			//
			return  ResponseEntity.ok(BiColumnValuesUtil.columnValues(connection, dataModelWrap, body.get("column", Document.class), body.getString("filter")));
		}
	}
	
//	//sample_input.json
//	private Document loadBody(String file) throws Exception {
//		try (InputStream is = new FileInputStream(
//				"D:\\biz\\development\\vueWrapping\\code_java\\Backend\\src\\test\\java\\com\\mttk\\lowcode\\backend\\"+file)) {
//			String data = new String(IOUtil.toArray(is), "utf-8");
//			return Document.parse(data);
//		}
//
//	}

	// ****************************************************
	// *
	// ****************************************************
	private  ResponseEntity<Document> loadDataModel(Document body) throws Exception{
		Document config = body.get("config", Document.class);
		Assert.notNull(config, "config is empty");
		String dataModel = config.getString("dataModel");
		Assert.notNull(dataModel, "dataModel is not set");
		//
		return loadDataModelInternal(dataModel);
	}

	private  ResponseEntity<Document> loadDataModelInternal(String dataModelId) throws Exception{
		return dataModelController.load(dataModelId);
	}

	
}
