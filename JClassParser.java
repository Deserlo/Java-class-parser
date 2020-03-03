import java.io.IOException;
import java.util.Scanner;

/*
 NAMES: NAJERA E., NIKIFOROVA M.
 FILE: JClassParser.java
 SEM: FALL 2018
 COURSE: CSC 135
 
 
 A SIMPLE JAVA CLASS PARSER
 
 TO COMPILE ON ATHENA:
  ENTER 'javac JClassParser.java' INTO CMD LINE(without the single quotes)
 TO RUN:
   FIRST COMPILE, THEN RUN BY ENTERING 'java JClassParser' (without the single quotes) INTO CMD LINE 
 
THIS PARSER ASSUMES:
	-The user enters an input stream of legal tokens, followed by a $. 
	-The user enters no whitespace. 
	-The user only enters legal tokens listed below. 
	-The user enters one and only one $, at the end. 
 	
EBNF grammar for a very simple Java Class definition in the Java programming language:
Note: varlist, assignstatemt (and varname in returnstatemt) are followed by a ";".
<javaclass> ::= <classname> [X <classname>] B <varlist>; {<method>} E 
<classname> ::= C|D 
<varlist> ::= <vardef> {, <vardef>} 
<vardef> ::= <type> <varname> | <classname> <varref> 
<type> ::= I|S 
<varname> ::= <letter> {<char>} 
<letter> ::= Y|Z 
<char> ::= <letter> | <digit> 
<digit> ::= 0|1|2|3 
<integer> ::= <digit> {<digit>} 
<varref> ::= J|K 
<method> ::= <accessor> <type> <methodname> ([<varlist>]) B {<statemt>} <returnstatemt> E 
<accessor> ::= P|V 
<methodname> ::= M|N 
<statemt> ::= <ifstatemt> | <whilestatemt>|<assignstatemt>;
<ifstatemt> ::= F <cond> T B {<statemt>} E [L B {<statemt>} E] 
<assignstatemt> ::= <varname> = <mathexpr> | <varref> = <methodcall>  
<mathexpr> ::= <factor> {+ factor} 
<factor> ::= <oprnd> {* oprnd} 
<oprnd> ::= <integer> | <varname> | (<mathexpr>) | <methodcall> 
<getvarref> ::= O <classname>() | <methodcall> 
<whilestatemt> ::= W <cond> T B {<statemt>} E 
<cond> ::= (<oprnd> <operator> <oprnd>) 
<operator> ::= < | = | > | ! 
<returnstatemt> ::= R <varname>; 
<methodcall> ::= <varref>.<methodname>( [ <varlist> ] )

KEY: The single letters are codes after lexical analysis for the following words: 
The tokens are: 
X for extends 
B for Begin of block 
E for End of block 
I for Integer 
S for String 
P for public 
V for private 
F for if 
T for then 
L for else
O for new (to create a new class Object reference) 
W for while 
R for return

The tokens also include: C D I S P V 0 1 2 3 J K M N ( ) + * < = > ! Nonterminals are shown as lowercase words. 
The following characters are NOT tokens (they are EBNF metasymbols): | { } [ ] Note that parentheses are TOKENS, not EBNF metasymbols in this particular grammar.


TEST CASES:
	Legal Expressions:
*ONE METHOD: CBIY3;PIN(IY3)BRZ;EE$
*EXTENDING A CLASS: DXCBIY0;PIN(IY)BRZ;EE$
*MULTIPLE VARDEFS IN VARLIST: CBIYYZZ00YYZZ1122,SZ000000Y,IZ,SZY3;E$
*MULTIPLE METHODS: CBIY3;PIN(IY3)BRZ;EPIN(IY3)BRZ;EE$
*METHOD WITH VARLIST: CBIY3;PIN(IY3)BRYYYZ230;EE$
*IF STMT: CBIY3;PIN(IY3)BF(0<1)TBERYYYZ230;EE$
*IF-ELSE STMT: DBIZ2,SZY;VIM(IZ)BF(Z!Z)TBZ=1;ELBY=2;ERZ;EE$
*IF-WHILESTMT-ELSE: DBIZ2,SZY;VIM(IZ)BF(Z!Z)TBW(Z=Z)TBZ1Y=Y*1+Z;EELBY=2;ERZ;EE$
*WHILE STMT: CBIY3;PIN(IY3)BW(0<1)TBZ=2;ERYYYZ230;EE$
*WHILE STMT WITH AN ASSIGN STMT TO METHODCALL: CBIY3;PIN(IY3)BW(0<1)TBJ=K.M();ERYYYZ230;EE$
*WHILE STMT WITH ONE FACTOR AND TWO OPERANDS: CBSZZ11;PSM(SYZ123)BW(Z=Z)TBZ1Y=Y*1+Z;ERY;EE$
*WHILE STMT WITH ASSIGN STMT TO METHODCALL IN LOOP WITH A VARLIST: CBIY3;PIN(IY3)BW(0<1)TBJ=K.M(IY3,SZY2,SY);ERYYYZ230;EE$
*MULTIPLE FACTORS AND MULTIPLE OPERANDS: CBSZZ11;PSM(SYZ123)BW(Z=Z)TBZ1Y=Y*1*3*Z+ZZY*2;ERY;EE$
*MULTIPLE FACTORS WITH ONE MATHEXPR: CBSZZ11;PSM(SYZ123)BW(Z=Z)TBZ1Y=(Y*1)*3*Z+ZZY*2;ERY;EE$
*MULTIPLE MATHEXPRS: CBSZZ11;PSM(SYZ123)BW(Z=Z)TBZ1Y=(Y*1)*((3)*Z)+(Z)+ZY*2;ERY;EE$

	Illegal Expressions:
*EXTENDS MORE THAN ONE CLASS: DXCXDBIY0;PIN(IY)BRZ;EE$
*IF-ELSE WITH MORE THAN ONE ELSE CLAUSES: DBIZ2,SZY;VIM(IZ)BF(Z!Z)TBZ=1;ELBY=2;ELBY=2;ERZ;EE$
*OPERAND (Z) NOT FOLLOWED BY '*': CBSZZ11;PSM(SYZ123)BW(Z=Z)TBZ1Y=Y*1*3*Z+(Z)ZY*2;ERY;EE$

*/


