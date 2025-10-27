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

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SourceRenderer {
    private SourceRenderer() {
    }

    public static @NotNull String render(final @NotNull List<Token> tokens) {
        return tokens.stream().map(Token::getText).collect(Collectors.joining());
    }

    public static @NotNull String render(final @NotNull Element element) {
        return render(element.getContainingFile().getTokens(element.getTokenRange()));
    }

    public static @NotNull String render(final @NotNull List<Token> tokens,
                                         final @NotNull SourceRange highlightedRange,
                                         final @Nullable String message) {
        if (tokens.isEmpty()) {
            return "";
        }

        final var lines = render(tokens).split("\n");
        final var highlightedLines = new ArrayList<String>();

        final var firstToken = tokens.get(0);
        final var lineOffset = firstToken.getLine();
        final var columnOffset = firstToken.getCharPositionInLine();

        final var startLine = highlightedRange.startLine() - lineOffset;
        final var endLine = highlightedRange.endLine() - lineOffset;
        final var startColumn = highlightedRange.startColumn() - columnOffset;
        final var endColumn = highlightedRange.endColumn() - (columnOffset - 1);
        final var lineCount = lines.length;

        for (var i = 0; i < lineCount; ++i) {
            final var line = lines[i];
            // If line is not within the range, just add it and skip highlighting
            if (i < startLine || i > endLine) {
                highlightedLines.add(line);
                continue;
            }
            // If this is the first or last highlighted line, take into account column indices
            final var lineLength = line.length();
            final var offset = i == 0 ? startColumn : 0;
            final var length = lineLength == endColumn ? lineLength : lineLength - endColumn;
            highlightedLines.add(line);
            final var highlightBuilder = new StringBuilder(String.format("%s%s",
                " ".repeat(offset),
                "^".repeat(length)));
            if (message != null) {
                highlightBuilder.append(String.format(" %s", message));
            }
            highlightedLines.add(highlightBuilder.toString());
        }

        return String.join("\n", highlightedLines);
    }

    public static @NotNull String render(final @NotNull Element element,
                                         final @NotNull SourceRange highlightedRange,
                                         final @Nullable String message) {
        return render(element.getContainingFile().getTokens(element.getTokenRange()), highlightedRange, message);
    }
}
