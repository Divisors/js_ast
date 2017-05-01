package com.mindlin.jsast.impl.writer;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.mindlin.jsast.impl.util.Characters;
import com.mindlin.jsast.tree.ArrayLiteralTree;
import com.mindlin.jsast.tree.ArrayPatternTree;
import com.mindlin.jsast.tree.AssignmentPatternTree;
import com.mindlin.jsast.tree.AssignmentTree;
import com.mindlin.jsast.tree.BinaryTree;
import com.mindlin.jsast.tree.BlockTree;
import com.mindlin.jsast.tree.BooleanLiteralTree;
import com.mindlin.jsast.tree.BreakTree;
import com.mindlin.jsast.tree.CaseTree;
import com.mindlin.jsast.tree.CastTree;
import com.mindlin.jsast.tree.CatchTree;
import com.mindlin.jsast.tree.ClassDeclarationTree;
import com.mindlin.jsast.tree.ClassPropertyTree;
import com.mindlin.jsast.tree.ClassPropertyTree.AccessModifier;
import com.mindlin.jsast.tree.ClassPropertyTree.PropertyDeclarationType;
import com.mindlin.jsast.tree.CommentNode;
import com.mindlin.jsast.tree.CompilationUnitTree;
import com.mindlin.jsast.tree.ComputedPropertyKeyTree;
import com.mindlin.jsast.tree.ConditionalExpressionTree;
import com.mindlin.jsast.tree.ContinueTree;
import com.mindlin.jsast.tree.DebuggerTree;
import com.mindlin.jsast.tree.DoWhileLoopTree;
import com.mindlin.jsast.tree.EmptyStatementTree;
import com.mindlin.jsast.tree.EnumDeclarationTree;
import com.mindlin.jsast.tree.ExportTree;
import com.mindlin.jsast.tree.ExpressionStatementTree;
import com.mindlin.jsast.tree.ExpressionTree;
import com.mindlin.jsast.tree.ForEachLoopTree;
import com.mindlin.jsast.tree.ForLoopTree;
import com.mindlin.jsast.tree.FunctionCallTree;
import com.mindlin.jsast.tree.FunctionExpressionTree;
import com.mindlin.jsast.tree.IdentifierTree;
import com.mindlin.jsast.tree.IfTree;
import com.mindlin.jsast.tree.ImportTree;
import com.mindlin.jsast.tree.InterfaceDeclarationTree;
import com.mindlin.jsast.tree.InterfacePropertyTree;
import com.mindlin.jsast.tree.LabeledStatementTree;
import com.mindlin.jsast.tree.LiteralTree;
import com.mindlin.jsast.tree.MethodDefinitionTree;
import com.mindlin.jsast.tree.NewTree;
import com.mindlin.jsast.tree.NullLiteralTree;
import com.mindlin.jsast.tree.NumericLiteralTree;
import com.mindlin.jsast.tree.ObjectLiteralPropertyTree;
import com.mindlin.jsast.tree.ObjectLiteralTree;
import com.mindlin.jsast.tree.ObjectPatternTree;
import com.mindlin.jsast.tree.ObjectPropertyKeyTree;
import com.mindlin.jsast.tree.ParameterTree;
import com.mindlin.jsast.tree.ParenthesizedTree;
import com.mindlin.jsast.tree.PatternTree;
import com.mindlin.jsast.tree.RegExpLiteralTree;
import com.mindlin.jsast.tree.ReturnTree;
import com.mindlin.jsast.tree.SequenceTree;
import com.mindlin.jsast.tree.StatementTree;
import com.mindlin.jsast.tree.StringLiteralTree;
import com.mindlin.jsast.tree.SuperExpressionTree;
import com.mindlin.jsast.tree.SwitchTree;
import com.mindlin.jsast.tree.TemplateLiteralTree;
import com.mindlin.jsast.tree.ThisExpressionTree;
import com.mindlin.jsast.tree.ThrowTree;
import com.mindlin.jsast.tree.Tree;
import com.mindlin.jsast.tree.Tree.Kind;
import com.mindlin.jsast.tree.TreeVisitor;
import com.mindlin.jsast.tree.TryTree;
import com.mindlin.jsast.tree.TypeTree;
import com.mindlin.jsast.tree.UnaryTree;
import com.mindlin.jsast.tree.VariableDeclarationTree;
import com.mindlin.jsast.tree.VariableDeclaratorTree;
import com.mindlin.jsast.tree.WhileLoopTree;
import com.mindlin.jsast.tree.WithTree;
import com.mindlin.jsast.tree.type.AnyTypeTree;
import com.mindlin.jsast.tree.type.ArrayTypeTree;
import com.mindlin.jsast.tree.type.FunctionTypeTree;
import com.mindlin.jsast.tree.type.GenericRefTypeTree;
import com.mindlin.jsast.tree.type.GenericTypeTree;
import com.mindlin.jsast.tree.type.IdentifierTypeTree;
import com.mindlin.jsast.tree.type.IndexTypeTree;
import com.mindlin.jsast.tree.type.InterfaceTypeTree;
import com.mindlin.jsast.tree.type.IntersectionTypeTree;
import com.mindlin.jsast.tree.type.MemberTypeTree;
import com.mindlin.jsast.tree.type.ParameterTypeTree;
import com.mindlin.jsast.tree.type.TupleTypeTree;
import com.mindlin.jsast.tree.type.UnionTypeTree;
import com.mindlin.jsast.tree.type.VoidTypeTree;
import com.mindlin.jsast.writer.JSWriter;
import com.mindlin.jsast.writer.JSWriterOptions;

