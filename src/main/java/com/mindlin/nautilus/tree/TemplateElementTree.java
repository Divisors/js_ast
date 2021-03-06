package com.mindlin.nautilus.tree;

public interface TemplateElementTree extends Tree, UnvisitableTree {
	
	String getRaw();
	
	String getCooked();
	
	@Override
	default Tree.Kind getKind() {
		return Tree.Kind.TEMPLATE_ELEMENT;
	}
}