public class JClassParser {
	static String inputString; 
	static int index = 0; 
	static int errorflag = 0;

	private char token() 
	{ return(inputString.charAt(index)); }

	private void advancePtr() 
	{ if (index < (inputString.length()-1)) index++; }

	private void match(char T) 
	{ if (T == token()) advancePtr(); else error(); }
	
	private void error() {
		System.out.println("error at position: " + index); errorflag = 1; advancePtr(); 
	} //----------------------
	
	private void jClass() {
		className();
		if (token()=='X'){
			match('X');
			className();
		}
		match('B'); 
		varlist(); 
		match(';');
		while(token() == 'P' || token()=='V') {
			method(); 
		} 
		match('E');
	}
	
	private void className(){
		switch(token()) {
			case 'C': match('C'); break;
			case 'D': match('D'); break;
			default: error();
		}
	}
	
	private void varlist(){
		vardef();
		while(token()==','){
			match(',');
			vardef();
		}

	}
	
	private void vardef(){
		if (token()=='I' || token()=='S') {
			type();
			varname();
		}
		else if (token()=='C' || token()=='D'){
			className();
			varref();
		}
		
	}
	
	private void type(){
		switch(token()) {
			case 'I': match('I'); break;
			case 'S': match('S'); break;
			default: error();
		}
	}
	
	private void varname(){
		letter();
		while(token()=='Y' || token()=='Z' || token()=='0' || token()=='1' || token()=='2' || token()=='3') {
			switch(token()) {
				case 'Y':letter(); break;
				case 'Z':letter(); break;
				case '0':charcter(); break;
				case '1':charcter(); break;
				case '2':charcter(); break;
				case '3':charcter(); break;
			}
		}
	}
	
	private void charcter() {
		switch(token()) {
			case 'Y':letter(); break;
			case 'Z':letter(); break;
			case '0':digit(); break;
			case '1':digit(); break;
			case '2':digit(); break;
			case '3':digit(); break;
		}
	}
	
	private void letter(){
		switch(token()) {
			case 'Y': match('Y'); break;
			case 'Z': match('Z'); break;
			default: error();
		}
			
	}
	
	private void digit(){
		switch(token()) {
			case '0':match('0'); break;
			case '1':match('1'); break;
			case '2':match('2'); break;
			case '3':match('3'); break;
			default: error();
		}			
	}
	
