package com.mttk.lowcode.backend.web;

import java.sql.Connection;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

	@RequestMapping(value = "/build")
	//
	public Document build(@RequestBody @NonNull Document body) throws Exception {
		//
//		Document body = loadBody();

		// Load data model
		Document dataModel = loadDataModel(body);
		//
		 return BiBuildUtil.build(template, dataModel, body);
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
	public Document columnValues(@RequestBody @NonNull Document body) throws Exception{
//		Document body=loadBody("sample_column_values.json");
		Document dataModeDoc = loadDataModelInternal(body.getString("dataModel"));
		Document jdbcConnection = BiMiscUtil.loadJdbcConnection(template, dataModeDoc.getString("jdbcConnection"));
		//
		try (Connection connection = BiMiscUtil.buildConnection(jdbcConnection)) {
			//
			DataModelWrap dataModelWrap = new DataModelWrap(dataModeDoc);
			//
			return  BiColumnValuesUtil.columnValues(connection, dataModelWrap, body.get("column", Document.class), body.getString("filter"));
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
	private Document loadDataModel(Document body) {
		Document config = body.get("config", Document.class);
		Assert.notNull(config, "config is empty");
		String dataModel = config.getString("dataModel");
		Assert.notNull(dataModel, "dataModel is not set");
		//
		return loadDataModelInternal(dataModel);
	}

	private Document loadDataModelInternal(String dataModelId) {
		//
		Document result = template.findById(dataModelId, Document.class, "dataModel");
		Assert.notNull(result, "No dataModel is found by " + dataModelId);

		//
		return result;
	}

	
}
