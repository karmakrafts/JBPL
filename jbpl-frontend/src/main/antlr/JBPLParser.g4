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

assertStatement:
    KW_PREPRO_ASSERT
    expr
    ;

infoStatement:
    KW_PREPRO_INFO
    expr
    ;

errorStatement:
    KW_PREPRO_ERROR
    expr
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

macroSignature:
    exprOrName
    L_PAREN
    (parameter
    (COMMA
    parameter)*
    )?
    R_PAREN
    (COLON
    exprOrType)?
    ;

macro:
    KW_PREPRO_MACRO
    macroSignature
    L_BRACE
    (bodyElement
    | NL)*?
    R_BRACE
    ;

macroCall:
    IDENT
    CARET
    L_PAREN
    (argument
    (COMMA
    argument)*
    )?
    R_PAREN
    ;

argument:
    namedArgument
    | expr
    ;

namedArgument:
    exprOrName
    COLON
    expr
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
    exprOrName
    L_PAREN
    (parameter
    (COMMA
    parameter)*
    )?
    R_PAREN
    ;

parameter:
    exprOrName
    COLON
    exprOrType
    ;

exprOrName:
    wrappedExpr
    | nameSegment
    ;

exprOrType:
    wrappedExpr
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
    (argument
    (COMMA
    argument)*
    )?
    R_PAREN
    ;

// Language definitions

expr:
    L_PAREN NL*? expr NL*? R_PAREN
    | expr NL*? L_SQBRACKET NL*? expr NL*? R_SQBRACKET

    | expr NL*? DOT NL*? macroCall
    | expr NL*? DOT NL*? reference

    | expr NL*? SPACESHIP NL*? expr
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

    | expr KW_IS (wrappedExpr | type)
    | expr KW_AS (wrappedExpr | type)

    | ifExpr
    | arrayExpr
    | macroCall
    | signatureExpr
    | typeOfExpr
    | opcodeOfExpr
    | defaultExpr
    | definedExpr
    | nameOfExpr
    | preproClassInstantiation
    | injectorReference
    | selectorReference
    | reference
    | literal
    ;

wrappedExpr:
    DOLLAR
    L_BRACE
    expr
    R_BRACE
    ;

defaultExpr:
    KW_DEFAULT
    L_PAREN
    expr
    R_PAREN
    ;

arrayExpr:
    L_SQBRACKET
    exprOrType?
    R_SQBRACKET
    L_BRACE
    (expr
    (COMMA
    expr)*
    )?
    R_BRACE
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

reference: // Impplicit references
    IDENT
    ;

opcodeOfExpr:
    KW_OPCODEOF
    L_PAREN
    expr
    R_PAREN
    ;

typeOfExpr:
    KW_TYPEOF
    L_PAREN
    expr
    R_PAREN
    ;

typeLiteral:
    KW_TYPE
    L_PAREN
    (type
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
    (wrappedExpr
    | fieldSignature)
    (EQ
    expr)?
    ;

fieldSignature:
    signatureOwner
    NL*?
    DOT
    NL*?
    exprOrName
    NL*?
    COLON
    NL*?
    exprOrType
    ;

signatureOwner:
    wrappedExpr
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
    exprOrName
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
    expr
    ;

injector:
    KW_INJECT
    NL*?
    (functionSignature
    | reference)
    NL*?
    KW_BY
    NL*?
    exprOrName
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
    exprOrType
    ;

functionSignatureParameter:
    (exprOrName
    COLON)?
    exprOrType
    ;

specialFunctionName:
    L_ABRACKET
    IDENT
    R_ABRACKET
    ;

functionName:
    specialFunctionName
    | exprOrName
    ;

statement:
    include
    | define
    | returnStatement
    | yeetStatement
    | versionStatement
    | infoStatement
    | errorStatement
    | assertStatement
    | label
    | local
    | instruction
    | expr
    ;

local:
    KW_LOCAL
    exprOrName
    COLON
    exprOrType
    (EQ
    expr)? // Allow explicit assignment of local slot index
    ;

instructionLiteral:
    KW_INSTRUCTION
    L_PAREN
    instruction
    R_PAREN
    ;

instruction:
    load
    | fieldLoad
    | store
    | fieldStore
    | jump
    | invokedynamic
    | invoke
    | ret
    | ipush
    | ldc
    | typeInstruction
    | oplessInstruction
    ;

ret:
    INSN_RET
    (intLiteral
    | IDENT)
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
    exprOrName
    ;

jumpInstruction:
    (INSN_GOTO
    | INSN_IF
    | INSN_IF_ACMP
    | INSN_IF_ICMP
    | INSN_JSR)
    ;

jump:
    jumpInstruction
    exprOrName
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

constInstruction:
    INSN_ACONST_NULL
    | INSN_ICONST
    | INSN_LCONST
    | INSN_FCONST
    | INSN_DCONST
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
    | typeLiteral
    | opcodeLiteral
    | instructionLiteral
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
    | constInstruction
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

opcodeLiteral:
    KW_OPCODE
    L_PAREN
    opcode
    R_PAREN
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

softKeyword:
    KW_TYPE
    | KW_OPCODE
    | KW_VERSION
    | KW_LOCAL
    ;

nameSegment:
    (IDENT
    | softKeyword)
    (DOT
    | DOLLAR
    | IDENT
    | softKeyword
    | LITERAL_INT)*
    ;