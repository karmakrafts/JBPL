/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

lexer grammar JBPLLexer;

// To workaround https://github.com/antlr/antlr4/issues/2006
@members {
    private void popModeIfPresent() {
        if(_modeStack.size() == 0) {
            return;
        }
        popMode();
    }
}

LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT: '/*' (BLOCK_COMMENT | .)*? '*/' -> channel(HIDDEN);
WS: [\u0020\u0009\u000C] -> channel(HIDDEN);
NL: ('\n' | ('\r' '\n'?));

CONST_STR_START: '"' -> pushMode(M_CONST_STR), type(QUOTE);

KW_PREPRO_INCLUDE: '^include';
KW_PREPRO_RETURN: '^return';
KW_PREPRO_ASSERT: '^assert';
KW_PREPRO_CLASS: '^class';
KW_PREPRO_ERROR: '^error';
KW_PREPRO_INFO: '^info';
CARET: '^';
EXCL: '!';

KW_INSTRUCTION: 'instruction';
KW_SIGNATURE: 'signature';
KW_PROTECTED: 'protected';
KW_TRANSIENT: 'transient';
KW_OPCODEOF: 'opcodeof';
KW_CONTINUE: 'continue';
KW_VOLATILE: 'volatile';
KW_DEFINED: 'defined';
KW_PRIVATE: 'private';
KW_DEFAULT: 'default';
KW_VERSION: 'version'; // This indicates the ASM (classfile) version we assemble against
KW_DEFINE: 'define';
KW_NAMEOF: 'nameof';
KW_PUBLIC: 'public';
KW_OPCODE: 'opcode';
KW_BEFORE: 'before';
KW_OFFSET: 'offset';
KW_INJECT: 'inject';
KW_STRING: 'string';
KW_STATIC: 'static';
KW_TYPEOF: 'typeof';
KW_SIZEOF: 'sizeof';
KW_MACRO: 'macro';
KW_CLASS: 'class';
KW_FINAL: 'final';
KW_FIELD: 'field';
KW_LOCAL: 'local';
KW_FALSE: 'false';
KW_AFTER: 'after';
KW_BREAK: 'break';
KW_WHEN: 'when';
KW_ENUM: 'enum';
KW_SYNC: 'sync';
KW_TYPE: 'type';
KW_TRUE: 'true';
KW_ELSE: 'else';
KW_YEET: 'yeet';
KW_FUN: 'fun';
KW_FOR: 'for';
KW_IN: 'in';
KW_IF: 'if';
KW_IS: 'is';
KW_AS: 'as';
KW_BY: 'by';

KW_VOID: 'void';
KW_BOOL: 'bool';
KW_CHAR: 'char';
KW_I16: 'i16';
KW_I32: 'i32';
KW_I64: 'i64';
KW_F32: 'f32';
KW_F64: 'f64';
KW_I8: 'i8';

PLUS_ASSIGN: '+=';
MINUS_ASSIGN: '-=';
TIMES_ASSIGN: '*=';
DIV_ASSIGN: '/=';
REM_ASSIGN: '%=';
LSH_ASSIGN: '<<=';
URSH_ASSIGN: '>>>=';
RSH_ASSIGN: '>>=';
AND_ASSIGN: '&=';
OR_ASSIGN: '|=';
XOR_ASSIGN: '^=';

INC: '++';
DEC: '--';

SPACESHIP: '<=>';
EQEQ: '==';
NEQ: '!=';
LEQ: '<=';
GEQ: '>=';
AMPAMP: '&&';
AMP: '&';
PIPEPIPE: '||';
PIPE: '|';
TILDE: '~';

EXCL_RANGE: '..<';
DOTDOT: '..';
DOT: '.';
DOLLAR: '$';
SLASH: '/';
LSH: '<<';
L_ABRACKET: '<';
URSH: '>>>';
RSH: '>>';
R_ABRACKET: '>';
L_PAREN: '(' -> pushMode(DEFAULT_MODE);
R_PAREN: ')' { popModeIfPresent(); };
L_SQBRACKET: '[' -> pushMode(DEFAULT_MODE);
R_SQBRACKET: ']' { popModeIfPresent(); };
L_BRACE: '{' -> pushMode(DEFAULT_MODE);
R_BRACE: '}' { popModeIfPresent(); };
SINGLE_QUOTE: '\'';
QUOTE: '"';
SEMICOLON: ';';
COLON: ':';
EQ: '=';
COMMA: ',';
ARROW: '->';
MINUS: '-';
PLUS: '+';
ASTERISK: '*';
REM: '%';
AT: '@';

fragment INT_TYPE: [il];
fragment FLOAT_TYPE: [fd];
fragment PRIMITIVE_TYPE: INT_TYPE | FLOAT_TYPE;
fragment OBJECT_TYPE: 'a';
fragment EXT_INT_TYPE: [bs];
fragment CHAR_TYPE: 'c';
fragment BOOL_TYPE: 'z';
fragment ARRAY_TYPE: PRIMITIVE_TYPE | EXT_INT_TYPE | CHAR_TYPE | BOOL_TYPE;
fragment E_CONDITIONS: 'eq' | 'ne';
fragment Q_CONDITIONS: 'lt' | 'ge' | 'gt' | 'le';
fragment CONDITIONS: E_CONDITIONS | Q_CONDITIONS;

