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

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.MultiMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record SourceDiagnostic( // @formatter:off
    AssemblyFile file,
    TokenRange renderedRange,
    SourceRange highlightedRange
) { // @formatter:on
    private static final int LINE_NUMBER_SPACING = 2;

    public static @NotNull SourceDiagnostic from(final @NotNull Element renderedElement,
                                                 final @NotNull Element highlightedElement) {
        final var file = renderedElement.getContainingFile();
        if (file != highlightedElement.getContainingFile()) {
            throw new IllegalArgumentException("Rendered and highlighted regions must be within same file");
        }
        final var renderedRange = file.getTokenRange();
        final var highlightedRange = file.getSourceRange(highlightedElement.getTokenRange());
        return new SourceDiagnostic(file, renderedRange, highlightedRange);
    }

    public static @NotNull SourceDiagnostic from(final @NotNull Element element) {
        return from(Objects.requireNonNullElse(element.getParent(), element), element);
    }

    private @NotNull List<SourceLine> getRenderedLines() {
        final var tokens = file.getTokens(renderedRange);
        final var mappedTokens = new MultiMap<Integer, Token>();
        for (final var token : tokens) {
            final var lineIndex = token.getLine() - 1;
            mappedTokens.map(lineIndex, token);
        }
        // @formatter:off
        var sortedLines = mappedTokens.entrySet().stream()
            .map(entry -> new SourceLine(entry.getValue(), entry.getKey()))
            .sorted()
            .toList();
        // @formatter:on
        if (highlightedRange != null) {
            final var maxAllowedLines = highlightedRange.getLineCount() + 2; // One additional line before and after
            if (sortedLines.size() > maxAllowedLines) {
                // We need to shorten the list to only include the maximum allowed number of lines
                // TODO: ...
            }
        }
        return sortedLines;
    }

    @Override
    public @NotNull String toString() {
        final var renderedSourceRange = file.getSourceRange(renderedRange);
        if (highlightedRange != null && !renderedSourceRange.contains(highlightedRange)) {
            throw new IllegalStateException("Highlighted source range is outside of element source range");
        }
        final var lines = getRenderedLines();
        final var maxLineNumberLength = lines.stream().mapToInt(SourceLine::getLineNumberLength).max().orElseThrow();
        final var builder = new StringBuilder();
        for (final var line : lines) {
            // Build line number with uniform spacing and add line content
            final var lineNumberSpaceCount = (maxLineNumberLength - line.getLineNumberLength()) + LINE_NUMBER_SPACING;
            builder.append(line.getLineNumber());
            builder.append(" ".repeat(lineNumberSpaceCount));
            builder.append(line);
            // If this line is within the highlighted region, render the highlight below it accordingly
            if (highlightedRange != null && highlightedRange.containsLine(line.lineIndex())) {
                // TODO: ...
            }
        }
        return builder.toString();
    }
}
