package br.ufpe.cin.if688.minijava.grammar;

import br.ufpe.cin.if688.minijava.ast.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.RuleContext.*;

import java.util.*;

public class Lcgm_esvm_ASTVisitor implements Lcgm_esvmVisitor<Object> {

	public Object visitGoal(Lcgm_esvmParser.GoalContext ctx) {
		int numChildren = ctx.getChildCount();
		MainClass main = ((MainClass)ctx.getChild(0).accept(this));
		ClassDeclList classes = new ClassDeclList();

		// From [1, length-1] to avoid main class and EOF
		for(int i = 1 ; i < (numChildren-1) ; ++i){
			classes.addElement( ((ClassDecl)ctx.getChild(i).accept(this)) );
		}

		Program AST = new Program(main, classes);
		return AST;
	}

	public Object visitMain_class_decl(Lcgm_esvmParser.Main_class_declContext ctx) {
		String className = ctx.classId.getText();
		Identifier classIdentifier = new Identifier(className);

		String argsName = ctx.argsId.getText();
		Identifier argsIdentifier = new Identifier(argsName);

		Object body = ctx.bodyStatements.accept(this);
		
		MainClass main = new MainClass(classIdentifier, argsIdentifier, ((Statement)body));
		return main;
	}

	public Object visitMethod_decl(Lcgm_esvmParser.Method_declContext ctx) {
		Type returnType = ((Type)ctx.returnType.accept(this));
		String methodName = ctx.methodId.getText();
		Identifier methodId = new Identifier(methodName);
		FormalList parameters = new FormalList();
		VarDeclList vars = new VarDeclList();
		StatementList statements = new StatementList();
		Exp returnExpr = ((Exp)ctx.returnExpr.accept(this));

		int numChildren = ctx.getChildCount();

		int idx;

		// Parses parameters
		for(idx = 4 ; idx < numChildren ; ++idx) {
			Object currentChild = ctx.getChild(idx);

			if(ctx.getChild(idx).getText().equals(")")) {
				idx += 2; // Jumps ) and { symbols
				break;
			}

			if(currentChild instanceof Lcgm_esvmParser.INT_TYPE_RULEContext) {
				IntegerType paramType = new IntegerType();
				Identifier paramId = new Identifier(ctx.getChild(++idx).getText());
				Formal paramDeclaration = new Formal(paramType, paramId);
				parameters.addElement(paramDeclaration);
			} else if(currentChild instanceof Lcgm_esvmParser.BOOLEAN_TYPE_RULEContext) {
				BooleanType paramType = new BooleanType();
				Identifier paramId = new Identifier(ctx.getChild(++idx).getText());
				Formal paramDeclaration = new Formal(paramType, paramId);
				parameters.addElement(paramDeclaration);
			} else if(currentChild instanceof Lcgm_esvmParser.INT_ARRAY_TYPE_RULEContext) {
				IntArrayType paramType = new IntArrayType();
				Identifier paramId = new Identifier(ctx.getChild(++idx).getText());
				Formal paramDeclaration = new Formal(paramType, paramId);
				parameters.addElement(paramDeclaration);

			} else if(currentChild instanceof Lcgm_esvmParser.CLASS_TYPE_RULEContext) {
				IdentifierType paramType = 
					new IdentifierType(((Lcgm_esvmParser.CLASS_TYPE_RULEContext)ctx.getChild(idx))
															.classId.getText());
				Identifier paramId = new Identifier(ctx.getChild(++idx).getText());
				Formal paramDeclaration = new Formal(paramType, paramId);
				parameters.addElement(paramDeclaration);
			}
		}

		// Parses method body
		for(; idx < numChildren ; ++idx) {
			Object currentChild = ctx.getChild(idx);
			String childText = ((ParseTree)currentChild).getText();

			if(currentChild instanceof Lcgm_esvmParser.Var_declContext) {
				Lcgm_esvmParser.Var_declContext temp = ((Lcgm_esvmParser.Var_declContext)currentChild);
				VarDecl declaration = ((VarDecl)temp.accept(this));
				vars.addElement(declaration);
			} else if(childText.equals("return")) {
				break;
			} else {
				// Statements
				ParseTree temp = ((ParseTree)currentChild);
				Statement statement = ((Statement)temp.accept(this));
				statements.addElement(statement);
			}
		}

		MethodDecl method = new MethodDecl(returnType, methodId, parameters,
											vars, statements, returnExpr);

		return method;
	}

