parser grammar JBPLParser;

options {
    tokenVocab = JBPLLexer;
}

file:
    (bodyElement
    | NL)*?
    EOF
    ;

bodyElement:
    declaration
    | statement
    ;

reportStatement:
    (KW_PREPRO_ERROR
    | KW_PREPRO_INFO)
    L_PAREN
    stringLiteral
    R_PAREN
    ;

ifExpr:
    KW_IF
    L_PAREN
    expr
    R_PAREN
    (ifBody
    | bodyElement)
    (elseIfBranch
    | NL)*?
    elseBranch?
    ;

ifBody:
    L_BRACE
    (bodyElement
    | NL)*?
    R_BRACE
    ;

elseIfBranch:
    KW_ELSE
    KW_IF
    L_PAREN
    expr
    R_PAREN
    (ifBody
    | bodyElement)
    ;

elseBranch:
    KW_ELSE
    (ifBody
    | bodyElement)
    ;

define:
    KW_PREPRO_DEFINE
    NL*?
    IDENT
    COLON
    type
    EQ
    expr
    ;

include:
    KW_PREPRO_INCLUDE
    simpleStringLiteral
    ;

versionStatement:
    KW_VERSION
    expr
    ;

yeetStatement:
    KW_YEET
    (functionSignature
    | fieldSignature
    | classType)
    ;

returnStatement:
    KW_PREPRO_RETURN
    expr?
    ;

macro:
    KW_PREPRO_MACRO
    IDENT
    L_PAREN
    (parameter
    (COMMA
    parameter)*
    )?
    R_PAREN
    (COLON
    type)?
    L_BRACE
    (bodyElement
    | NL)*?
    R_BRACE
    ;

macroCall:
    IDENT
    CARET
    L_PAREN
    (expr
    (COMMA
    expr)*
    )?
    R_PAREN
    ;

classDecl:
    accessModifier*?
    KW_CLASS
    (reference
    | classType)
    ;

enumDecl:
    accessModifier*?
    KW_ENUM
    (reference
    | classType)
    ;

preproClass:
    KW_PREPRO_CLASS
    IDENT
    L_PAREN
    (parameter
    (COMMA
    parameter)*
    )?
    R_PAREN
    ;

parameter:
    refOrName
    COLON
    refOrType
    ;

refOrName:
    reference
    | nameSegment
    ;

refOrType:
    reference
    | type
    ;

signatureType:
    KW_SIGNATURE
    L_PAREN
    (KW_FIELD
    | KW_FUN)
    R_PAREN
    ;

intersectionType:
    L_PAREN
    type
    (PIPE
    type)+
    R_PAREN
    ;

preproClassInstantiation:
    IDENT
    L_PAREN
    (expr
    (COMMA
    expr)*
    )?
    R_PAREN
    ;

// Language definitions

expr:
    L_PAREN NL*? expr NL*? R_PAREN

    | expr NL*? DOT NL*? macroCall
    | expr NL*? DOT NL*? reference

    | expr NL*? AMPAMP NL*? expr
    | expr NL*? PIPEPIPE NL*? expr
    | expr NL*? AMP NL*? expr
    | expr NL*? PIPE NL*? expr
    | expr NL*? CARET NL*? expr
    | expr NL*? LSH NL*? expr
    | expr NL*? RSH NL*? expr

    | expr NL*? LEQ NL*? expr
    | expr NL*? L_ABRACKET NL*? expr
    | expr NL*? GEQ NL*? expr
    | expr NL*? R_ABRACKET NL*? expr
    | expr NL*? EQEQ NL*? expr
    | expr NL*? NEQ NL*? expr

    | expr NL*? ASTERISK NL*? expr
    | expr NL*? SLASH NL*? expr
    | expr NL*? REM NL*? expr
    | expr NL*? PLUS NL*? expr
    | expr NL*? MINUS NL*? expr

    | TILDE expr
    | MINUS expr
    | PLUS expr
    | EXCL expr

    | expr KW_IS type

    | ifExpr
    | macroCall
    | signatureExpr
    | typeOfExpr
    | opcodeOfExpr
    | definedExpr
    | nameOfExpr
    | preproClassInstantiation
    | injectorReference
    | selectorReference
    | reference
    | literal
    ;

