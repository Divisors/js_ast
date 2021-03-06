package com.mindlin.nautilus.tree.type;

import com.mindlin.nautilus.tree.IdentifierTree;
import com.mindlin.nautilus.tree.Tree;
import com.mindlin.nautilus.tree.UnvisitableTree;

/**
 * Generic type parameter (not argument).
 * 
 * @author mailmindlin
 * @see GenericRefTypeTree
 */
public interface TypeParameterDeclarationTree extends Tree, UnvisitableTree {
	
	/**
	 * Name of generic parameter.
	 * 
	 * @return name (not null)
	 */
	IdentifierTree getName();
	
	/**
	 * Supertype of generic parameter. For example, if declared as
	 * {@code T extends Foo}, this method would return Foo.
	 * 
	 * @return supertype, else null if not present
	 */
	TypeTree getSupertype();
	
	/**
	 * Default value of parameter
	 * 
	 * @return default value, else null if not present
	 */
	TypeTree getDefault();
	
	@Override
	default Tree.Kind getKind() {
		return Tree.Kind.TYPE_PARAMETER_DECLARATION;
	}
}