	public Object visitClass_decl(Lcgm_esvmParser.Class_declContext ctx) {
		boolean extended = (ctx.extendsId != null);
		VarDeclList vars = new VarDeclList();
		MethodDeclList methods = new MethodDeclList();
		Identifier classId = new Identifier(ctx.classId.getText());

		int numChildren = ctx.getChildCount();

		for(int i = 0 ; i < numChildren ; ++i) {
			Object currentChild = ctx.getChild(i);
			if(currentChild instanceof Lcgm_esvmParser.Method_declContext){
				Lcgm_esvmParser.Method_declContext temp = 
					((Lcgm_esvmParser.Method_declContext) currentChild);
				MethodDecl method = ((MethodDecl)temp.accept(this));
				methods.addElement(method);
			}
			else if(currentChild instanceof Lcgm_esvmParser.Var_declContext) {
				Lcgm_esvmParser.Var_declContext temp =
					((Lcgm_esvmParser.Var_declContext) currentChild);
				VarDecl declaration = ((VarDecl)temp.accept(this));
				vars.addElement(declaration); 
			}
			
		}

		if(extended){
			Identifier extendedId = new Identifier(ctx.extendsId.getText());
			return new ClassDeclExtends(classId, extendedId, vars, methods);
		}
		return new ClassDeclSimple(classId, vars, methods);
	}

	public Object visitINT_TYPE_RULE(Lcgm_esvmParser.INT_TYPE_RULEContext ctx) {
		return new IntegerType();
	}

	public Object visitBOOLEAN_TYPE_RULE(Lcgm_esvmParser.BOOLEAN_TYPE_RULEContext ctx) {
		return new BooleanType();
	}

	public Object visitINT_ARRAY_TYPE_RULE(Lcgm_esvmParser.INT_ARRAY_TYPE_RULEContext ctx) {
		return new IntArrayType();
	}

	public Object visitCLASS_TYPE_RULE(Lcgm_esvmParser.CLASS_TYPE_RULEContext ctx) {
		String classIdTokenStr = ctx.classId.getText();
		IdentifierType classType = new IdentifierType(classIdTokenStr);

		return classType;
	}

	public Object visitVar_decl(Lcgm_esvmParser.Var_declContext ctx) {
		// Declaration type object
		Object type = ctx.varType.accept(this);

		// Declaration identifier 
		String varIdTokenStr = ctx.varId.getText();
		Identifier varId = new Identifier(varIdTokenStr);

		VarDecl declaration = new VarDecl( ((Type)type), varId );
		return declaration;
	}

	public Object visitBOOLEAN_FALSE_RULE(Lcgm_esvmParser.BOOLEAN_FALSE_RULEContext ctx) {
		return new False();
	}

	public Object visitNOT_OP_RULE(Lcgm_esvmParser.NOT_OP_RULEContext ctx) {
		// Negated expression
		Object notExpr = ctx.notExpr.accept(this);

		Not negation = new Not( ((Exp)notExpr) );
		return negation;
	}

	public Object visitNEW_ARRAY_RULE(Lcgm_esvmParser.NEW_ARRAY_RULEContext ctx) {
		// Array size expression
		Object sizeExpr = ctx.sizeExpr.accept(this);

		NewArray arrayCreation = new NewArray( ((Exp)sizeExpr) );
		return arrayCreation;
	}

