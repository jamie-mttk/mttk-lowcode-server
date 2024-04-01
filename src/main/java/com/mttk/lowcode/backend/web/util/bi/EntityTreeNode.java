package com.mttk.lowcode.backend.web.util.bi;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

//Present a tree node of model entity
public class EntityTreeNode {
	//Parent of this tree node, null if it is root node
	private EntityTreeNode parent;
	//Children of this tree node,empty if it is a leaf node
	private List<EntityTreeNode> children=new ArrayList<>();
	//The leve in the tree, the root is level 0
	private int level;
	//Entity definition
	private Document entity;
	//The relation to parent,null if it is root
	private Document relation;
	//
	public EntityTreeNode getParent() {
		return parent;
	}
	public void setParent(EntityTreeNode parent) {
		this.parent = parent;
	}
	public List<EntityTreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<EntityTreeNode> children) {
		this.children = children;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public Document getEntity() {
		return entity;
	}
	public void setEntity(Document entity) {
		this.entity = entity;
	}
	public Document getRelation() {
		return relation;
	}
	public void setRelation(Document relation) {
		this.relation = relation;
	}
	//
	@Override
	public String toString() {
		return "Entity=" + (entity==null?"":entity.getString("key")) +"  parent=" + (parent==null?"":parent.getEntity().getString("key")) + ", level=" + level +  ", relation=" + (relation==null?"":relation.toJson());
	}
	
}
