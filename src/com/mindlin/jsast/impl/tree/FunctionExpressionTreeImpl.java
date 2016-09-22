package com.mindlin.jsast.impl.tree;

import java.util.Collections;
import java.util.List;

import com.mindlin.jsast.tree.FunctionExpressionTree;
import com.mindlin.jsast.tree.IdentifierTree;
import com.mindlin.jsast.tree.ParameterTree;
import com.mindlin.jsast.tree.StatementTree;
import com.mindlin.jsast.tree.Tree;

public class FunctionExpressionTreeImpl extends AbstractTree implements FunctionExpressionTree, StatementTree {
	protected final StatementTree body;
	protected final IdentifierTree name;
	protected final List<ParameterTree> parameters;
	protected final boolean strict;
	protected final boolean arrow;
	protected final boolean generator;
	protected final boolean isStmt;

	public FunctionExpressionTreeImpl(long start, long end, List<ParameterTree> parameters, IdentifierTree name, boolean arrow,
			StatementTree body, boolean strict, boolean generator) {
		super(start, end);
		this.parameters = Collections.unmodifiableList(parameters);
		this.name = name;
		this.arrow = arrow;
		this.body = body;
		this.strict = strict;
		this.generator = generator;
		this.isStmt = generator || (name != null);
	}

	@Override
	public StatementTree getBody() {
		return body;
	}

	@Override
	public IdentifierTree getName() {
		return name;
	}

	@Override
	public List<ParameterTree> getParameters() {
		return parameters;
	}

	@Override
	public boolean isStrict() {
		return strict;
	}

	@Override
	public boolean isArrow() {
		return arrow;
	}

	@Override
	public boolean isGenerator() {
		return generator;
	}

	@Override
	public Tree.Kind getKind() {
		return isStmt ? Kind.FUNCTION : Kind.FUNCTION_EXPRESSION;
	}
}