	public Object visitARRAY_ACCESS_RULE(Lcgm_esvmParser.ARRAY_ACCESS_RULEContext ctx) {
		// Array Expression (left side) 
		Object arrayExpr = ctx.arrayExpr.accept(this);

		// Access position expression ([x] = position x)
		Object positionExpr = ctx.posExpr.accept(this);

		ArrayLookup access = new ArrayLookup( ((Exp)arrayExpr), ((Exp)positionExpr) );

		return access;
	}

	public Object visitMETHOD_CALL_RULE(Lcgm_esvmParser.METHOD_CALL_RULEContext ctx) {
		Object objectExpr = ctx.objectExpr.accept(this); 

		Identifier methodId = new Identifier(ctx.methodId.getText());

		int numChildren = ctx.getChildCount();
		ExpList expressions = new ExpList();

		// from [4, length-1] for skipping this, ., methodName, ( and ) symbols
		for(int i = 4 ; i < (numChildren-1) ; ++i) {
			Object currentChild = ctx.getChild(i);

			//Skips ',' symbols
			if(!(currentChild instanceof TerminalNode)) {
				Object visited = ((Lcgm_esvmParser.ExprContext)currentChild).accept(this);
				expressions.addElement( ((Exp) visited) );
			}
		}

		Call call = new Call(((Exp)objectExpr), methodId, expressions);
		return call;
	}

	public Object visitADD_OP_RULE(Lcgm_esvmParser.ADD_OP_RULEContext ctx) {
		Object expr1 = ctx.expr1.accept(this);
		Object expr2 = ctx.expr2.accept(this);

		Plus addition = new Plus( ((Exp)expr1), ((Exp)expr2) );
		return addition;		
	}

	public Object visitMULT_OP_RULE(Lcgm_esvmParser.MULT_OP_RULEContext ctx) {
		Object expr1 = ctx.expr1.accept(this);
		Object expr2 = ctx.expr2.accept(this);

		Times multiplication = new Times( ((Exp)expr1), ((Exp)expr2) ); 
		return multiplication;
	}

	public Object visitBOOLEAN_TRUE_RULE(Lcgm_esvmParser.BOOLEAN_TRUE_RULEContext ctx) {
		return new True();
	}

	public Object visitINTEGER_RULE(Lcgm_esvmParser.INTEGER_RULEContext ctx) {
		// Conversion String -> int
		int parsedInt = Integer.parseInt(ctx.getText());

		IntegerLiteral integer = new IntegerLiteral(parsedInt);
		return integer;
	}

	public Object visitTHIS_RULE(Lcgm_esvmParser.THIS_RULEContext ctx) {
		return new This();
	}

	public Object visitID_RULE(Lcgm_esvmParser.ID_RULEContext ctx) {
		String id = ctx.getText();
		IdentifierExp idExp = new IdentifierExp(id); 
		return idExp;
	}

	public Object visitAND_OP_RULE(Lcgm_esvmParser.AND_OP_RULEContext ctx) {
		Object expr1 = ctx.expr1.accept(this);
		Object expr2 = ctx.expr2.accept(this);

		And andOp = new And( ((Exp)expr1), ((Exp)expr2) ); 
		return andOp;
	}

	public Object visitUNARY_MINUS_OP_RULE(Lcgm_esvmParser.UNARY_MINUS_OP_RULEContext ctx) {
		return ctx.minusExpr.accept(this);
	}

	public Object visitPARENTHESIS_EXPR_RULE(Lcgm_esvmParser.PARENTHESIS_EXPR_RULEContext ctx) {
		return ctx.innerExpr.accept(this);
	}

	public Object visitLESS_OP_RULE(Lcgm_esvmParser.LESS_OP_RULEContext ctx) {	
		Object expr1 = ctx.expr1.accept(this);
		Object expr2 = ctx.expr2.accept(this);

		LessThan lessThan = new LessThan( ((Exp)expr1), ((Exp)expr2) ); 
		return lessThan;
	}

