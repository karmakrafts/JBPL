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

defineModifiers:
    KW_PRIVATE? // Only accessible current scope
    KW_FINAL? // Immutable define
    ;

define:
    defineModifiers
    KW_DEFINE
    NL*?
    exprOrName
    COLON
    exprOrType
    EQ
    expr
    ;

include:
    KW_INCLUDE
    simpleStringLiteral
    ;

assertStatement:
    KW_ASSERT
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

continueStatement:
    KW_CONTINUE
    ;

breakStatement:
    KW_BREAK
    ;

returnStatement:
    KW_PREPRO_RETURN
    expr?
    ;

typeParameter:
    exprOrName
    (COLON
    exprOrType)
    ;

macroSignature:
    exprOrName
    (L_SQBRACKET
    typeParameter
    (COMMA
    typeParameter)*
    R_SQBRACKET)?
    L_PAREN
    (parameter
    (COMMA
    parameter)*
    )?
    R_PAREN
    (COLON
    exprOrType)?
    ;

macroModifiers:
    KW_PRIVATE?
    ;

macro:
    macroModifiers
    KW_MACRO
    macroSignature
    L_BRACE
    (bodyElement
    | NL)*?
    R_BRACE
    ;

macroCall:
    exprOrName
    (L_SQBRACKET
    (typeArgument
    (COMMA
    typeArgument)*
    )
    R_SQBRACKET)?
    L_PAREN
    (argument
    (COMMA
    argument)*
    )?
    R_PAREN
    ;

typeArgument:
    namedTypeArgument
    | type
    ;

namedTypeArgument:
    exprOrName
    EQ
    type
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
    exprOrClassType
    (COLON
    (superType
    (COMMA
    superType)*
    ))?
    ;

superType:
    AT?
    exprOrClassType
    ;

enumDecl:
    accessModifier*?
    KW_ENUM
    exprOrClassType
    ;

exprOrClassType:
    wrappedExpr
    | classType
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

    | expr NL*? EXCL_RANGE NL*? expr
    | expr NL*? DOTDOT NL*? expr

    | expr NL*? DOT NL*? macroCall // TODO: allow interpolated calls
    | expr NL*? DOT NL*? (wrappedExpr | reference)

    | expr NL*? SPACESHIP NL*? expr
    | expr NL*? AMPAMP NL*? expr
    | expr NL*? PIPEPIPE NL*? expr
    | expr NL*? AMP NL*? expr
    | expr NL*? PIPE NL*? expr
    | expr NL*? CARET NL*? expr
    | expr NL*? LSH NL*? expr
    | expr NL*? URSH NL*? expr
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

    | INC expr
    | expr INC
    | DEC expr
    | expr DEC

    | expr KW_IS exprOrType
    | expr KW_AS exprOrType
    | expr KW_IN expr

    | <assoc=right> expr NL*? EQ NL*? expr // Assignments
    | <assoc=right> expr NL*? PLUS_ASSIGN NL*? expr
    | <assoc=right> expr NL*? MINUS_ASSIGN NL*? expr
    | <assoc=right> expr NL*? TIMES_ASSIGN NL*? expr
    | <assoc=right> expr NL*? DIV_ASSIGN NL*? expr
    | <assoc=right> expr NL*? REM_ASSIGN NL*? expr
    | <assoc=right> expr NL*? LSH_ASSIGN NL*? expr
    | <assoc=right> expr NL*? RSH_ASSIGN NL*? expr
    | <assoc=right> expr NL*? URSH_ASSIGN NL*? expr
    | <assoc=right> expr NL*? AND_ASSIGN NL*? expr
    | <assoc=right> expr NL*? OR_ASSIGN NL*? expr
    | <assoc=right> expr NL*? XOR_ASSIGN NL*? expr

    | ifExpr
    | whenExpr
    | arrayExpr
    | macroCall
    | signatureExpr
    | typeOfExpr
    | opcodeOfExpr
    | sizeOfExpr
    | defaultExpr
    | definedExpr
    | nameOfExpr
    | preproClassInstantiation
    | injectorReference
    | functionScopeReference
    | fieldScopeReference
    | wrappedExpr // Interpolated references
    | literal
    | reference
    ;

fieldScopeReference: // field.access for example inside inject body
    KW_FIELD
    DOT
    exprOrName
    ;

functionScopeReference: // fun.instructions for example inside fun or inject body
    KW_FUN
    DOT
    exprOrName
    ;

whenExpr:
    KW_WHEN
    L_PAREN
    expr
    R_PAREN
    L_BRACE
    (whenBranch
    | NL)*?
    defaultWhenBranch?
    R_BRACE
    ;

defaultWhenBranch:
    KW_ELSE
    ARROW
    whenBranchBody
    ;

whenBranch:
    expr
    ARROW
    whenBranchBody
    ;

whenBranchBody:
    ((L_BRACE
    (bodyElement
    | NL)*?
    R_BRACE
    NL+)
    | bodyElement
    NL+)
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
    KW_SIGNATURE
    (functionSignature
    | fieldSignature)
    ;

reference: // Impplicit references
    (softKeyword
    | IDENT)
    ;

sizeOfExpr:
    KW_SIZEOF
    L_PAREN
    expr
    R_PAREN
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
    type
    ;

declaration:
    preproClass
    | classDecl
    | enumDecl
    | macro
    | function
    | field
    | injector
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
    | KW_VOLATILE
    | KW_TRANSIENT
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

injector:
    KW_INJECT
    NL*?
    (functionSignature
    | fieldSignature
    | wrappedExpr)
    NL*?
    (KW_BY
    NL*?
    exprOrName)?
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
    | typeAliasStatement
    | forStatement
    | returnStatement
    | yeetStatement
    | versionStatement
    | assertStatement
    | breakStatement
    | continueStatement
    | label
    | local
    | instruction
    | expr
    ;

typeAliasStatement:
    KW_PRIVATE?
    KW_TYPE
    exprOrName
    EQ
    exprOrType
    ;

forStatement:
    KW_FOR
    L_PAREN
    exprOrName
    KW_IN
    expr
    R_PAREN
    L_BRACE
    (bodyElement
    | NL)*?
    R_BRACE
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
    stackInstruction
    | fieldInstruction
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
    (wrappedExpr
    | intLiteral
    | IDENT)
    ;

stackInstruction:
    (INSN_LOAD
    | INSN_STORE)
    (wrappedExpr
    | intLiteral
    | IDENT)
    ;

fieldInstruction:
    (INSN_GET
    | INSN_PUT)
    (wrappedExpr
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
    (wrappedExpr
    | classType)
    ;

ldc:
    INSN_LDC
    (wrappedExpr
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
    opcode
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
    | KW_SIGNATURE
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