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
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class JBPLSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("JBPL_KEYWORD",
        DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey IDENT = TextAttributesKey.createTextAttributesKey("JBPL_IDENT",
        DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey("JBPL_NUMBER",
        DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("JBPL_STRING",
        DefaultLanguageHighlighterColors.STRING);

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
                 JBPLLexer.KW_TYPE,
                 JBPLLexer.KW_TYPEOF,
                 JBPLLexer.KW_OPCODE,
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
                 JBPLLexer.KW_AS,
                 JBPLLexer.KW_IS,
                 JBPLLexer.KW_PREPRO_ASSERT,
                 JBPLLexer.KW_PREPRO_CLASS,
                 JBPLLexer.KW_PREPRO_DEFINE,
                 JBPLLexer.KW_PREPRO_INFO,
                 JBPLLexer.KW_PREPRO_ERROR,
                 JBPLLexer.KW_PREPRO_RETURN,
                 JBPLLexer.KW_PREPRO_INCLUDE,
                 JBPLLexer.KW_PREPRO_MACRO -> keys.add(KEYWORD);
            case JBPLLexer.LITERAL_INT, JBPLLexer.LITERAL_FLOAT_LIKE -> keys.add(NUMBER);
            case JBPLLexer.QUOTE, JBPLLexer.M_CONST_STR_TEXT -> keys.add(STRING);
            case JBPLLexer.IDENT -> keys.add(IDENT);
        } // @formatter:on
        return keys.toArray(TextAttributesKey[]::new);
    }
}