	public Object visitNEW_OBJECT_RULE(Lcgm_esvmParser.NEW_OBJECT_RULEContext ctx) {
		String newObjectTokenStr = ctx.objectId.getText();
		Identifier objectIdentifier = new Identifier(newObjectTokenStr);

		NewObject newObject = new NewObject(objectIdentifier);
		return newObject;
	}

	public Object visitMINUS_OP_RULE(Lcgm_esvmParser.MINUS_OP_RULEContext ctx) {
		Object expr1 = ctx.expr1.accept(this);
		Object expr2 = ctx.expr2.accept(this);

		Minus minus = new Minus( ((Exp)expr1), ((Exp)expr2) ); 
		return minus;
	}

	public Object visitARRAY_LENGTH_RULE(Lcgm_esvmParser.ARRAY_LENGTH_RULEContext ctx) {
		Object arrayExpr = ctx.arrayExpr.accept(this);

		ArrayLength arrayLength = new ArrayLength( ((Exp)arrayExpr) );
		return arrayLength;
	}

	/*public Object visitBINARY_THIS_RULE(Lcgm_esvmParser.BINARY_THIS_RULEContext ctx) {
		return new Object();
	}*/

	public Object visitBLOCK_RULE(Lcgm_esvmParser.BLOCK_RULEContext ctx) {
		int numChildren = ctx.getChildCount();
		StatementList statements = new StatementList();

		// [1, length-1] to avoid processing curly brackets
		for(int i = 1 ; i < (numChildren-1) ; ++i) {
			Object currentChild = ctx.getChild(i).accept(this);
			statements.addElement( ((Statement)currentChild) );
		}

		Block block = new Block(statements);
		return block;
	}

	public Object visitWHILE_RULE(Lcgm_esvmParser.WHILE_RULEContext ctx) {
		Object expr = ctx.loopExpr.accept(this);
		Object stmt = ctx.stmt.accept(this);

		While whileLoop = new While( ((Exp)expr), ((Statement)stmt) );
		return whileLoop;
	}

	public Object visitIF_RULE(Lcgm_esvmParser.IF_RULEContext ctx) {
		Object condition = ctx.cond.accept(this);
		Object ifStmt = ctx.ifStmt.accept(this);
		Object elseStmt = ctx.elseStmt.accept(this);
		Statement a = ((Statement)ifStmt);
		If ifConditional = new If( ((Exp)condition), ((Statement)ifStmt), 
									((Statement)elseStmt) );

		return ifConditional;
	}

	public Object visitSIMPLE_ATTRIB_RULE(Lcgm_esvmParser.SIMPLE_ATTRIB_RULEContext ctx) {
		String varIdTokenStr = ctx.varId.getText();
		Identifier varId = new Identifier(varIdTokenStr);

		Object valueExpr = ctx.varExpr.accept(this);

		Assign attribution = new Assign(varId, ((Exp)valueExpr));
		return attribution;
	}

	public Object visitARRAY_ATTRIB_RULE(Lcgm_esvmParser.ARRAY_ATTRIB_RULEContext ctx) {
		String varIdTokenStr = ctx.varId.getText();
		Identifier varId = new Identifier(varIdTokenStr);

		Object positionExpr = ctx.posExpr.accept(this);
		Object valueExpr = ctx.valueExpr.accept(this);

		ArrayAssign arrayAttribution = new ArrayAssign(varId, ((Exp)positionExpr), ((Exp)valueExpr));
		return arrayAttribution;
	}

	public Object visitPRINT_RULE(Lcgm_esvmParser.PRINT_RULEContext ctx) {
		Object expr = ctx.exp.accept(this);

		Statement printStatement = new Print( ((Exp)expr) );
		return printStatement;
	}

	// Inherited from ParseTreeVisitor (indirect) superclass  
	public Object visitErrorNode(ErrorNode err) {
		return new Object();
	}

	public Object visitTerminal(TerminalNode term) {
		return term;
	}

	public Object visitChildren(RuleNode rl) {
		return new Object();
	}

	public Object visit(ParseTree pt) {
		pt.accept(this);
		return new Object();
	}

}