public class JSWriterImpl implements JSWriter, TreeVisitor<Void, JSWriterImpl.WriterHelper> {
	protected final JSWriterOptions options;
	public JSWriterImpl(JSWriterOptions options) {
		//TODO clone
		this.options = options;
	}
	
	@Override
	public void write(CompilationUnitTree tree, Writer output) throws IOException {
		try {
			WriterHelper wh = new WriterHelper(output);
			tree.accept(this, wh);
		} catch (RuntimeException e) {
			if (e.getCause() != null)
				throw (IOException) e.getCause();
			throw e;
		}
	}
	
	void writeTypeMaybe(TypeTree type, WriterHelper out) {
		if (type == null || type.isImplicit())
			return;
		out.append(':').optionalSpace();
		type.accept(this, out);
	}
	
	protected class WriterHelper implements Closeable {
		protected final Writer parent;
		protected int indentLevel = options.baseIndent;
		private String indent = "";
		protected int newlineBacklog = 0;
		protected Stack<WriterHelperContext> context = new Stack<>();
		
		public WriterHelper(Writer parent) {
			this.parent = parent;
			context.push(new WriterHelperContext());
		}
		
		public void beginRegion(long srcStart) {
			//TODO impl
		}
		
		public void endRegion(long srcEnd) {
			
		}
		
		public void pushIndent() {
			indentLevel++;
			indent += options.indentStyle;
		}
		
		public void popIndent() {
			if (--indentLevel < options.baseIndent) {
				indentLevel++;
				return;
			}
			indent = indent.substring(0, indent.length() - options.indentStyle.length());
		}
		
		public void pushContext() {
			this.context.push(new WriterHelperContext(this.context.peek()));
		}
		
		public void popContext() {
			this.context.pop();
		}
		
		public void doFinishWithNewline(boolean enableNewline) {
			this.context.peek().noNewline = !enableNewline;
		}
		
		public void finishStatement(boolean semicolon) {
			if (this.context.peek().noNewline)
				return;
			if (semicolon)
				this.append(';');
			this.newline();
		}
		
