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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SourceDiagnostic {
    private static final int LINE_NUMBER_SPACING = 1;
    private static final String V_BORDER_CHAR = "│";
    private static final String H_BORDER_CHAR = "─";
    private static final String UNDERLINE_CHAR = "━";
    private static final String UNDERLINE_CONN_CHAR = "┯";
    private static final String CORNER_CHAR = "└";

    public final AssemblyFile file;
    public final TokenRange renderedRange;
    public final SourceRange renderedSourceRange;
    public final ArrayList<Highlight> highlights;
    public final SourceRange highlightBounds;

    public SourceDiagnostic(final @NotNull AssemblyFile file,
                            final @NotNull TokenRange renderedRange,
                            final @NotNull List<Highlight> highlights) {
        this.file = file;
        this.renderedRange = renderedRange;
        renderedSourceRange = file.getSourceRange(renderedRange);
        this.highlights = new ArrayList<>(highlights);
        highlightBounds = SourceRange.union(highlights.stream().map(Highlight::range).toList());
    }

    public static @NotNull SourceDiagnostic from(final @NotNull AssemblyFile file,
                                                 final @NotNull Token token,
                                                 final @Nullable String message) {
        final var tokenRange = TokenRange.fromToken(token);
        final var line = token.getLine();
        // @formatter:off
        final var lineTokenIndices = file.getTokens().stream()
            .filter(t -> t.getLine() == line)
            .toList();
        final var lineStartToken = lineTokenIndices.isEmpty()
            ? token
            : lineTokenIndices.get(0);
        final var lineEndToken = lineTokenIndices.isEmpty()
            ? token
            : lineTokenIndices.get(lineTokenIndices.size() - 1);
        // @formatter:on
        final var lineTokenRange = TokenRange.fromTokens(lineStartToken, lineEndToken);
        final var highlight = new Highlight(file.getSourceRange(tokenRange), message);
        return new SourceDiagnostic(file, lineTokenRange, List.of(highlight));
    }

    public static @NotNull SourceDiagnostic from(final @NotNull AssemblyFile file, final @NotNull Token token) {
        return from(file, token, null);
    }

    public static @NotNull SourceDiagnostic from(final @NotNull Element renderedElement,
                                                 final @NotNull Element highlightedElement,
                                                 final @Nullable String message) {
        final var file = renderedElement.getContainingFile();
        if (file != highlightedElement.getContainingFile()) {
            throw new IllegalArgumentException("Rendered and highlighted regions must be within same file");
        }
        final var renderedRange = file.getTokenRange();
        final var highlightedRange = file.getSourceRange(highlightedElement.getTokenRange());
        final var highlight = new Highlight(highlightedRange, message);
        return new SourceDiagnostic(file, renderedRange, List.of(highlight));
    }

    public static @NotNull SourceDiagnostic from(final @NotNull Element renderedElement,
                                                 final @NotNull Element highlightedElement) {
        return from(renderedElement, highlightedElement, null);
    }

    public static @NotNull SourceDiagnostic from(final @NotNull Element element, final @Nullable String message) {
        return from(Objects.requireNonNullElse(element.getParent(), element), element, message);
    }

    public static @NotNull SourceDiagnostic from(final @NotNull Element element) {
        return from(element, (String) null);
    }

    public void addHighlight(final @NotNull Highlight highlight) {
        highlights.add(highlight);
    }

    public void removeHighlight(final @NotNull Highlight highlight) {
        highlights.remove(highlight);
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
            .collect(Collectors.toCollection(ArrayList::new));
        // @formatter:on
        // We only want to keep lines that contain a highlight
        // TODO: reimplement additional context lines around the highlighted ones?
        if (!highlights.isEmpty()) {
            final var allLines = new ArrayList<>(sortedLines);
            sortedLines.clear();
            for (final var line : allLines) {
                final var lineIndex = line.lineIndex();
                if (highlights.stream().noneMatch(highlight -> highlight.range.containsLine(lineIndex))) {
                    continue;
                }
                sortedLines.add(line);
            }
        }
        return sortedLines;
    }

    private void renderLineHighlight(final @NotNull StringBuilder builder,
                                     final @NotNull SourceLine line,
                                     final @NotNull Highlight highlight,
                                     final int spacing) {
        final var lineIndex = line.lineIndex();
        final var range = highlight.range;
        if (!range.containsLine(lineIndex)) {
            return;
        }
        builder.append(" ".repeat(spacing));
        if (range.getLineCount() == 1) {
            // If we are rendering a single line highlight, start and end column are on same line
            final var start = range.startColumn();
            final var highlightLength = range.endColumn() - start;
            final var lineLength = line.getLength();

            builder.append(" ".repeat(start));
            builder.append(UNDERLINE_CHAR.repeat(highlightLength));

            final var message = highlight.message;
            if (message != null) {
                builder.append(UNDERLINE_CONN_CHAR);
                final var connOffset = spacing + start + highlightLength;
                builder.append('\n');
                builder.append(" ".repeat(connOffset));
                builder.append(CORNER_CHAR);
                builder.append(H_BORDER_CHAR.repeat(lineLength - (start + highlightLength + 1) - 1));
                builder.append(' ');
                builder.append(message);
                return;
            }

            builder.append(UNDERLINE_CHAR);
            return;
        }
        // For multi-line highlights, we need to handle start-, end- and intermediate lines
        if (range.isFirstLine(lineIndex)) {
            // We render from startColumn to end
            final var start = range.startColumn();
            final var lineLength = line.getLength();
            final var highlightLength = lineLength - start;

            builder.append(" ".repeat(start));
            builder.append(UNDERLINE_CHAR.repeat(highlightLength + 1));
        }
        else if (range.isLastLine(lineIndex)) {
            // We render from 0 to endColumn and the message
        }
        else {
            // We render the entire line
        }
    }

    @Override
    public @NotNull String toString() {
        final var renderedSourceRange = file.getSourceRange(renderedRange);
        if (!renderedSourceRange.contains(highlightBounds)) {
            throw new IllegalStateException("Highlighted source range is outside of element source range");
        }
        final var lines = getRenderedLines();
        final var maxLineNumberLength = lines.stream().mapToInt(SourceLine::getLineNumberLength).max().orElseThrow();
        final var highlightSpacing = maxLineNumberLength + LINE_NUMBER_SPACING + 2;
        final var builder = new StringBuilder();
        for (var i = 0; i < highlights.size(); i++) {
            final var highlight = highlights.get(i);
            for (final var line : lines) {
                // Build line number with uniform spacing and add line content
                final var lineNumberSpaceCount = (maxLineNumberLength - line.getLineNumberLength()) + LINE_NUMBER_SPACING;
                builder.append(line.getLineNumber());
                builder.append(" ".repeat(lineNumberSpaceCount));
                builder.append(V_BORDER_CHAR).append(' ');
                builder.append(line.renderWithColor());
                // Render all highlights for this line
                renderLineHighlight(builder, line, highlight, highlightSpacing);
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    // @formatter:off
    public record Highlight(
        @NotNull SourceRange range,
        @Nullable String message
    ) {}
}
