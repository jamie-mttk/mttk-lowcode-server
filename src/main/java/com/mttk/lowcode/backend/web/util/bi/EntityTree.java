package com.mttk.lowcode.backend.web.util.bi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bson.Document;

import com.mttk.lowcode.backend.web.util.StringUtil;

//
public class EntityTree {
	private DataModelWrap dataModelWrap;
	//
	private EntityTreeNode root;
	//
	private EntityTree() {
		
	}
	public static EntityTree build(DataModelWrap dataModelWrap) {
		//
		EntityTreeNode rootEntityTreeNode=new EntityTreeNode();
		rootEntityTreeNode.setLevel(0);
		rootEntityTreeNode.setEntity(dataModelWrap.getRootEntity());
		//
		buildChildren(rootEntityTreeNode,dataModelWrap.getRelations(),dataModelWrap);
		//
		EntityTree entityTree=new EntityTree();
		entityTree.root=rootEntityTreeNode;
		entityTree.dataModelWrap=dataModelWrap;
		//
		return entityTree;
	}	
	//Find nodes by entityKeys and sort by level DESC
	public  List<EntityTreeNode> findNodes(List<String> entityKeys){
		//
		List<EntityTreeNode>  result=new ArrayList<>(entityKeys.size());
		//
		findNodesInternal(result,entityKeys,root);
		//
		result.sort(new Comparator<EntityTreeNode>(){
            @Override
            public int compare(EntityTreeNode node1, EntityTreeNode node2) {
                   return node2.getLevel()-node1.getLevel();
            }

        });
		//
		return result;
		
	}
	//
	private  void findNodesInternal(List<EntityTreeNode>  result,List<String> entityKeys,EntityTreeNode node) {
		if(entityKeys.contains(node.getEntity().getString("key"))) {
			result.add(node);
		}
		for(EntityTreeNode child:node.getChildren()) {
			findNodesInternal(result, entityKeys, child);
		}
	}
	//
	private static void buildChildren(EntityTreeNode entityTreeNode,List<Document> relations ,DataModelWrap dataModelWrap) {
		String entityKey=entityTreeNode.getEntity().getString("key");


		for(Document relation:relations) {
			if(!entityKey.equals(relation.getString("source"))) {
				continue;
			}
			//
			EntityTreeNode child=new EntityTreeNode();
			entityTreeNode.getChildren().add(child);
			//
			child.setParent(entityTreeNode);
			child.setLevel(entityTreeNode.getLevel()+1);
			child.setEntity(dataModelWrap.findEntity(relation.getString("target")));
			child.setRelation(relation);
	
			//
			buildChildren(child,relations,dataModelWrap);
		}
	}
	//
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		//
		print(sb,root);
		//
		return sb.toString();
		
	}
	private void print(StringBuilder sb,EntityTreeNode node) {
		sb.append(StringUtil.fillString(node.getLevel(), ' ')).append(node.toString()).append("\n");
		for(EntityTreeNode child:node.getChildren()) {
			print(sb,child);
		}
	}
	
}
