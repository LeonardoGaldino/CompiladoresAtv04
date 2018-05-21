grammar Grammar;

@header {
	package br.ufpe.cin.if688.minijava.grammar;


	import br.ufpe.cin.if688.minijava.ast.*;
	import org.antlr.v4.runtime.tree.*;
	import org.antlr.v4.runtime.RuleContext.*;
	import java.util.*;
}

@members {
	private Program ast;
	//ASTVisitor visitor = new ASTVisitor();

	public Program getAST() {
		return this.ast;
	}

}

// Lexer Rules

INTEGER_LITERAL : 
	'0' 
	| [1-9][0-9]*;

BOOLEAN_LITERAL : 
	'true' 
	| 'false' 
	;

UNARY_OP :
	'!'
	;

SUB_OP :
	'-'
	;

fragment ADD_OP : 
	'+'
	;

fragment MULT_OP :
	'*'
	;

fragment LOGICAL_OP :
	'&&'
	;

fragment CMP_OP :
	'<'
	;

BINARY_OP :
	ADD_OP
	| SUB_OP
	| MULT_OP
	| LOGICAL_OP
	| CMP_OP
	;

ID : [a-z_A-Z0-9]+ ;

WS : [ \r\n\t] -> skip ;

// Parser Rules

init :
	main_class_decl (class_decl)* EOF 
	{	
		//Object x = $ctx.accept(this.visitor);
		//this.ast = (Program)x;
	}
	;

main_class_decl : 
	'class' classId=ID '{' 
		'public' 'static' 'void' 'main' '(' 'String' '[' ']' argsId=ID ')' 
		'{' stmt=statement '}' 
	'}'
	;

method_decl : 'public' type ID '(' (type ID (',' type ID)* )? ')' '{'
		(var_decl)*
		(statement)*
		'return' expr ';'
	'}'
	;

class_decl : 'class' ID ('extends' ID)? '{'
		(var_decl)*
		(method_decl)*
	'}'
	;

// Built-ins Data types
type :
	'int'
	| 'boolean'
	| 'int[]'
	| ID
	;

// Variable declaration
var_decl : type ID ';' ;

// Expressions
expr :
	ID
	| UNARY_OP expr
	| INTEGER_LITERAL
	| ('-') expr
	| BOOLEAN_LITERAL
	| expr '[' expr ']'
	| <assoc=left> expr BINARY_OP expr
	| <assoc=left> expr SUB_OP expr
	| expr '.' 'length' 
	| 'this' '.' expr
	| 'this'
	| 'new' 'int' '[' expr ']'
	| 'new' ID '(' ')'
	| expr '.' ID '(' (expr (',' expr)* )? ')'
	| '(' expr ')'
	;

// Possible statements
block_stmt : '{' (statement)* '}' ;

while_stmt : 'while' '(' expr ')' statement ;

if_stmt : 'if' '(' expr ')' statement 'else' statement ;

simple_attrib_stmt : ID '=' expr ;

array_attrib_stmt : ID'['expr']' '=' expr ;

print_stmt : 
	'System.out.println' '(' exp=expr')' 
	{
		//$ctx.accept(this.visitor);
	}
	;

// General statement
statement :
	block_stmt
	| while_stmt
	| if_stmt
	| simple_attrib_stmt ';'
	| array_attrib_stmt ';'
	| print_stmt ';'
	;