		@Override
		public void close() {
			try {
				parent.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void flush() {
			flushNewlines();
			try {
				parent.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		public WriterHelper append(long srcStart, long srcEnd, String s) {
			flushNewlines();
			try {
				parent.append(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return this;
		}

		public WriterHelper append(char c) {
			flushNewlines();
			try {
				parent.append(c);
				return this;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		public WriterHelper append(CharSequence csq, int start, int end) {
			flushNewlines();
			return doAppend(csq, start, end);
		}
		
		public WriterHelper append(CharSequence csq) {
			flushNewlines();
			return doAppend(csq, 0, csq.length());
		}
		
		protected void flushNewlines() {
			if (this.newlineBacklog == 0)
				return;
			String newline = "\n" + indent;
			while (this.newlineBacklog > 0) {
				this.newlineBacklog--;
				doAppend(newline, 0, newline.length());
			}
		}
		
		protected WriterHelper doAppend(CharSequence csq, int start, int end) {
			try {
				parent.append(csq, start, end);
				return this;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		public WriterHelper newline() {
			this.newlineBacklog++;
//			this.append("\n" + indent);
			return this;
		}
		
		public WriterHelper optionalSpace() {
			append(options.space);
			return this;
		}
		
		protected class WriterHelperContext {
			boolean noNewline = false;
			
			protected WriterHelperContext() {
				
			}
			
			protected WriterHelperContext(WriterHelperContext parent) {
				this.noNewline = parent.noNewline;
			}
		}
	}

	protected String stringify(Number value) {
		double dValue = value.doubleValue();
		if (!Double.isFinite(dValue)) {
			if (Double.isNaN(dValue))
				return "NaN";
			if (Double.isInfinite(dValue))
				return (dValue == Double.POSITIVE_INFINITY) ? "Infinity" : "-Infinity";
			throw new IllegalArgumentException("Unknown non-finite value " + value);
		}
		
		String result = value.toString();
		if (result.length() < 3)
			return result;
		
		//TODO finish
		return result;
	}
	
	@Override
	public Void visitAnyType(AnyTypeTree node, WriterHelper out) {
		out.beginRegion(node.getStart());
		out.append("any");
		out.endRegion(node.getEnd());
		return null;
	}

	@Override
	public Void visitArrayLiteral(ArrayLiteralTree node, WriterHelper out) {
		out.beginRegion(node.getStart());
		out.append('[');
		boolean isFirst = true;
		for (ExpressionTree element : node.getElements()) {
			if (!isFirst)
				out.append(',').optionalSpace();
			isFirst = false;
			if (element != null)
				element.accept(this, out);
		}
		out.append(']');
		out.endRegion(node.getEnd());
		return null;
	}

	@Override
	public Void visitArrayPattern(ArrayPatternTree node, WriterHelper out) {
		out.append('[');
		boolean isFirst = true;
		for (PatternTree element : node.getElements()) {
			if (!isFirst)
				out.append(',');
			isFirst = false;
			if (element != null)
				element.accept(this, out);
		}
		out.append(']');
		return null;
	}

	@Override
	public Void visitArrayType(ArrayTypeTree node, WriterHelper out) {
		node.getBaseType().accept(this, out);
		out.append("[]");
		return null;
	}

	@Override
	public Void visitAssignment(AssignmentTree node, WriterHelper out) {
		node.getLeftOperand().accept(this, out);
		out.optionalSpace().append('=').optionalSpace();
		node.getRightOperand().accept(this, out);
		return null;
	}

	@Override
	public Void visitAssignmentPattern(AssignmentPatternTree node, WriterHelper out) {
		node.getLeft().accept(this, out);
		out.append('=');
		node.getRight().accept(this, out);
		return null;
	}

	@Override
	public Void visitBinary(BinaryTree node, WriterHelper out) {
		node.getLeftOperand().accept(this, out);
		String operator;
		boolean optionalSpace = true;
		switch (node.getKind()) {
			case ADDITION:
				operator =  "+";
				break;
			case ADDITION_ASSIGNMENT:
				operator = "+=";
				break;
			case ARRAY_ACCESS:
				out.append('[');
				node.getRightOperand().accept(this, out);
				out.append(']');
				return null;
			case MEMBER_SELECT:
				operator = ".";
				optionalSpace = false;
				break;
			case BITWISE_AND:
				operator = "&";
				break;
			case BITWISE_AND_ASSIGNMENT:
				operator = "&=";
				break;
			case BITWISE_OR:
				operator = "|";
				break;
			case BITWISE_OR_ASSIGNMENT:
				operator = "|=";
				break;
			case BITWISE_XOR:
				operator = "^";
				break;
			case BITWISE_XOR_ASSIGNMENT:
				operator = "^=";
				break;
			case DIVISION:
				operator = "/";
				break;
			case DIVISION_ASSIGNMENT:
				operator = "/=";
				break;
			case EQUAL:
				operator = "==";
				break;
			case EXPONENTIATION:
				operator = "**";
				break;
			case EXPONENTIATION_ASSIGNMENT:
				operator = "**=";
				break;
			case GREATER_THAN:
				operator = ">";
				break;
			case GREATER_THAN_EQUAL:
				operator = "<";
				break;
			case IN:
				operator = " in ";
				optionalSpace = false;
				break;
			case INSTANCEOF:
				operator = " instanceof ";
				optionalSpace = false;
				break;
			case LEFT_SHIFT:
				operator = "<<";
				break;
			case LEFT_SHIFT_ASSIGNMENT:
				operator = "<<=";
				break;
			case LESS_THAN:
				operator = "<";
				break;
			case LESS_THAN_EQUAL:
				operator = "<=";
				break;
			case LOGICAL_AND:
				operator = "&&";
				break;
			case LOGICAL_OR:
				operator = "||";
				break;
			case MULTIPLICATION:
				operator = "*";
				break;
			case MULTIPLICATION_ASSIGNMENT:
				operator = "*=";
				break;
			case NOT_EQUAL:
				operator = "!=";
				break;
			case REMAINDER:
				operator = "%";
				break;
			case REMAINDER_ASSIGNMENT:
				operator = "%=";
				break;
			case RIGHT_SHIFT:
				operator = ">>";
				break;
			case RIGHT_SHIFT_ASSIGNMENT:
				operator = ">>=";
				break;
			case STRICT_EQUAL:
				operator = "===";
				break;
			case STRICT_NOT_EQUAL:
				operator = "!==";
				break;
			case SUBTRACTION:
				operator = "-";
				break;
			case SUBTRACTION_ASSIGNMENT:
				operator = "-=";
				break;
			case UNSIGNED_RIGHT_SHIFT:
				operator = ">>>";
				break;
			case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
				operator = ">>>=";
				break;
			default:
				throw new IllegalArgumentException();
		}
		if (optionalSpace)
			out.optionalSpace().append(operator).optionalSpace();
		else
			out.append(operator);
		node.getRightOperand().accept(this, out);
		return null;
	}

	@Override
	public Void visitBlock(BlockTree node, WriterHelper out) {
		out.append('{');
		out.pushIndent();
		out.newline();
		
		out.pushContext();
		out.doFinishWithNewline(true);
		for (StatementTree statement : node.getStatements())
			statement.accept(this, out);
		out.popContext();
		
		out.popIndent();
		out.append('}');
		out.finishStatement(false);
		
		return null;
	}

	@Override
	public Void visitBooleanLiteral(BooleanLiteralTree node, WriterHelper out) {
		out.append(node.getValue().toString());
		return null;
	}

	@Override
	public Void visitBreak(BreakTree node, WriterHelper out) {
		if (node.getLabel() == null)
			out.append("break");
		else
			out.append("break " + node.getLabel());
		out.finishStatement(true);
		return null;
	}
	
	@Override
	public Void visitCast(CastTree node, WriterHelper out) {
		ExpressionTree expression = node.getExpression();
		TypeTree type = node.getType();
		
		expression.accept(this, out);
		
		if (type.isImplicit())
			return null;
		
		out.append(options.space).append("as").append(options.space);
		type.accept(this, out);
		return null;
	}

	@Override
	public Void visitClassDeclaration(ClassDeclarationTree node, WriterHelper out) {
		out.append("class");
		if (node.getIdentifier() != null) {
			out.append(' ');
			node.getIdentifier().accept(this, out);
		}
		if (node.getSuperType().isPresent()) {
			out.append(" extends ");
			node.getSuperType().get().accept(this, out);
		}
		if (!node.getImplementing().isEmpty()) {
			out.append(" implements ");
			boolean isFirst = true;
			for (TypeTree iface : node.getImplementing()) {
				if (!isFirst)
					out.append(',');
				isFirst = false;
				iface.accept(this, out);
			}
		}
		
		out.append('{');
		for (ClassPropertyTree<?> property : node.getProperties()) {
			//TODO finish
			throw new UnsupportedOperationException();
		}
		out.append('}');
		return null;
	}

	@Override
	public Void visitComment(CommentNode node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitCompilationUnit(CompilationUnitTree node, WriterHelper out) {
		for (StatementTree statement : node.getSourceElements())
			statement.accept(this, out);
		return null;
	}

	@Override
	public Void visitComputedPropertyKey(ComputedPropertyKeyTree node, WriterHelper out) {
		out.append('[');
		node.getExpression().accept(this, out);
		out.append(']');
		return null;
	}

	@Override
	public Void visitConditionalExpression(ConditionalExpressionTree node, WriterHelper out) {
		node.getCondition().accept(this, out);
		out.optionalSpace().append('?').optionalSpace();
		node.getTrueExpression().accept(this, out);
		out.optionalSpace().append(':').optionalSpace();
		node.getFalseExpression().accept(this, out);
		return null;
	}

	@Override
	public Void visitContinue(ContinueTree node, WriterHelper out) {
		if (node.getLabel() == null) {
			out.append("continue");
			out.finishStatement(true);
			return null;
		}
		out.append("continue ").append(node.getLabel());
		out.finishStatement(true);
		return null;
	}

	@Override
	public Void visitDebugger(DebuggerTree node, WriterHelper out) {
		out.append("debugger").finishStatement(true);
		return null;
	}

	@Override
	public Void visitDoWhileLoop(DoWhileLoopTree node, WriterHelper out) {
		out.append("do");
		
		StatementTree statement = node.getStatement();
		
		out.pushContext();
		if (statement.getKind() == Kind.BLOCK) {
			out.optionalSpace();
			out.doFinishWithNewline(false);
		} else {
			out.newline();
			out.pushIndent();
		}
		
		statement.accept(this, out);
		
		if (statement.getKind() == Kind.BLOCK) {
			out.optionalSpace();
		} else {
			out.popIndent();
			out.newline();
		}
		out.popContext();
		
		out.append("while(");
		node.getCondition().accept(this, out);
		out.append(")").finishStatement(true);
		return null;
	}

	@Override
	public Void visitEmptyStatement(EmptyStatementTree node, WriterHelper out) {
		out.finishStatement(true);
		return null;
	}

	@Override
	public Void visitEnumDeclaration(EnumDeclarationTree node, WriterHelper out) {
		out.append("enum ");
		node.getIdentifier().accept(this, out);
		out.append('{');
		// TODO Auto-generated method stub
		out.append('}');
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitExport(ExportTree node, WriterHelper out) {
		out.append("export").append(options.space);
		
		out.pushContext();
		out.doFinishWithNewline(false);
		node.getExpression().accept(this, out);
		out.popContext();
		
		out.finishStatement(true);
		return null;
	}

	@Override
	public Void visitExpressionStatement(ExpressionStatementTree node, WriterHelper out) {
		node.getExpression().accept(this, out);
		out.finishStatement(true);
		return null;
	}

	@Override
	public Void visitForEachLoop(ForEachLoopTree node, WriterHelper out) {
		out.append("for(");
		node.getVariable().accept(this, out);
		if (node.getKind() == Tree.Kind.FOR_IN_LOOP)
			out.append(" in ");
		else if (node.getKind() == Tree.Kind.FOR_OF_LOOP)
			out.append(" of ");
		else
			throw new IllegalArgumentException("Can only process for/in and for/of loops");
		
		node.getExpression().accept(this, out);
		out.append(')');
		node.getStatement().accept(this, out);
		return null;
	}

	@Override
	public Void visitForLoop(ForLoopTree node, WriterHelper out) {
		out.append("for(");
		
		StatementTree initializer = node.getInitializer();
		if (initializer != null) {
			out.pushContext();
			out.doFinishWithNewline(false);
			initializer.accept(this, out);
			out.popContext();
		}
		out.append(';').optionalSpace();
		
		ExpressionTree condition = node.getCondition();
		if (condition != null)
			condition.accept(this, out);
		out.append(';').optionalSpace();
		
		ExpressionTree update = node.getUpdate();
		if (update != null)
			update.accept(this, out);
		out.append(')');
		
		StatementTree statement = node.getStatement();
		if (statement.getKind() == Kind.BLOCK)//Optional space between ')' in for header and '{' in block
			out.optionalSpace();
		statement.accept(this, out);
		
		return null;
	}

	@Override
	public Void visitFunctionCall(FunctionCallTree node, WriterHelper out) {
		node.getCallee().accept(this, out);
		out.append('(');
		boolean isFirst = true;
		for (ExpressionTree arg : node.getArguments()) {
			if (!isFirst)
				out.append(',').optionalSpace();
			isFirst = false;
			arg.accept(this, out);
		}
		out.append(')');
		return null;
	}

	protected void writeFunctionParameters(FunctionExpressionTree node, WriterHelper out) {
		List<ParameterTree> params = node.getParameters();
		if (node.isArrow() && params.size() == 1) {
			ParameterTree param0 = params.get(0);
			if (!param0.isOptional() && param0.getInitializer() == null && !param0.isRest() && (param0.getType() == null || param0.getType().isImplicit())) {
				//We don't have to write parentheses
				param0.accept(this, out);
				return;
			}
		}
		out.append('(');
		boolean isFirst = true;
		for (ParameterTree param : params) {
			if (!isFirst)
				out.append(',');
			isFirst = false;
			param.accept(this, out);
		}
		out.append(')');
	}
	
	protected void writeFunctionBody(BlockTree body, WriterHelper out) {
		
	}
	
	@Override
	public Void visitFunctionExpression(FunctionExpressionTree node, WriterHelper out) {
		if (!node.isArrow()) {
			out.append("function");
			if (node.getName() != null) {
				out.append(' ');
				node.getName().accept(this, out);
			}
		}
		
		//Write parameters
		this.writeFunctionParameters(node, out);
		
		if (node.getReturnType() != null && !node.getReturnType().isImplicit()) {
			out.optionalSpace().append(':').optionalSpace();
			node.getReturnType().accept(this, out);
		}
		
		out.optionalSpace();
		
		if (node.isArrow()) {
			out.append("=>");
			out.optionalSpace();
			if (node.getBody().getKind() == Kind.RETURN)
				((ReturnTree) node.getBody()).getExpression().accept(this, out);
			else
				node.getBody().accept(this, out);
		}
		
		out.pushContext();
		out.doFinishWithNewline(false);
		node.getBody().accept(this, out);
		out.popContext();
		return null;
	}

	@Override
	public Void visitFunctionType(FunctionTypeTree node, WriterHelper out) {
		throw new UnsupportedOperationException();
		// TODO Auto-generated method stub
	}

	@Override
	public Void visitGenericRefType(GenericRefTypeTree node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitGenericType(GenericTypeTree node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitIdentifier(IdentifierTree node, WriterHelper out) {
		//TODO check if correct
		out.append(node.getName());
		return null;
	}

	@Override
	public Void visitIdentifierType(IdentifierTypeTree node, WriterHelper out) {
		node.getIdentifier().accept(this, out);
		if (node.getGenerics().isEmpty())
			return null;
		
		out.append('<');
		boolean isFirst = true;
		for (TypeTree type : node.getGenerics()) {
			if (!isFirst)
				out.append(',');
			isFirst = false;
			type.accept(this, out);
		}
		out.append('>');
		return null;
	}

	@Override
	public Void visitIf(IfTree node, WriterHelper out) {
		out.append("if(");
		node.getExpression().accept(this, out);
		out.append(')');
		
		StatementTree thenStmt = node.getThenStatement();
		StatementTree elseStmt = node.getElseStatement();
		
		if (elseStmt != null && elseStmt.getKind() == Kind.EMPTY_STATEMENT)
			elseStmt = null;
		
		if (thenStmt.getKind() == Kind.BLOCK) {
			out.optionalSpace();

			out.pushContext();
			if (elseStmt != null)
				out.doFinishWithNewline(false);
			
			thenStmt.accept(this, out);
			
			out.popContext();
			
			if (elseStmt != null)
				out.optionalSpace();
		} else {
			thenStmt.accept(this, out);
		}
		
		if (elseStmt == null || elseStmt.getKind() == Kind.EMPTY_STATEMENT)
			return null;
		out.append("else");
		if (elseStmt.getKind() == Kind.IF)
			out.append(options.space);
		else if (elseStmt.getKind() == Kind.BLOCK)
			out.optionalSpace();
		elseStmt.accept(this, out);
		return null;
	}

	@Override
	public Void visitImport(ImportTree node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitIndexType(IndexTypeTree node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitInterfaceDeclaration(InterfaceDeclarationTree node, WriterHelper out) {
		
		out.append("interface");
		if (node.getIdentifier() != null) {
			out.append(options.space);
			node.getIdentifier().accept(this, out);
		}
		//TODO what if no identifier but yes supertypes
		if (node.getSupertypes() != null && !node.getSupertypes().isEmpty()) {
			out.append(options.space).append("extends");
			out.append(options.space);
			boolean isFirst = true;
			for (TypeTree superType : node.getSupertypes()) {
				if (!isFirst)
					out.append(',').optionalSpace();
				isFirst = false;
				superType.accept(this, out);
			}
		}
		out.optionalSpace().append('{');
		out.pushIndent();
		out.newline();
		for (InterfacePropertyTree property : node.getProperties()) {
			if (property.isReadonly())
				out.append("readonly").append(options.space);
			
			if (property.getKey() == null) {
				//Is function interface
				out.append('(');
				FunctionTypeTree type = (FunctionTypeTree) property.getType();
				boolean isFirst = true;
				for (ParameterTree param : type.getParameters()) {
					if (!isFirst)
						out.append(',').optionalSpace();
					isFirst = false;
					param.accept(this, out);
				}
				out.append(')').append(':').optionalSpace();
				type.getReturnType().accept(this, out);
			} else if (property.getType().getKind() == Kind.INDEX_TYPE) {
				//TODO finish
				throw new UnsupportedOperationException();
			} else {
				//TODO finish
				throw new UnsupportedOperationException();
			}
		}
		out.popIndent();
		out.newline();
		out.append('}');
		out.finishStatement(false);
		return null;
	}

	@Override
	public Void visitInterfaceType(InterfaceTypeTree node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitIntersectionType(IntersectionTypeTree node, WriterHelper out) {
		node.getLeftType().accept(this, out);
		out.append('&');
		node.getRightType().accept(this, out);
		return null;
	}

	@Override
	public Void visitLabeledStatement(LabeledStatementTree node, WriterHelper out) {
		node.getName().accept(this, out);
		out.append(':');
		StatementTree stmt = node.getStatement();
		stmt.accept(this, out);
		return null;
	}

	@Override
	public Void visitLiteral(LiteralTree<?> node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitMemberType(MemberTypeTree node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitMethodDefinition(MethodDefinitionTree node, WriterHelper out) {
		//Write access modifier; public is implied
		if (node.getAccess() == AccessModifier.PROTECTED)
			out.append("protected ");
		else if (node.getAccess() == AccessModifier.PRIVATE)
			out.append("private ");
		
		if (node.isStatic())
			out.append("static ");

		//Pretty sure that 'readonly' isn't valid
		
		
		switch (node.getDeclarationType()) {
			case CONSTRUCTOR:
				out.append("constructor");
				break;
			case GETTER:
				out.append("get ");
				break;
			case SETTER:
				out.append("set ");
				break;
			case METHOD:
				break;
			default:
				throw new IllegalArgumentException();
		}
		
		if (node.getDeclarationType() != PropertyDeclarationType.CONSTRUCTOR)
			node.getKey().accept(this, out);
		
		//TODO check that this is right
		node.getValue().accept(this, out);
		return null;
	}

	@Override
	public Void visitNew(NewTree node, WriterHelper out) {
		out.append("new ");
		node.getCallee().accept(this, out);
		out.append('(');
		boolean isFirst = true;
		for (ExpressionTree expr : node.getArguments()) {
			if (!isFirst)
				out.append(',');
			isFirst = false;
			expr.accept(this, out);
		}
		out.append(')');
		return null;
	}

	@Override
	public Void visitNull(NullLiteralTree node, WriterHelper out) {
		out.append("null");
		return null;
	}

	@Override
	public Void visitNumericLiteral(NumericLiteralTree node, WriterHelper out) {
		Number value = node.getValue();
		//TODO compress hex numbers, if possible
		out.append(value.toString());
		return null;
	}

	@Override
	public Void visitObjectLiteral(ObjectLiteralTree node, WriterHelper out) {
		out.append('{');
		boolean isFirst = true;
		for (ObjectLiteralPropertyTree property : node.getProperties()) {
			if (!isFirst)
				out.append(',');
			isFirst = false;
			if (property.getKind() == Kind.METHOD_DEFINITION) {
				MethodDefinitionTree method = (MethodDefinitionTree) property;
				FunctionExpressionTree fn = method.getValue();
				if (fn.isGenerator())
					out.append("* ");
				else if (method.getDeclarationType() == PropertyDeclarationType.GETTER)
					out.append("get ");
				else if (method.getDeclarationType() == PropertyDeclarationType.SETTER)
					out.append("set ");
				method.getKey().accept(this, out);
				out.append('(');
				{
					boolean isFirstParam = true;
					for (ParameterTree param : fn.getParameters()) {
						if (!isFirstParam)
							out.append(',');
						isFirstParam = false;
						param.accept(this, out);
					}
				}
				out.append(')');
				this.writeTypeMaybe(fn.getReturnType(), out);
				fn.getBody().accept(this, out);
			} else {
				ObjectPropertyKeyTree key = property.getKey();
				key.accept(this, out);
				if (!key.equals(property.getValue())) {
					out.append(':');
					property.getValue().accept(this, out);
				}
			}
		}
		out.append('}');
		return null;
	}

	@Override
	public Void visitObjectPattern(ObjectPatternTree node, WriterHelper out) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitParameter(ParameterTree node, WriterHelper out) {
		if (node.isRest())
			out.append("...");
		node.getIdentifier().accept(this, out);
		if (node.isOptional())
			out.append('?');
		this.writeTypeMaybe(node.getType(), out);
		if (node.getInitializer() != null) {
			out.optionalSpace().append('=').optionalSpace();
			node.getInitializer().accept(this, out);
		}
		return null;
	}

	@Override
	public Void visitParameterType(ParameterTypeTree node, WriterHelper out) {
		node.getName().accept(this, out);
		if (node.isOptional())
			out.append("?");
		if (node.getType() != null && !node.getType().isImplicit()) {
			out.append(':');
			node.getType().accept(this, out);
		}
		return null;
	}

	@Override
	public Void visitParentheses(ParenthesizedTree node, WriterHelper out) {
		out.append('(');
		node.getExpression().accept(this, out);
		out.append(')');
		return null;
	}

	@Override
	public Void visitRegExpLiteral(RegExpLiteralTree node, WriterHelper out) {
		System.out.println(Arrays.toString(node.getValue()));
		//TODO escape correctly
		out.append('/');
		out.append(node.getValue()[0]);
		out.append('/');
		out.append(node.getValue()[1]);
		return null;
	}

	@Override
	public Void visitReturn(ReturnTree node, WriterHelper out) {
		if (node.getExpression() == null) {
			out.append("return").finishStatement(true);
			return null;
		}
		out.append("return ");
		node.getExpression().accept(this, out);
		out.finishStatement(true);
		return null;
	}

	@Override
	public Void visitSequence(SequenceTree node, WriterHelper out) {
		boolean isFirst = true;
		for (ExpressionTree expr : node.getExpressions()) {
			if (!isFirst)
				out.append(',').optionalSpace();
			isFirst = false;
			expr.accept(this, out);
		}
		return null;
	}

	@Override
	public Void visitStringLiteral(StringLiteralTree node, WriterHelper out) {
		String value = node.getValue();
		boolean singleQuotes = value.indexOf('"') >= 0;
		out.append(singleQuotes ? '\'' : '"');
		for (int i = 0, l = value.length(); i < l; i++) {
			char c = value.charAt(i);
			boolean escape = false;
			switch (c) {
				case '\t':
					c = 't';
					break;
				case '\b':
					c = 'b';
					break;
				case '\r':
					c = 'r';
					break;
				case '\n':
					c = 'n';
					break;
				case Characters.VT:
					c = 'v';
					break;
				case '\f':
					c = 'f';
					break;
				case '\0':
					c = '0';
					break;
				case '\\':
					break;
				case '\'':
					if (singleQuotes)
						break;
				default:
					out.append(c);
					continue;
			}
			out.append('\\');
			out.append(c);
		}
		out.append(singleQuotes ? '\'' : '"');
		return null;
	}

	@Override
	public Void visitSuper(SuperExpressionTree node, WriterHelper out) {
		out.append("super");
		return null;
	}

	@Override
	public Void visitSwitch(SwitchTree node, WriterHelper out) {
		out.append("switch(");
		node.getExpression().accept(this, out);
		out.append("){");
		
		boolean isFirst = true;
		for (CaseTree caseStmt : node.getCases()) {
			if (!isFirst)
				out.append(' ');
			isFirst = false;
			ExpressionTree expr = caseStmt.getExpression();
			if (expr == null) {
				out.append("default:");
			} else {
				out.append("case ");
				expr.accept(this, out);
				out.append(':');
			}
			for (StatementTree stmt : caseStmt.getStatements())
				stmt.accept(this, out);
		}
		out.append('}');
		return null;
	}

	@Override
	public Void visitThis(ThisExpressionTree node, WriterHelper out) {
		out.append("this");
		return null;
	}

	@Override
	public Void visitThrow(ThrowTree node, WriterHelper out) {
		out.append("throw ");
		node.getExpression().accept(this, out);
		out.finishStatement(true);
		return null;
	}

	@Override
	public Void visitTry(TryTree node, WriterHelper out) {
		out.append("try");
		node.getBlock().accept(this, out);
		if (!node.getCatches().isEmpty())
			for (CatchTree ct : node.getCatches()) {
				out.append("catch(");
				ct.getParameter().accept(this, out);
				TypeTree ctParamType = ct.getType();
				if (ctParamType != null && !ctParamType.isImplicit()) {
					out.append(':');
					ctParamType.accept(this, out);
				}
				out.append(')');
				
				ct.getBlock().accept(this, out);
			}
		if (node.getFinallyBlock() != null) {
			out.append("finally");
			node.getFinallyBlock().accept(this, out);
		}
		return null;
	}

	@Override
	public Void visitTupleType(TupleTypeTree node, WriterHelper out) {
		out.append('[');
		boolean isFirst = true;
		for (TypeTree type : node.getSlotTypes()) {
			if (!isFirst)
				out.append(',');
			isFirst = false;
			type.accept(this, out);
		}
		out.append(']');
		return null;
	}

	@Override
	public Void visitUnary(UnaryTree node, WriterHelper out) {
		//TODO replace out.append(x) with a variable
		switch (node.getKind()) {
			case PREFIX_INCREMENT:
				out.append("++");
				break;
			case PREFIX_DECREMENT:
				out.append("--");
				break;
			case TYPEOF:
				out.append("typeof ");
				break;
			case VOID:
				out.append("void");
				 if (node.getExpression() == null) {
					 return null;
				 }
				 out.append(' ');
				 break;
			case DELETE:
				out.append("delete ");
				break;
			case UNARY_PLUS:
				out.append('+');
				break;
			case UNARY_MINUS:
				out.append('-');
				break;
			case LOGICAL_NOT:
				out.append('!');
				break;
			case BITWISE_NOT:
				out.append('~');
				break;
			case POSTFIX_INCREMENT:
				node.getExpression().accept(this, out);
				out.append("++");
				return null;
			case POSTFIX_DECREMENT:
				node.getExpression().accept(this, out);
				out.append("--");
				return null;
			default:
				throw new IllegalArgumentException("Unknown operator type: " + node.getKind());
		}
		node.getExpression().accept(this, out);
		return null;
	}

	@Override
	public Void visitUnionType(UnionTypeTree node, WriterHelper out) {
		node.getLeftType().accept(this, out);
		out.append('|');
		node.getRightType().accept(this, out);
		return null;
	}

	@Override
	public Void visitVariableDeclaration(VariableDeclarationTree node, WriterHelper out) {
		if (node.isConst())
			out.append("const");
		else if (node.isScoped())
			out.append("let");
		else
			out.append("var");
		
		out.append(options.space);
		
		boolean isFirstDeclaration = true;
		for (VariableDeclaratorTree declarator : node.getDeclarations()) {
			if (!isFirstDeclaration)
				out.append(',').optionalSpace();
			isFirstDeclaration = false;
			
			declarator.getIdentifier().accept(this, out);
			
			TypeTree type = declarator.getType();
			if (type != null && !type.isImplicit()) {
				out.append(':').optionalSpace();
				type.accept(this, out);
			}
			
			ExpressionTree initializer = declarator.getIntitializer();
			if (initializer != null) {
				out.optionalSpace().append('=').optionalSpace();
				initializer.accept(this, out);
			}
		}
		out.finishStatement(true);
		return null;
	}

	@Override
	public Void visitVoidType(VoidTypeTree node, WriterHelper out) {
		if (node.isImplicit())
			return null;
		out.append("void");
		return null;
	}

	@Override
	public Void visitWhileLoop(WhileLoopTree node, WriterHelper out) {
		out.append("while(");
		node.getCondition().accept(this, out);
		out.append(")");
		StatementTree statement = node.getStatement();
		if (statement.getKind() == Kind.BLOCK) {
			out.optionalSpace();
			statement.accept(this, out);
		} else {
			out.pushIndent();
			statement.accept(this, out);
			out.popIndent();
		}
		return null;
	}

	@Override
	public Void visitWith(WithTree node, WriterHelper out) {
		out.append("with(");
		node.getScope().accept(this, out);
		out.append(")");
		node.getStatement().accept(this, out);
		return null;
	}

	@Override
	public Void visitTemplateLiteral(TemplateLiteralTree node, WriterHelper d) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
