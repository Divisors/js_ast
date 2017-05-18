package com.mindlin.jsast.transform;

import com.mindlin.jsast.impl.tree.AssignmentTreeImpl;
import com.mindlin.jsast.impl.tree.BinaryTreeImpl;
import com.mindlin.jsast.impl.tree.CastTreeImpl;
import com.mindlin.jsast.impl.tree.MemberExpressionTreeImpl;
import com.mindlin.jsast.impl.tree.ParenthesizedTreeImpl;
import com.mindlin.jsast.impl.tree.UnaryTreeImpl;
import com.mindlin.jsast.tree.AssignmentTree;
import com.mindlin.jsast.tree.BinaryTree;
import com.mindlin.jsast.tree.CastTree;
import com.mindlin.jsast.tree.ConditionalExpressionTree;
import com.mindlin.jsast.tree.ExpressionTree;
import com.mindlin.jsast.tree.NewTree;
import com.mindlin.jsast.tree.Tree;
import com.mindlin.jsast.tree.UnaryTree;
import com.mindlin.jsast.tree.Tree.Kind;

/**
 * Expression trees (esp. binary expressions) may be in a form that violates
 * precedence. This transformation adds parentheses to fix it.
 * 
 * @author mailmindlin
 */
public class ExpressionFixerTf implements TreeTransformation<ASTTransformerContext> {
	protected int precedence(Tree.Kind kind) {
		switch (kind) {
			case IDENTIFIER:
				return 21;
			case PARENTHESIZED:
				return 20;
			case MEMBER_SELECT:
			case ARRAY_ACCESS:
			case NEW:// Assuming argument list
				return 19;
			case FUNCTION_INVOCATION:
				return 18;
			case POSTFIX_INCREMENT:
			case POSTFIX_DECREMENT:
				return 17;
			case LOGICAL_NOT:
			case BITWISE_NOT:
			case UNARY_PLUS:
			case UNARY_MINUS:
			case PREFIX_INCREMENT:
			case PREFIX_DECREMENT:
			case TYPEOF:
			case VOID:
			case DELETE:
				return 16;
			case EXPONENTIATION:
			case CAST:
				return 15;
			case MULTIPLICATION:
			case DIVISION:
			case REMAINDER:
				return 14;
			case ADDITION:
			case SUBTRACTION:
				return 13;
			case LEFT_SHIFT:
			case RIGHT_SHIFT:
			case UNSIGNED_RIGHT_SHIFT:
				return 12;
			case LESS_THAN:
			case LESS_THAN_EQUAL:
			case GREATER_THAN:
			case GREATER_THAN_EQUAL:
			case IN:
			case INSTANCEOF:
				return 11;
			case EQUAL:
			case NOT_EQUAL:
			case STRICT_EQUAL:
			case STRICT_NOT_EQUAL:
				return 10;
			case BITWISE_AND:
				return 9;
			case BITWISE_XOR:
				return 8;
			case BITWISE_OR:
				return 7;
			case LOGICAL_AND:
				return 6;
			case LOGICAL_OR:
				return 5;
			case CONDITIONAL:
				return 4;
			case ASSIGNMENT:
			case ADDITION_ASSIGNMENT:
			case SUBTRACTION_ASSIGNMENT:
			case EXPONENTIATION_ASSIGNMENT:
			case MULTIPLICATION_ASSIGNMENT:
			case DIVISION_ASSIGNMENT:
			case REMAINDER_ASSIGNMENT:
			case LEFT_SHIFT_ASSIGNMENT:
			case RIGHT_SHIFT_ASSIGNMENT:
			case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
			case BITWISE_AND_ASSIGNMENT:
			case BITWISE_XOR_ASSIGNMENT:
			case BITWISE_OR_ASSIGNMENT:
				return 3;
			case YIELD:
			case YIELD_GENERATOR:
				return 2;
			case SPREAD:
				return 1;
			case SEQUENCE:
				return 0;
			default:
				return 99;
		}
	}
	
	@Override
	public ExpressionTree visitCast(CastTree node, ASTTransformerContext d) {
		ExpressionTree expr = node.getExpression();
		if (precedence(node.getKind()) > precedence(expr.getKind())) {
			expr = new ParenthesizedTreeImpl(expr.getStart(), expr.getEnd(), expr);
			node = new CastTreeImpl(expr, node.getType());
		}
		return node;
	}
	
	@Override
	public ExpressionTree visitConditionalExpression(ConditionalExpressionTree node, ASTTransformerContext d) {
		// TODO Auto-generated method stub
		return TreeTransformation.super.visitConditionalExpression(node, d);
	}
	
	@Override
	public ExpressionTree visitNew(NewTree node, ASTTransformerContext d) {
		// TODO Auto-generated method stub
		return TreeTransformation.super.visitNew(node, d);
	}
	
	@Override
	public ExpressionTree visitAssignment(AssignmentTree node, ASTTransformerContext d) {
		Tree.Kind kind = node.getKind();
		int precedence = precedence(kind);
		ExpressionTree lhs = node.getLeftOperand(), rhs = node.getRightOperand(), oldLhs = lhs, oldRhs = rhs;
		
		if (precedence(lhs.getKind()) < precedence)
			lhs = new ParenthesizedTreeImpl(lhs.getStart(), lhs.getEnd(), lhs);
		if (precedence(rhs.getKind()) < precedence)
			rhs = new ParenthesizedTreeImpl(rhs.getStart(), rhs.getEnd(), rhs);
		
		if (lhs != oldLhs || rhs != oldRhs)
			node = new AssignmentTreeImpl(kind, lhs, rhs);
		
		return node;
	}
	
	@Override
	public ExpressionTree visitBinary(BinaryTree node, ASTTransformerContext d) {
		Tree.Kind kind = node.getKind();
		int precedence = precedence(kind);
		ExpressionTree lhs = node.getLeftOperand(), rhs = node.getRightOperand(), oldLhs = lhs, oldRhs = rhs;
		
		if (precedence(lhs.getKind()) < precedence)
			lhs = new ParenthesizedTreeImpl(lhs.getStart(), lhs.getEnd(), lhs);
		if (precedence(rhs.getKind()) < precedence)
			rhs = new ParenthesizedTreeImpl(rhs.getStart(), rhs.getEnd(), rhs);
		
		if (lhs != oldLhs || rhs != oldRhs) {
			if (kind == Kind.MEMBER_SELECT || kind == Kind.ARRAY_ACCESS)
				node = new MemberExpressionTreeImpl(kind, lhs, rhs);
			else
				node = new BinaryTreeImpl(kind, lhs, rhs);
		}
		
		return node;
	}
	
	@Override
	public ExpressionTree visitUnary(UnaryTree node, ASTTransformerContext d) {
		ExpressionTree expr = node.getExpression();
		
		if (precedence(expr.getKind()) < precedence(node.getKind())) {
			expr = new ParenthesizedTreeImpl(expr.getStart(), expr.getEnd(), expr);
			
			if (node.getKind() == Kind.VOID)
				node = new UnaryTreeImpl.VoidTreeImpl(expr);
			else
				node = new UnaryTreeImpl(node.getStart(), node.getEnd(), expr, node.getKind());
		}
		
		return node;
	}
	
}
