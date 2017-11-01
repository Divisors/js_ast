package com.mindlin.jsast.tree;

import com.mindlin.jsast.tree.type.TypeTreeVisitor;

public interface TreeVisitor<R, D> extends StatementTreeVisitor<R, D>, ExpressionTreeVisitor<R, D>, TypeTreeVisitor<R, D> {
	R visitArrayPattern(ArrayPatternTree node, D d);
	R visitAssignmentPattern(AssignmentPatternTree node, D d);
	R visitObjectPattern(ObjectPatternTree node, D d);
	
	R visitClassDeclaration(ClassDeclarationTree node, D d);
	
	R visitComment(CommentNode node, D d);
	R visitCompilationUnit(CompilationUnitTree node, D d);
}
