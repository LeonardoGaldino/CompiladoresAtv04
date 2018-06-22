package br.ufpe.cin.if688.minijava.visitor;

import java.util.Enumeration;

import br.ufpe.cin.if688.minijava.ast.And;
import br.ufpe.cin.if688.minijava.ast.ArrayAssign;
import br.ufpe.cin.if688.minijava.ast.ArrayLength;
import br.ufpe.cin.if688.minijava.ast.ArrayLookup;
import br.ufpe.cin.if688.minijava.ast.Assign;
import br.ufpe.cin.if688.minijava.ast.Block;
import br.ufpe.cin.if688.minijava.ast.BooleanType;
import br.ufpe.cin.if688.minijava.ast.Call;
import br.ufpe.cin.if688.minijava.ast.ClassDeclExtends;
import br.ufpe.cin.if688.minijava.ast.ClassDeclSimple;
import br.ufpe.cin.if688.minijava.ast.False;
import br.ufpe.cin.if688.minijava.ast.Formal;
import br.ufpe.cin.if688.minijava.ast.Identifier;
import br.ufpe.cin.if688.minijava.ast.IdentifierExp;
import br.ufpe.cin.if688.minijava.ast.IdentifierType;
import br.ufpe.cin.if688.minijava.ast.If;
import br.ufpe.cin.if688.minijava.ast.IntArrayType;
import br.ufpe.cin.if688.minijava.ast.IntegerLiteral;
import br.ufpe.cin.if688.minijava.ast.IntegerType;
import br.ufpe.cin.if688.minijava.ast.LessThan;
import br.ufpe.cin.if688.minijava.ast.MainClass;
import br.ufpe.cin.if688.minijava.ast.MethodDecl;
import br.ufpe.cin.if688.minijava.ast.Minus;
import br.ufpe.cin.if688.minijava.ast.NewArray;
import br.ufpe.cin.if688.minijava.ast.NewObject;
import br.ufpe.cin.if688.minijava.ast.Not;
import br.ufpe.cin.if688.minijava.ast.Plus;
import br.ufpe.cin.if688.minijava.ast.Print;
import br.ufpe.cin.if688.minijava.ast.Program;
import br.ufpe.cin.if688.minijava.ast.This;
import br.ufpe.cin.if688.minijava.ast.Times;
import br.ufpe.cin.if688.minijava.ast.True;
import br.ufpe.cin.if688.minijava.ast.Type;
import br.ufpe.cin.if688.minijava.ast.VarDecl;
import br.ufpe.cin.if688.minijava.ast.While;

import br.ufpe.cin.if688.minijava.symboltable.Class;
import br.ufpe.cin.if688.minijava.symboltable.Method;
import br.ufpe.cin.if688.minijava.symboltable.Variable;
import br.ufpe.cin.if688.minijava.symboltable.SymbolTable;

public class TypeCheckVisitor implements IVisitor<Type> {

	private SymbolTable symbolTable;
	private Class currClass;
	private Method currMethod;