injectorReference:
    KW_INJECT
    L_PAREN
    IDENT
    R_PAREN
    ;

selectorReference:
    KW_SELECTOR
    L_PAREN
    IDENT
    R_PAREN
    ;

nameOfExpr:
    KW_NAMEOF
    L_PAREN
    expr
    R_PAREN
    ;

definedExpr:
    KW_DEFINED
    L_PAREN
    expr
    R_PAREN
    ;

signatureExpr:
    functionSignature
    | fieldSignature
    ;

reference:
    DOLLAR
    L_BRACE
    IDENT
    R_BRACE
    ;

opcodeOfExpr:
    KW_OPCODEOF
    L_PAREN
    (opcode
    | expr)
    R_PAREN
    ;

typeOfExpr:
    KW_TYPEOF
    L_PAREN
    (expr
    | type
    | diamond)
    R_PAREN
    ;

diamond:
    L_ABRACKET
    R_ABRACKET
    ;

declaration:
    preproClass
    | classDecl
    | enumDecl
    | macro
    | function
    | field
    | injector
    | selector
    ;

field:
    accessModifier*?
    KW_FIELD
    (reference
    | fieldSignature)
    (EQ
    expr)?
    ;

fieldSignature:
    signatureOwner
    NL*?
    DOT
    NL*?
    refOrName
    NL*?
    COLON
    NL*?
    refOrType
    ;

signatureOwner:
    reference
    | classType
    ;

accessModifier:
    KW_PUBLIC
    | KW_PROTECTED
    | KW_PRIVATE
    | KW_STATIC
    | KW_FINAL
    | KW_SYNC
    ;

function:
    accessModifier*?
    KW_FUN
    functionSignature
    L_BRACE
    (statement
    | NL)*?
    R_BRACE
    ;

selector:
    KW_SELECTOR
    NL*?
    IDENT
    NL*?
    L_BRACE
    (selectionStatement
    | selectionOffset
    | NL)*?
    R_BRACE
    ;

selectionOffset:
    KW_OFFSET
    expr
    ;

selectionStatement:
    (KW_AFTER
    | KW_BEFORE)
    (instruction
    | opcode)
    ;

injector:
    KW_INJECT
    NL*?
    (functionSignature
    | reference)
    NL*?
    KW_BY
    NL*?
    refOrName
    L_BRACE
    (statement
    | NL)*?
    R_BRACE
    ;

functionSignature:
    signatureOwner
    NL*?
    DOT
    NL*?
    functionName
    NL*?
    L_PAREN
    NL*?
    (functionSignatureParameter
    ((COMMA
    functionSignatureParameter)
    | NL)*
    )?
    NL*?
    R_PAREN
    NL*?
    COLON
    NL*?
    refOrType
    ;

functionSignatureParameter:
    (refOrName
    COLON)?
    refOrType
    ;

specialFunctionName:
    L_ABRACKET
    IDENT
    R_ABRACKET
    ;

functionName:
    specialFunctionName
    | refOrName
    ;

statement:
    include
    | define
    | reportStatement
    | returnStatement
    | yeetStatement
    | versionStatement
    | label
    | local
    | instruction
    | expr
    ;

local:
    KW_LOCAL
    refOrName
    COLON
    refOrType
    ;

instruction:
    load
    | fieldLoad
    | store
    | fieldStore
    | jump
    | invokedynamic
    | invoke
    | ipush
    | ldc
    | typeInstruction
    | oplessInstruction
    ;

load:
    INSN_LOAD
    (intLiteral
    | IDENT)
    ;

store:
    INSN_STORE
    (intLiteral
    | IDENT)
    ;

fieldLoad:
    INSN_GET
    (reference
    | fieldSignature)
    ;

fieldStore:
    INSN_PUT
    (reference
    | fieldSignature)
    ;

label:
    COLON
    refOrName
    ;