fragment STACK_TYPE: PRIMITIVE_TYPE | OBJECT_TYPE;
INSN_LOAD: STACK_TYPE 'load';
INSN_STORE: STACK_TYPE 'store';

fragment ARRAY_STACK_TYPE: STACK_TYPE | EXT_INT_TYPE | CHAR_TYPE;
INSN_ARRAY_LOAD: STACK_TYPE 'aload';
INSN_ARRAY_STORE: STACK_TYPE 'astore';

INSN_LOOKUPSWITCH: 'lookupswitch';
INSN_TABLESWITCH: 'tableswitch';

INSN_ACONST_NULL: 'aconst_null';
INSN_IPUSH: EXT_INT_TYPE 'ipush';

INSN_ICONST: 'iconst_' ('m1' | [012345]);
INSN_LCONST: 'lconst_' [01];
INSN_FCONST: 'fconst_' [012];
INSN_DCONST: 'dconst_' [01];

INSN_PUT: 'put' ('field' | 'static');
INSN_GET: 'get' ('field' | 'static');

INSN_INVOKEDYNAMIC: 'invokedynamic';
INSN_INVOKE: 'invoke' ('interface' | 'special' | 'static' | 'virtual');
INSN_MULTIANEWARRAY: 'multianewarray';
INSN_MONITORENTER: 'monitorenter';
INSN_MONITOREXIT: 'monitorexit';
INSN_ARRAYLENGTH: 'arraylength';
INSN_INSTANCEOF: 'instanceof';
INSN_IFNONNULL: 'ifnonnull';
INSN_ANEWARRAY: 'anewarray';
INSN_CHECKCAST: 'checkcast';
INSN_NEWARRAY: ARRAY_TYPE 'newarray';
INSN_IF_ACMP: 'if_acmp' E_CONDITIONS;
INSN_IF_ICMP: 'if_icmp' CONDITIONS;
INSN_RETURN: STACK_TYPE? 'return';
INSN_IFNULL: 'ifnull';
INSN_ATHROW: 'athrow';
INSN_IF: 'if' CONDITIONS;
INSN_SWAP: 'swap';
INSN_GOTO: 'goto';
INSN_IINC: 'iinc';
INSN_POP: 'pop' '2'?;
INSN_DUP: 'dup' '2'? ('_x' [12])?;
INSN_NOP: 'nop';
INSN_NEW: 'new';
INSN_JSR: 'jsr';
INSN_RET: 'ret';
INSN_LDC: 'ldc';

INSN_ADD: PRIMITIVE_TYPE 'add';
INSN_SUB: PRIMITIVE_TYPE 'sub';
INSN_MUL: PRIMITIVE_TYPE 'mul';
INSN_DIV: PRIMITIVE_TYPE 'div';
INSN_REM: PRIMITIVE_TYPE 'rem';
INSN_NEG: PRIMITIVE_TYPE 'neg';

INSN_USHR: INT_TYPE 'ushr';
INSN_SHL: INT_TYPE 'shl';
INSN_SHR: INT_TYPE 'shr';
INSN_AND: INT_TYPE 'and';
INSN_XOR: INT_TYPE 'xor';
INSN_OR: INT_TYPE 'or';

fragment L2D: 'l2d';
fragment L2F: 'l2f';
fragment L2I: 'l2i';
INSN_L2: L2D | L2F | L2I;

fragment D2F: 'd2f';
fragment D2I: 'd2i';
fragment D2L: 'd2l';
INSN_D2: D2F | D2I | D2L;

fragment F2D: 'f2d';
fragment F2I: 'f2i';
fragment F2L: 'f2l';
INSN_F2: F2D | F2I | F2L;

fragment I2B: 'i2b';
fragment I2C: 'i2c';
fragment I2D: 'i2d';
fragment I2F: 'i2f';
fragment I2L: 'i2l';
fragment I2S: 'i2s';
INSN_I2: I2B | I2C | I2D | I2F | I2L | I2S;

fragment BIN_DIGIT: [01_];
fragment DEC_DIGIT: [0-9_];
fragment HEX_DIGIT: [0-9a-fA-F_];
fragment OCT_DIGIT: [0-7_];

fragment LITERAL_DEC_INT: DEC_DIGIT+;
fragment LITERAL_BIN_INT: '0' [bB] BIN_DIGIT+;
fragment LITERAL_HEX_INT: '0' [xX] HEX_DIGIT+;
fragment LITERAL_OCT_INT: '0' [oO] OCT_DIGIT+;

LITERAL_INT: LITERAL_DEC_INT | LITERAL_BIN_INT | LITERAL_HEX_INT | LITERAL_OCT_INT;
LITERAL_FLOAT_LIKE: DEC_DIGIT+ ('.' DEC_DIGIT+)?;

fragment ESCAPED_CHAR: '\\' [nrbt0];
LITERAL_CHAR: SINGLE_QUOTE (ESCAPED_CHAR | ~[']) SINGLE_QUOTE;

IDENT: [a-zA-Z_]+[a-zA-Z0-9_]*;

ERROR: .;

mode M_CONST_STR;

M_CONST_STR_END: QUOTE -> popMode, type(QUOTE);
M_CONST_STR_LERP_BEGIN: '${' -> pushMode(DEFAULT_MODE);
M_CONST_STR_TEXT: ~('"' | '$')+;
M_CONST_STR_ERROR: ERROR -> type(ERROR);