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
        JBPLLexer.KW_DEFAULT,
        JBPLLexer.KW_INJECT,
        JBPLLexer.KW_STATIC,
        JBPLLexer.KW_FINAL,
        JBPLLexer.KW_SYNC,
        JBPLLexer.KW_FIELD,
        JBPLLexer.KW_FUN,
        JBPLLexer.KW_IF,
        JBPLLexer.KW_ELSE,
        JBPLLexer.KW_IS,
        JBPLLexer.KW_AS,
        JBPLLexer.KW_BY,
        JBPLLexer.KW_IN,
        JBPLLexer.KW_MACRO,
        JBPLLexer.KW_DEFINE,
        JBPLLexer.KW_FOR,
        JBPLLexer.KW_INFO,
        JBPLLexer.KW_ERROR,
        JBPLLexer.KW_PREPRO_CLASS,
        JBPLLexer.KW_INCLUDE,
        JBPLLexer.KW_PREPRO_RETURN);
    private static final Set<Integer> STRING_TOKENS = Set.of(JBPLLexer.QUOTE,
        JBPLLexer.M_CONST_STR_TEXT,
        JBPLLexer.SINGLE_QUOTE,
        JBPLLexer.LITERAL_CHAR);
    private static final Set<Integer> NUMERIC_TOKENS = Set.of(JBPLLexer.LITERAL_INT, JBPLLexer.LITERAL_FLOAT_LIKE);
    private static final Set<Integer> INSN_TOKENS = Set.of(JBPLLexer.INSN_INVOKE,
        JBPLLexer.INSN_F2,
        JBPLLexer.INSN_D2,
        JBPLLexer.INSN_I2,
        JBPLLexer.INSN_L2,
        JBPLLexer.INSN_STORE,
        JBPLLexer.INSN_LOAD,
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
        JBPLLexer.INSN_IF,
        JBPLLexer.INSN_IF_ACMP,
        JBPLLexer.INSN_IF_ICMP,
        JBPLLexer.INSN_IFNONNULL,
        JBPLLexer.INSN_IFNULL,
        JBPLLexer.INSN_GOTO,
        JBPLLexer.INSN_PUT,
        JBPLLexer.INSN_GET,
        JBPLLexer.INSN_MONITORENTER,
        JBPLLexer.INSN_MONITOREXIT);

    private SourceColorizer() {
    }

    private static void applyTokenColor(final @NotNull Ansi builder,
                                        final @NotNull Token token,
                                        final @NotNull State state) {
        final var type = token.getType();
        if (type == JBPLLexer.ERROR) {
            builder.fgBrightRed();
            state.previousType = type;
            return;
        }
        if (INSN_TOKENS.contains(type)) {
            builder.fgBrightCyan();
            state.previousType = type;
            return;
        }
        if (KEYWORDS.contains(type)) {
            // Number suffixes shall have the same color as the number itself
            if (state.previousType == JBPLLexer.LITERAL_INT || state.previousType == JBPLLexer.LITERAL_FLOAT_LIKE) {
                builder.fgBrightBlue();
                state.previousType = type;
                return;
            }
            builder.fgBrightMagenta();
            state.previousType = type;
            return;
        }
        if (STRING_TOKENS.contains(type)) {
            builder.fgBrightGreen();
            state.previousType = type;
            return;
        }
        if (NUMERIC_TOKENS.contains(type)) {
            builder.fgBrightBlue();
            state.previousType = type;
            return;
        }
        if (type == JBPLLexer.IDENT) {
            builder.fgBrightYellow();
            state.previousType = type;
            return;
        }
        builder.fgBright(Color.WHITE);
        state.previousType = type;
    }

    public static @NotNull String colorize(final @NotNull List<Token> tokens) {
        final var builder = Ansi.ansi();
        final var state = new State();
        for (final var token : tokens) {
            applyTokenColor(builder, token, state);
            builder.a(token.getText());
            builder.reset();
        }
        return builder.toString();
    }

    private static final class State {
        public int previousType;
    }
}