jump:
    (INSN_GOTO
    | INSN_IF
    | INSN_IF_ACMP
    | INSN_IF_ICMP)
    IDENT
    ;

typeInstruction:
    (INSN_NEW
    | INSN_CHECKCAST
    | INSN_INSTANCEOF)
    (reference
    | classType)
    ;

ldc:
    INSN_LDC
    (reference
    | literal)
    ;

ipush:
    INSN_IPUSH
    intLiteral
    ;

invokedynamic:
    INSN_INVOKEDYNAMIC
    functionSignature
    NL*?
    (KW_BY
    NL*?
    L_PAREN
    NL*?
    functionSignature
    NL*?
    R_PAREN)? // For specifying optional SAM signature for generic interfaces
    NL*?
    L_BRACE
    NL*?
    invoke
    NL*?
    COMMA
    NL*?
    invoke
    NL*?
    R_BRACE
    NL*?
    (L_PAREN
    NL*?
    (expr
    ((COMMA
    expr)
    | NL)*)?
    NL*?
    R_PAREN)?
    ;

invoke:
    INSN_INVOKE
    functionSignature
    ;

literal:
    stringLiteral
    | boolLiteral
    | LITERAL_CHAR
    | floatLiteral
    | intLiteral
    ;

boolLiteral:
    KW_FALSE
    | KW_TRUE
    ;

floatLiteral:
    (LITERAL_FLOAT_LIKE
    | LITERAL_INT)
    (KW_F32
    | KW_F64)
    ;

intLiteral:
    LITERAL_INT
    (KW_I8
    | KW_I16
    | KW_I32
    | KW_I64)?
    ;

stringLiteral:
    QUOTE
    stringSegment*?
    QUOTE
    ;

stringSegment:
    (M_CONST_STR_LERP_BEGIN
    expr
    R_BRACE)
    | M_CONST_STR_TEXT
    ;

simpleStringLiteral:
    QUOTE
    M_CONST_STR_TEXT*?
    QUOTE
    ;

oplessInstruction:
    INSN_NOP
    | INSN_POP
    | INSN_DUP
    | INSN_IINC
    | INSN_SWAP
    | INSN_ATHROW
    | INSN_RETURN
    | INSN_NEWARRAY
    | INSN_ANEWARRAY
    | INSN_ARRAYLENGTH
    | INSN_MONITORENTER
    | INSN_MONITOREXIT
    | INSN_MULTIANEWARRAY
    | INSN_ARRAY_LOAD
    | INSN_ARRAY_STORE
    | arithmeticInstruction
    | logicInstruction
    ;

arithmeticInstruction:
    INSN_ADD
    | INSN_SUB
    | INSN_MUL
    | INSN_DIV
    | INSN_REM
    | INSN_NEG
    ;

logicInstruction:
    INSN_USHR
    | INSN_SHL
    | INSN_SHR
    | INSN_AND
    | INSN_XOR
    | INSN_OR
    ;

opcode:
    INSN_INVOKE
    | INSN_LOAD
    | INSN_STORE
    | INSN_NEW
    | INSN_LDC
    | INSN_IPUSH
    | INSN_RETURN
    | INSN_JSR
    | INSN_RET
    | oplessInstruction
    ;

arrayType:
    L_SQBRACKET
    type
    R_SQBRACKET
    ;

type:
    arrayType
    | intersectionType
    | signatureType
    | classType
    | KW_SELECTOR
    | KW_TYPE
    | KW_STRING
    | KW_OPCODE
    | KW_INSTRUCTION
    | KW_VOID
    | KW_BOOL
    | KW_CHAR
    | KW_I16
    | KW_I32
    | KW_I64
    | KW_F32
    | KW_F64
    | KW_I8
    | IDENT // This handles preprocessor types like prepro classes
    ;

classType:
    L_ABRACKET
    nameSegment
    (SLASH
    nameSegment)*
    R_ABRACKET
    ;

nameSegment:
    IDENT
    (DOT
    | DOLLAR
    | IDENT
    | LITERAL_INT)*
    ;