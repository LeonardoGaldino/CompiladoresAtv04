grammar Lcgm_esvm;

@header {
	package br.ufpe.cin.if688.minijava.grammar;

	import br.ufpe.cin.if688.minijava.ast.*;

	import org.antlr.v4.runtime.tree.*;
	import org.antlr.v4.runtime.RuleContext.*;
	import java.util.*;
}

@members {
	
	private Program ast;
	Lcgm_esvm_ASTVisitor visitor = new Lcgm_esvm_ASTVisitor();

	public Program getAST() {
		return this.ast;
	}

}

// ----------------------------------------- LEXER SPEC ------------------------------------------

fragment ZERO_DIGIT : '0' ;
fragment POSITIVE_INTEGER : [1-9][0-9]* ;

INTEGER_LITERAL : 
	ZERO_DIGIT 
	| POSITIVE_INTEGER;

BOOLEAN_TRUE : 'true' ;
BOOLEAN_FALSE : 'false' ;

NOT_OP :
	'!'
	;

MINUS_OP :
	'-'
	;

ADD_OP : 
	'+'
	;

MULT_OP :
	'*'
	;

AND_OP :
	'&&'
	;

LESS_OP :
	'<'
	;

FIELD_OP :
	'.'
	;

THIS :
	'this'
	;

ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \r\n\t] -> skip ;

// ----------------------------------------- PARSER SPEC ------------------------------------------

goal :
	mainClass=main_class_decl (class_decl)* EOF 
	{	
		this.ast = ((Program)$ctx.accept(this.visitor));
	}
	;

main_class_decl : 
	'class' classId=ID '{' 
		'public' 'static' 'void' 'main' '(' 'String' '[' ']' argsId=ID ')' '{' 
			bodyStatements=statement 
		'}' 
	'}'
	;

method_decl : 'public' returnType=type methodId=ID '(' (type ID (',' type ID)* )? ')' '{'
		(var_decl)*
		(statement)*
		'return' returnExpr=expr ';'
	'}'
	;

class_decl : 'class' classId=ID ('extends' extendsId=ID)? '{'
		(var_decl)*
		(method_decl)*
	'}'
	;

// Built-ins Data types
type :
	'int'               #INT_TYPE_RULE
	| 'boolean'         #BOOLEAN_TYPE_RULE
	| 'int[]'           #INT_ARRAY_TYPE_RULE
	| classId=ID        #CLASS_TYPE_RULE
	;

// Variable declaration
var_decl : varType=type varId=ID ';' ;

// Expressions
expr :
	// Literals
	INTEGER_LITERAL     #INTEGER_RULE

	// Method call
	| objectExpr=expr '.' methodId=ID '(' ( expr (',' expr)* )? ')'     #METHOD_CALL_RULE
	
	| BOOLEAN_TRUE      #BOOLEAN_TRUE_RULE
	| BOOLEAN_FALSE     #BOOLEAN_FALSE_RULE
	| THIS              #THIS_RULE

	// Identifier as expression
	| ID #ID_RULE

	// Binary expressions
	| expr1=expr ADD_OP expr2=expr                                      #ADD_OP_RULE
	| expr1=expr MINUS_OP expr2=expr                                    #MINUS_OP_RULE
	| expr1=expr MULT_OP expr2=expr                                     #MULT_OP_RULE
	| expr1=expr AND_OP expr2=expr                                      #AND_OP_RULE
	| expr1=expr LESS_OP expr2=expr                                     #LESS_OP_RULE
	//| THIS '.' expr1=expr                           #BINARY_THIS_RULE

	// Unary expression
	| MINUS_OP minusExpr=expr                                           #UNARY_MINUS_OP_RULE
	| NOT_OP notExpr=expr                                               #NOT_OP_RULE

	// Constructors (array and object)
	| 'new' 'int' '[' sizeExpr=expr ']'                                 #NEW_ARRAY_RULE
	| 'new' objectId=ID '(' ')'                                         #NEW_OBJECT_RULE

	// Others
	| arrayExpr=expr '[' posExpr=expr ']'                               #ARRAY_ACCESS_RULE
	| arrayExpr=expr '.' 'length'                                       #ARRAY_LENGTH_RULE
	| '(' innerExpr=expr ')'                                            #PARENTHESIS_EXPR_RULE
	;

statement :
	// Defines a block with several statements
	'{' (statement)* '}'                                                  #BLOCK_RULE

	// Control statements
	| 'while' '(' loopExpr=expr ')' stmt=statement                        #WHILE_RULE
	| 'if' '(' cond=expr ')' ifStmt=statement 'else' elseStmt=statement   #IF_RULE

	// Attribution statements
	| varId=ID '=' varExpr=expr ';'                                       #SIMPLE_ATTRIB_RULE
	| varId=ID '[' posExpr=expr ']' '=' valueExpr=expr ';'                #ARRAY_ATTRIB_RULE

	// Output statement
	| 'System.out.println' '(' exp=expr ')' ';'                           #PRINT_RULE
	;
