package com.mttk.lowcode.backend.web;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAppAuthController;
import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAuthController;
import com.mttk.lowcode.backend.web.util.bi.BiMiscUtil;

@RestController
@RequestMapping("/bi/jdbcConnection")
public class JdbcConnectionController extends AbstractPersistentWithAppAuthController {
	@Override
	protected String getColName() {
		return "jdbcConnection";
	}
	// find all entities of the given connection(ID)
		@GetMapping(value = "/findEntities")
		public ResponseEntity<Document> findEntities(String connection) throws Exception {
			if(!checkDataAuthSingle(null,connection,null)) {
				return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
			}
			if(!canAccessApp(null, connection)) {
				return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
			}
			//
			try (Connection conn = BiMiscUtil.loadAndBuild(template, connection)) {
				//
				List<Document> entities = new ArrayList<>();
				try (ResultSet rs = conn.getMetaData().getTables(null, null, null, new String[] { "TABLE", "VIEW" })) {
//				//
//				ResultSetMetaData meta = rs.getMetaData();
//				for (int i = 0; i < meta.getColumnCount(); i++) {
//					System.out.println(i + "\t" + meta.getColumnName(i + 1));
//				}
					//
					while (rs.next()) {
						Document entity = new Document();
						entities.add(entity);
		
						entity.put("schema",  rs.getString("TABLE_SCHEM"));
						entity.put("catalog",  rs.getString("TABLE_CAT"));
						entity.put("name", rs.getString("TABLE_NAME"));
						entity.put("description", rs.getString("REMARKS"));
						entity.put("type", rs.getString("TABLE_TYPE"));
//						System.out.println(rs.getObject("TABLE_NAME")+"==="+rs.getObject("TABLE_CAT")+"==="+rs.getObject("TABLE_SCHEM"));
//						System.out.println(rs.getObject("SELF_REFERENCING_COL_NAME")+"==="+rs.getObject("REF_GENERATION"));
					}

				}
				//
				return ResponseEntity.ok(new Document("data", entities));
			}
		}

}
