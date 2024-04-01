package com.mttk.lowcode.backend.web.util.bi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.util.Assert;

import com.mttk.lowcode.backend.web.util.StringUtil;

//Functions to build BI SQL
public class BiSQLUtil {

	//Add SQL SELECT fields
	public static void applyFields(StringBuilder sb,List<SQLField> sqlFields,boolean alreadyHasField)throws Exception{
		//
		for(SQLField sqlField:sqlFields) {
	
			//If already has field in SQL, add "," to sepereate
			if(alreadyHasField) {
				sb.append(",");
			}else {
				alreadyHasField=true;
			}
			//Handle SQL field
			sb.append(sqlField.getExpression()).append(" AS ").append(sqlField.getAlias());
		}
		//
	}


	//Build from and join	
	public static void buildFrom(StringBuilder sb,DataModelWrap dataModelWrap,List<String>  relatedEntityKeys) {

		//
		//
		Assert.isTrue(relatedEntityKeys!=null && relatedEntityKeys.size()>0, "entityKeys is null");
		//
		sb.append(" FROM ");
		//
		if(relatedEntityKeys.size()==1) {
			//only one entity
			buildFromSingle(sb,dataModelWrap,relatedEntityKeys.get(0));
			//
			return ;
		}
	
		//
		List<Document> relations=buildRelations(dataModelWrap, relatedEntityKeys);
		boolean isFirst=true;
		//please note it should iterator from end to start
		for(int i=relations.size()-1;i>=0;i--) {
			Document relation=relations.get(i);
			if(isFirst) {
				buildFromSingle(sb,dataModelWrap,relation.getString("source"));
				//
				isFirst=false;
				
			}
			//Build join
			sb.append(" ").append(relation.get("joinType","INNER JOIN")).append(" ");
			buildFromSingle(sb,dataModelWrap,relation.getString("target"));
			//Build on 
			sb.append(" ON ");
			List<Document> keys=relation.getList("keys",Document.class);
			Document sourceEntity=	dataModelWrap.findEntity(relation.getString("source"));
			Document targetEntity=	dataModelWrap.findEntity(relation.getString("target"));
			for(int j=0;j<keys.size();j++) {
				Document keyDoc=keys.get(j);
				if(j!=0) {
					sb.append(" AND ");
				}
				sb.append(sourceEntity.getString("alias")).append(".").append(keyDoc.getString("sourceKey"));
				sb.append(" = ");
				sb.append(targetEntity.getString("alias")).append(".").append(keyDoc.getString("targetKey"));
				
			}
		}
	}
	
	//Found the entity keys needed in SQL FROM/JOIN
	public static List<String> parseEntitiesRelated(BiBodyWrap biBodyWrap){
		List<String> result=new ArrayList<>();
	

		//dimensions
		parseEntitiesRelatedInternal(result,biBodyWrap,biBodyWrap.getDimensions().stream().map(item->item.getConfig()).collect(Collectors.toList()));
		//metrics
		parseEntitiesRelatedInternal(result,biBodyWrap,biBodyWrap.getMetrics().stream().map(item->item.getConfig()).collect(Collectors.toList()));
		//filters
		parseEntitiesRelatedInternal(result,biBodyWrap,biBodyWrap.getFilters());
		//sorts
		parseEntitiesRelatedInternal(result,biBodyWrap,biBodyWrap.getSorts());
		//
		return result;
	}
	//Handle a list of 
	private static void parseEntitiesRelatedInternal(List<String> result,BiBodyWrap biBodyWrap,List<Document> list) {
		if(list==null||list.size()==0) {
			return;
		}
		//
		for(Document d:list) {

			Document column=biBodyWrap.getDataModelWrap().findColumn(d.getString("key"));
			String type=column.getString("type");
			if("field".equals(type)) {
				String entityKey=column.getString("entity");
				if(!result.contains(entityKey)) {
					result.add(entityKey);
				}
			}else if("expression".equals(type)) {
				List<String> entities=column.getList("entities", String.class);
				if(entities==null||entities.size()==0) {
					continue;
				}
				for(String s:entities) {
					if(!result.contains(s)) {
						result.add(s);
					}
				}
				
			}else {
				throw new RuntimeException("Unkown column type:"+type);
			}
			
			
		}
	}

	
	private static void buildFromSingle(StringBuilder sb,DataModelWrap dataModelWrap,String entityKey) {
		Document entity=dataModelWrap.findEntity(entityKey );
		sb.append(" ");
		String type=entity.getString("type");
		if("TABLE".equals(type)) {
			if(StringUtil.notEmpty(entity.getString("schema"))) {
				sb.append(entity.getString("schema")).append(".");
			}else if(StringUtil.notEmpty(entity.getString("catalog"))) {
				sb.append(entity.getString("catalog")).append(".");
			}
			sb.append(entity.getString("table"));
		}else if("SQL".equals(type)) {
			sb.append("(").append(entity.getString("sql")).append(")");
		}else {
			throw new RuntimeException("Unkown entity type:"+type);
		}
		sb.append(" AS ").append(entity.getString("alias"));
	}
	
	
	
	//Build all the relations used to build table from list and joins
		private static List<Document> buildRelations(DataModelWrap dataModelWrap,List<String> entityKeys) {
			Assert.isTrue(entityKeys.size()>1, "Size of Entitykeys should be greater than 1");
			//
			EntityTree entityTree=EntityTree.build(dataModelWrap);
			//
			List<Document> relations=new ArrayList<>();
			//
			List<String> remainder=new ArrayList<>(entityKeys.size());
			for(String str:entityKeys) {
				remainder.add(str);
			}
			
			for(int i=0;i<100;i++) {
				//Only one left,that means all the relations are found
				if(remainder.size()<=1) {
					break;
				}
				//
				List<EntityTreeNode> nodes= entityTree.findNodes(remainder);
				if(nodes.size()==0) {
					break;
				}
				//The fist node is the lowest node(with max level)
				EntityTreeNode node=nodes.get(0);
				if(node.getParent()==null) {
					//found root node
					break;
				}
				//Found one
				//add into relation
				relations.add(node.getRelation());
				//remove the processed node from remainder
				remainder.remove(node.getRelation().getString("target"));
				//
				String source=node.getRelation().getString("source");
				if(!remainder.contains(source)) {
					remainder.add(source);
				}
			}		
			//
			return relations;
		}
}
