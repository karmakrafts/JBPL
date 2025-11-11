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

package dev.karmakrafts.jbpl.intellij;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.util.TextAttributeKeys;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class JBPLSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    static {
        JBPLParserDefinition.ensureTokenTypesRegistered();
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new ANTLRLexerAdaptor(JBPLanguage.INSTANCE, new JBPLLexer(null));
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(final @NotNull IElementType elementType) {
        if (!(elementType instanceof TokenIElementType tokenType)) {
            return EMPTY_KEYS;
        }
        final var keys = new ArrayList<TextAttributesKey>();
        switch (tokenType.getANTLRTokenType()) { // @formatter:off
            case JBPLLexer.KW_I8,
                 JBPLLexer.KW_I16,
                 JBPLLexer.KW_I32,
                 JBPLLexer.KW_I64,
                 JBPLLexer.KW_F32,
                 JBPLLexer.KW_F64,
                 JBPLLexer.KW_BOOL,
                 JBPLLexer.KW_CHAR,
                 JBPLLexer.KW_STRING,
                 JBPLLexer.KW_VOID,
                 JBPLLexer.KW_TRUE,
                 JBPLLexer.KW_FALSE,
                 JBPLLexer.KW_TYPEOF,
                 JBPLLexer.KW_OPCODEOF,
                 JBPLLexer.KW_INSTRUCTION,
                 JBPLLexer.KW_PUBLIC,
                 JBPLLexer.KW_PRIVATE,
                 JBPLLexer.KW_PROTECTED,
                 JBPLLexer.KW_STATIC,
                 JBPLLexer.KW_SYNC,
                 JBPLLexer.KW_FUN,
                 JBPLLexer.KW_FIELD,
                 JBPLLexer.KW_INJECT,
                 JBPLLexer.KW_SELECTOR,
                 JBPLLexer.KW_YEET,
                 JBPLLexer.KW_AFTER,
                 JBPLLexer.KW_BEFORE,
                 JBPLLexer.KW_DEFAULT,
                 JBPLLexer.KW_FINAL,
                 JBPLLexer.KW_FOR,
                 JBPLLexer.KW_AS,
                 JBPLLexer.KW_IS,
                 JBPLLexer.KW_IN,
                 JBPLLexer.KW_BY,
                 JBPLLexer.KW_IF,
                 JBPLLexer.KW_ELSE,
                 JBPLLexer.KW_OFFSET,
                 JBPLLexer.KW_SIGNATURE,
                 JBPLLexer.KW_MACRO,
                 JBPLLexer.KW_DEFINE,
                 JBPLLexer.KW_SIZEOF,
                 JBPLLexer.KW_PREPRO_ASSERT,
                 JBPLLexer.KW_PREPRO_CLASS,
                 JBPLLexer.KW_PREPRO_INFO,
                 JBPLLexer.KW_PREPRO_ERROR,
                 JBPLLexer.KW_PREPRO_RETURN,
                 JBPLLexer.KW_PREPRO_INCLUDE -> keys.add(TextAttributeKeys.KEYWORD);
            case JBPLLexer.LITERAL_INT,
                 JBPLLexer.LITERAL_FLOAT_LIKE -> keys.add(TextAttributeKeys.NUMBER);
            case JBPLLexer.QUOTE,
                 JBPLLexer.M_CONST_STR_TEXT -> keys.add(TextAttributeKeys.STRING);
            case JBPLLexer.SINGLE_QUOTE,
                 JBPLLexer.LITERAL_CHAR -> keys.add(TextAttributeKeys.CHAR);
            case JBPLLexer.L_PAREN,
                 JBPLLexer.R_PAREN -> keys.add(TextAttributeKeys.PAREN);
            case JBPLLexer.L_BRACE,
                 JBPLLexer.R_BRACE -> keys.add(TextAttributeKeys.BRACE);
            case JBPLLexer.L_SQBRACKET,
                 JBPLLexer.R_SQBRACKET -> keys.add(TextAttributeKeys.BRACKET);
            case JBPLLexer.L_ABRACKET,
                 JBPLLexer.R_ABRACKET -> keys.add(TextAttributeKeys.ANGLE_BRACKET);
            case JBPLLexer.INSN_D2,
                 JBPLLexer.INSN_F2,
                 JBPLLexer.INSN_I2,
                 JBPLLexer.INSN_L2,
                 JBPLLexer.INSN_INVOKE,
                 JBPLLexer.INSN_INVOKEDYNAMIC,
                 JBPLLexer.INSN_GOTO,
                 JBPLLexer.INSN_LOAD,
                 JBPLLexer.INSN_STORE,
                 JBPLLexer.INSN_ARRAY_LOAD,
                 JBPLLexer.INSN_ARRAY_STORE,
                 JBPLLexer.INSN_LDC,
                 JBPLLexer.INSN_GET,
                 JBPLLexer.INSN_PUT,
                 JBPLLexer.INSN_ADD,
                 JBPLLexer.INSN_SUB,
                 JBPLLexer.INSN_MUL,
                 JBPLLexer.INSN_DIV,
                 JBPLLexer.INSN_REM,
                 JBPLLexer.INSN_SHL,
                 JBPLLexer.INSN_SHR,
                 JBPLLexer.INSN_USHR,
                 JBPLLexer.INSN_AND,
                 JBPLLexer.INSN_OR,
                 JBPLLexer.INSN_XOR,
                 JBPLLexer.INSN_RETURN,
                 JBPLLexer.INSN_MONITORENTER,
                 JBPLLexer.INSN_MONITOREXIT,
                 JBPLLexer.INSN_IPUSH,
                 JBPLLexer.INSN_SWAP,
                 JBPLLexer.INSN_JSR,
                 JBPLLexer.INSN_RET,
                 JBPLLexer.INSN_NEG,
                 JBPLLexer.INSN_NOP,
                 JBPLLexer.INSN_POP,
                 JBPLLexer.INSN_DUP -> keys.add(TextAttributeKeys.INSTRUCTION);
            case JBPLLexer.DOT -> keys.add(TextAttributeKeys.DOT);
            case JBPLLexer.IDENT -> keys.add(TextAttributeKeys.IDENT);
            case JBPLLexer.LINE_COMMENT -> keys.add(TextAttributeKeys.LINE_COMMENT);
            case JBPLLexer.BLOCK_COMMENT -> keys.add(TextAttributeKeys.BLOCK_COMMENT);
        } // @formatter:on
        return keys.toArray(TextAttributesKey[]::new);
    }
}
