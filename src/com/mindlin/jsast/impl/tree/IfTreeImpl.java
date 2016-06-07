package com.mindlin.jsast.impl.tree;

import com.mindlin.jsast.tree.ExpressionTree;
import com.mindlin.jsast.tree.IfTree;
import com.mindlin.jsast.tree.StatementTree;

public class IfTreeImpl extends AbstractTree implements IfTree {
	ExpressionTree expression;
	StatementTree thenStatement, elseStatement;
	public IfTreeImpl(long start, long end, ExpressionTree expression, StatementTree thenStatement, StatementTree elseStatement) {
		super(start, end);
		this.expression = expression;
		this.thenStatement = thenStatement;
		this.elseStatement = elseStatement;
	}

	@Override
	public ExpressionTree getExpression() {
		return this.expression;
	}

	@Override
	public StatementTree getThenStatement() {
		return this.thenStatement;
	}

	//TODO support nicer else-if stuff (could make optimizations easier)
	@Override
	public StatementTree getElseStatement() {
		return this.elseStatement;
	}
	
}
