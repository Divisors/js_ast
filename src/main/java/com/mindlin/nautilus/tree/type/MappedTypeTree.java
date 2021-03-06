package com.mindlin.nautilus.tree.type;

import com.mindlin.nautilus.tree.Modifiers;

public interface MappedTypeTree extends TypeTree {
	Modifiers getModifiers();
	
	TypeParameterDeclarationTree getParameter();
	
	TypeTree getType();
	
	@Override
	default Kind getKind() {
		return Kind.MAPPED_TYPE;
	}
	
	@Override
	default <R, D> R accept(TypeTreeVisitor<R, D> visitor, D data) {
		return visitor.visitMappedType(this, data);
	}
}