	private void integer(){
		digit();
		while (token()=='0' || token()=='1' || token()=='2' || token()=='3'){
			digit();
		}
	}
	
	private void varref(){
		switch(token()) {
			case 'J': match('J'); break;
			case 'K': match('K'); break;
			default: error();
		}				
	}
	
	private void method(){
		accessor();
		type();
		methodName();
		match('(');
		if (token()=='I' || token()=='S' || token()=='Y' || token()=='Z'){
			varlist();
		}
		match(')');
		match('B');
		while (token()=='F' || token()=='W' || token()=='Y' || token()=='Z' || token()=='J' || token()=='K'){
			statemt();
		}				
		returnStatemt();
		match('E');	
	}
	
	private void accessor(){
		switch(token()) {
			case 'P': match('P'); break;
			case 'V': match('V'); break;
			default: error();
		}
	}
	
	private void methodName(){
		switch(token()) {
			case 'M': match('M'); break;
			case 'N': match('N'); break;
			default: error();
		}
	}
	
	private void statemt(){
		if (token()=='F'){
			ifStatemt();
		}
		else if (token()=='W'){
			whileStatemt();
		}
		else if(token()=='Y' || token()=='Z' || token()=='J' || token()=='K'){
			assignStatemt();
			match(';');
		}
	}
	
	private void ifStatemt(){
		match('F');
		cond();
		match('T');
		match('B');
		while (token()=='F' || token()=='W' || token()=='Y' || token()=='Z' || token()=='J' || token()=='K'){
			statemt();
		}
		match('E');
		if (token()=='L'){
			match('L');
			match('B');
			while (token()=='F' || token()=='W' || token()=='Y' || token()=='Z' || token()=='J' || token()=='K'){
				statemt();
			}
			match('E');
		} 
	}
	
	private void whileStatemt(){
		match('W');
		cond();
		match('T');
		match('B');
		while (token()=='F' || token()=='W' || token()=='Y' || token()=='Z' || token()=='J' || token()=='K'){
			statemt();
		}
		match('E');
	}
	
	private void returnStatemt(){
		match('R');
		varname();
		match(';');
	}
	
	private void cond(){
		match('(');
		oprnd();
		operator();
		oprnd();
		match(')');
	}
	
	private void oprnd(){
		if (token()=='Y' || token()=='Z')
			varname();
		else if (token()=='0' || token()=='1' || token()=='2' || token()=='3')
			integer();
		else if (token()=='(') {
			match('(');
			mathexpr();
			match(')');
		}
		else if(token()=='J' || token()=='K')
			methodCall();	
	}
	
	private void operator(){
		switch(token()){
			case '<': match('<'); break;
			case '=': match('='); break;
			case '>': match('>'); break;
			case '!': match('!'); break;
			default: error();
		}
	}
	
	private void mathexpr(){
		factor();
		while (token()=='+') {
			match('+');
			factor();
		}
	}
	
	private void factor(){
		oprnd();
		while (token()=='*') {
			match('*');
			oprnd();
		}
	}
	
	private void assignStatemt(){
		if (token()=='Y' || token()=='Z'){
			varname();
			match('=');
			mathexpr();
		}
		else if (token()=='J' || token()=='K'){
			varref();
			match('=');
			methodCall();
		}
	}
	
	private void methodCall(){
		varref();
		match('.');
		methodName();
		match('(');
		if (token()=='I' || token()=='S' || token()=='Y' || token()=='Z'){
			varlist();
		}
		match(')');		
	}
	
	private void getvarref(){
		switch(token()){
			case 'O': 
				match('O'); 
				className();
				match('(');
				match(')');
				break;
			case 'J':
				methodCall(); break;
			case 'K':
				methodCall(); break;
		}
	}
	
	private void start() {
		jClass(); 
		match('$');
		if (errorflag == 0)
			System.out.println("legal." + "\n"); 
		else
			System.out.println("errors found." + "\n"); 
	} //----------------------
	
	
	public static void main (String[] args) throws IOException {
		
		JClassParser rec = new JClassParser();
		
		Scanner input = new Scanner(System.in);
		
		System.out.print("\n" + "enter an expression: ");
		inputString = input.nextLine();
		
		rec.start(); 
	} 
}
