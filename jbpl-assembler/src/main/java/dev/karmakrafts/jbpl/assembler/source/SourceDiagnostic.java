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
import dev.karmakrafts.jbpl.assembler.util.MathUtils;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                final var maxLineIndex = sortedLines.size();
                final var start = MathUtils.clamp(0, maxLineIndex, highlightedRange.startLine() - 1);
                final var end = MathUtils.clamp(0, maxLineIndex, highlightedRange.endLine() + 2);
                sortedLines = sortedLines.subList(start, end);
            }
        }
        return sortedLines;
    }

    public @NotNull String render(final @Nullable String message) {
        final var renderedSourceRange = file.getSourceRange(renderedRange);
        if (highlightedRange != null && !renderedSourceRange.contains(highlightedRange)) {
            throw new IllegalStateException("Highlighted source range is outside of element source range");
        }
        final var lines = getRenderedLines();
        final var maxLineNumberLength = lines.stream().mapToInt(SourceLine::getLineNumberLength).max().orElseThrow();
        final var highlightSpacing = maxLineNumberLength + LINE_NUMBER_SPACING;
        final var builder = new StringBuilder();
        for (final var line : lines) {
            // Build line number with uniform spacing and add line content
            final var lineNumberSpaceCount = (maxLineNumberLength - line.getLineNumberLength()) + LINE_NUMBER_SPACING;
            builder.append(line.getLineNumber());
            builder.append(" ".repeat(lineNumberSpaceCount));
            builder.append(line);
            // If this line is within the highlighted region, render the highlight below it accordingly
            final var lineIndex = line.lineIndex();
            if (highlightedRange != null && highlightedRange.containsLine(lineIndex)) {
                final var lineLength = line.getLength();
                var highlightOffset = 0;
                var highlightLength = 0;
                if (highlightedRange.isSingleLine()) {
                    // If we only highlight a single line, take into account start- and end column
                    highlightOffset = highlightedRange.startColumn();
                    highlightLength = (highlightedRange.endColumn() - highlightOffset) + 1;
                }
                else {
                    // Otherwise we are dealing with a multi-line highlight
                    if (highlightedRange.isFirstLine(lineIndex)) {
                        // In the first line, we highlight starting at column til the end of the line
                        highlightOffset = highlightedRange.startColumn();
                        highlightLength = (lineLength - highlightOffset) + 1;
                    }
                    else if (highlightedRange.isLastLine(lineIndex)) {
                        // In the last line, we highlight from line start to end column
                        highlightLength = highlightedRange.endColumn();
                    }
                    else {
                        // For any line in between, we highlight the entire line
                        highlightLength = lineLength;
                    }
                }
                if (highlightLength == 0) {
                    continue; // If the line length still comes out as 0 chars, we can skip the highlight entirely
                }
                if (!line.endsWithNewline()) {
                    // If the previous source line doesn't end in a \n (last line for example), we need to add one first
                    builder.append('\n');
                }
                builder.append(" ".repeat(highlightSpacing + highlightOffset));
                builder.append("^".repeat(highlightLength));
                if (message != null) {
                    // If a message is specified, render it next to the highlight
                    builder.append(' ');
                    builder.append(message);
                }
                builder.append('\n'); // Append an additional newline since we're not getting it from the SourceLine here
            }
        }
        return builder.toString();
    }
}
