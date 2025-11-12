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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record SourceLine(List<Token> tokens, int lineIndex) implements Comparable<SourceLine> {
    @Override
    public int compareTo(final @NotNull SourceLine o) {
        return Integer.compare(lineIndex, o.lineIndex);
    }

    public int getLineNumber() {
        return lineIndex + 1;
    }

    public int getLineNumberLength() {
        return Integer.toString(getLineNumber()).length();
    }

    public int getLength() {
        return toString().length();
    }

    public boolean endsWithNewline() {
        return toString().endsWith("\n") && !tokens.isEmpty() && tokens.get(tokens.size() - 1).getType() != JBPLLexer.EOF;
    }

    public @NotNull String renderWithColor() { // @formatter:off
        return SourceColorizer.colorize(tokens.stream()
            .filter(token -> token.getType() != Token.EOF)
            .toList());
    } // @formatter:on

    @Override
    public @NotNull String toString() {
        if (tokens.isEmpty()) {
            return "";
        }
        // @formatter:off
        return tokens.stream()
            .filter(token -> token.getType() != Token.EOF)
            .map(Token::getText)
            .collect(Collectors.joining());
        // @formatter:on
    }
}