	public TypeCheckVisitor(SymbolTable st) {
		symbolTable = st;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Type visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.elementAt(i).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Type visit(MainClass n) {
		this.symbolTable.addClass("main", null);
		this.currClass = this.symbolTable.getClass(n.i1.s);
		this.currClass.addMethod("main", null);
		this.currMethod = this.currClass.getMethod("main");
		this.currMethod.addParam(n.i2.s, null);
		n.i1.accept(this);
		n.i2.accept(this);
		n.s.accept(this);
		this.currClass = null;
		this.currMethod = null;
		return null;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclSimple n) {
		this.currClass = symbolTable.getClass(n.i.s);
		n.i.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		this.currClass = null;
		return null;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclExtends n) {
		this.currClass = symbolTable.getClass(n.i.s);
		n.i.accept(this);
		n.j.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		this.currClass = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(VarDecl n) {
		n.t.accept(this);
		n.i.accept(this);
		return n.t;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Type visit(MethodDecl n) {
		this.currMethod = this.symbolTable.getMethod(n.i.s, this.currClass.getId());
		n.t.accept(this);
		n.i.accept(this);
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		n.e.accept(this);
		this.currMethod = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(Formal n) {
		n.t.accept(this);
		n.i.accept(this);
		return n.t;
	}

	public Type visit(IntArrayType n) {
		return n;
	}

	public Type visit(BooleanType n) {
		return n;
	}

	public Type visit(IntegerType n) {
		return n;
	}

	// String s;
	public Type visit(IdentifierType n) {
		if(this.symbolTable.containsClass(n.s))
			return n;
		return this.symbolTable.getVarType(this.currMethod, this.currClass, n.s);
	}

	// StatementList sl;
	public Type visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {
		Type cond = n.e.accept(this);

		if(!(cond instanceof BooleanType)) {
			String errorMessage = "Conditional without boolean expression on " +
									"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
														this.currClass.getId());
		}
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		Type loopCondType = n.e.accept(this);
		if(!(loopCondType instanceof BooleanType)) {
			String errorMessage = "Loop Condition without boolean expression on " +
									"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
														this.currClass.getId());
		}
		n.s.accept(this);
		return null;
	}

	// Exp e;
	public Type visit(Print n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e;
	public Type visit(Assign n) {
		Type t1 = n.i.accept(this);
		Type t2 = n.e.accept(this);

		if(!this.symbolTable.compareTypes(t1, t2)) {
			String errorMessage = "Right expression has different type than " +
								"variable '%s' on method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, n.i.s,
										this.currMethod.getId(),
										this.currClass.getId());
		}

		return t1;
	}

	// Identifier i;
	// Exp e1,e2;
	public Type visit(ArrayAssign n) {
		n.i.accept(this);
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		if(!(t1 instanceof IntegerType)) {
			String errorMessage = "Array access index is not an integer on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
														this.currClass.getId());
		}

		if(!(t2 instanceof IntegerType)) {
			String errorMessage = "Array attribution expression is not an integer " +
								"on method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
														this.currClass.getId());
		}

		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(And n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);

		boolean typeCheckOk = (this.symbolTable.compareTypes(t1, t2)
								&& t1 instanceof BooleanType);

		if(!typeCheckOk) {
			String errorMessage = "Bitwise AND without two booleans on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
					this.currClass.getId());
		}

		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);

		boolean typeCheckOk = (this.symbolTable.compareTypes(t1, t2)
								&& t1 instanceof IntegerType);

		if(!typeCheckOk) {
			String errorMessage = "Comparison <LT> without two integers on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
					this.currClass.getId());
		}

		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);

		boolean typeCheckOk = (this.symbolTable.compareTypes(t1, t2)
								&& t1 instanceof IntegerType);

		if(!typeCheckOk) {
			String errorMessage = "Addition without two integers on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
								this.currClass.getId());
		}
		
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);

		boolean typeCheckOk = (this.symbolTable.compareTypes(t1, t2)
								&& t1 instanceof IntegerType);

		if(!typeCheckOk) {
			String errorMessage = "Subtraction without two integers on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
								this.currClass.getId());
		}
		
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);

		boolean typeCheckOk = (this.symbolTable.compareTypes(t1, t2)
									&& t1 instanceof IntegerType);

		if(!typeCheckOk) {
			String errorMessage = "Multiplication without two integers on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
								this.currClass.getId());
		}
		
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		Type arrayVarType = n.e1.accept(this);
		Type accessIndexType = n.e2.accept(this);

		if(!(arrayVarType instanceof IntArrayType)) {
			String errorMessage = "Array lookup on non array expression " +
								"on method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
														this.currClass.getId());
		}

		if(!(accessIndexType instanceof IntegerType)) {
			String errorMessage = "Array lookup with integer access index expression " +
								"on method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
														this.currClass.getId());
		}

		return new IntegerType();
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		Type type = n.e.accept(this);

		if(!(type instanceof IntArrayType)) {
			String errorMessage = "Array length look up on non array type on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
								this.currClass.getId());
		}
			
		return new IntegerType();
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {
		Type expType = n.e.accept(this);
		if(!(expType instanceof IdentifierType)) {
			String errorMessage = "Method call on a wrong object on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
								this.currClass.getId());
		}
		String typeId = ((IdentifierType) expType).s;
		Class temp = this.symbolTable.getClass(typeId);
		
		while(temp != null) {
			if(temp.containsMethod(n.i.s))
				break;
			String parentClassName = temp.parent();
			if(parentClassName == null)
				temp = null;
			else
				temp = this.symbolTable.getClass(temp.parent());
		}
		if(temp == null) {
			String errorMessage = "Method '%s' doesn't exists on class '%s'. " +
								"Occurence on method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, 
										n.i.s,
										typeId,
										this.currMethod.getId(),
										this.currClass.getId());
		}

		Method targetMethod = temp.getMethod(n.i.s);
		Method tempMethod = this.currMethod;
		Class tempClass = this.currClass;

		this.currClass = temp;
		this.currMethod = targetMethod;
		n.i.accept(this);
		this.currClass = tempClass;
		this.currMethod = tempMethod;
		
		int i;
		for (i = 0; i < n.el.size(); i++) {
			Type p1 = n.el.elementAt(i).accept(this);
			Variable p2 = targetMethod.getParamAt(i);
			if(p2 == null) {
				String errorMessage = "More parameters than expected on call of " +
									"method '%s' on method '%s' of class '%s'";
				Thrower.throwExceptionWrapper(errorMessage, 
										n.i.s,
										this.currMethod.getId(),
										this.currClass.getId());	
			}

			if(!this.symbolTable.compareTypes(p2.type(), p1)) {
				String errorMessage = "Wrong type on parameter '%d' called '%s' on " +
									"method '%s' on method '%s' of class '%s'";
				Thrower.throwExceptionWrapper(errorMessage, 
										i+1,
										p2.id(),
										n.i.s,
										this.currMethod.getId(),
										this.currClass.getId());
			}
		}
		Variable checkVar = targetMethod.getParamAt(i);
		if(checkVar != null) {
			String errorMessage = "Less paremeters than expected on call of " +
			"method '%s' on method '%s' of class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, 
									n.i.s,
									this.currMethod.getId(),
									this.currClass.getId());
		}

		return this.symbolTable.getMethodType(n.i.s, temp.getId());
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType();
	}

	public Type visit(True n) {
		return new BooleanType();
	}

	public Type visit(False n) {
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		return symbolTable.getVarType(this.currMethod,
										this.currClass,
										n.s);
	}

	public Type visit(This n) {
		return new IdentifierType(this.currClass.getId());
	}

	// Exp e;
	public Type visit(NewArray n) {
		Type expType = n.e.accept(this);

		if(!(expType instanceof IntegerType)) {
			String errorMessage = "Array initializating without integer expression " +
								"on method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
														this.currClass.getId());
		}

		return new IntArrayType();
	}

	// Identifier i;
	public Type visit(NewObject n) {
		if(!(this.symbolTable.containsClass(n.i.s))) {
			String errorMessage = "Trying to instantiate object with inexistent " +
								"class '%s' on method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, n.i.s, this.currMethod.getId(),
														this.currClass.getId());
		}
		return new IdentifierType(n.i.s);
	}

	// Exp e;
	public Type visit(Not n) {
		Type expType = n.e.accept(this);
		if(!(expType instanceof BooleanType)) {
			String errorMessage = "Negation without boolean expression on " +
								"method '%s' at class '%s'";
			Thrower.throwExceptionWrapper(errorMessage, this.currMethod.getId(),
														this.currClass.getId());
		}
		return new BooleanType();
	}

	// String s;
	public Type visit(Identifier n) {
		if(this.symbolTable.containsClass(n.s))
			return new IdentifierType(n.s);
		else if(this.currClass.containsMethod(n.s))
			return this.currClass.getMethod(n.s).type();
		else if(this.currClass.containsVar(n.s))
			return this.currClass.getVar(n.s).type();
		return this.symbolTable.getVarType(this.currMethod, this.currClass, n.s);
	}
}