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

package dev.karmakrafts.jbpl.assembler.source;

import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import org.antlr.v4.runtime.Token;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class SourceColorizer {
    private static final Set<Integer> KEYWORDS = Set.of(JBPLLexer.KW_VOID,
        JBPLLexer.KW_I8,
        JBPLLexer.KW_I16,
        JBPLLexer.KW_I32,
        JBPLLexer.KW_I64,
        JBPLLexer.KW_F32,
        JBPLLexer.KW_F64,
        JBPLLexer.KW_BOOL,
        JBPLLexer.KW_CHAR,
        JBPLLexer.KW_STRING,
        JBPLLexer.KW_TYPE,
        JBPLLexer.KW_TYPEOF,
        JBPLLexer.KW_TRUE,
        JBPLLexer.KW_FALSE,
        JBPLLexer.KW_OPCODE,
        JBPLLexer.KW_OPCODEOF,
        JBPLLexer.KW_PUBLIC,
        JBPLLexer.KW_PRIVATE,
        JBPLLexer.KW_PROTECTED,
        JBPLLexer.KW_STATIC,
        JBPLLexer.KW_FINAL,
        JBPLLexer.KW_SYNC,
        JBPLLexer.KW_IS,
        JBPLLexer.KW_AS);

    private static final Set<Integer> PREPRO_KEYWORDS = Set.of(JBPLLexer.KW_PREPRO_DEFINE,
        JBPLLexer.KW_PREPRO_INFO,
        JBPLLexer.KW_PREPRO_ERROR,
        JBPLLexer.KW_PREPRO_CLASS,
        JBPLLexer.KW_PREPRO_INCLUDE,
        JBPLLexer.KW_PREPRO_MACRO,
        JBPLLexer.KW_PREPRO_RETURN);

    private static final Set<Integer> STRING_TOKENS = Set.of(JBPLLexer.QUOTE,
        JBPLLexer.M_CONST_STR_TEXT,
        JBPLLexer.SINGLE_QUOTE,
        JBPLLexer.LITERAL_CHAR);

    private static final Set<Integer> NUMERIC_TOKENS = Set.of(JBPLLexer.LITERAL_INT, JBPLLexer.LITERAL_FLOAT_LIKE);

    private SourceColorizer() {
    }

    private static void applyTokenColor(final @NotNull Ansi builder, final @NotNull Token token) {
        final var type = token.getType();
        if (PREPRO_KEYWORDS.contains(type)) {
            builder.fgMagenta();
            return;
        }
        if (KEYWORDS.contains(type)) {
            builder.fgBrightMagenta();
            return;
        }
        if (STRING_TOKENS.contains(type)) {
            builder.fgBrightGreen();
            return;
        }
        if (NUMERIC_TOKENS.contains(type)) {
            builder.fgBrightBlue();
            return;
        }
        if (type == JBPLLexer.IDENT) {
            builder.fgBrightYellow();
            return;
        }
        builder.fgBright(Color.WHITE);
    }

    public static @NotNull String colorize(final @NotNull List<Token> tokens) {
        final var builder = Ansi.ansi();
        for (final var token : tokens) {
            applyTokenColor(builder, token);
            builder.a(token.getText());
            builder.reset();
        }
        return builder.toString();
    }
}
