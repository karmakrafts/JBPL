package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.source.SourceRange;
import dev.karmakrafts.jbpl.assembler.source.SourceRenderer;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AssemblerException extends Exception {
    public final AssemblyFile file;
    public final TokenRange tokenRange;
    public final SourceRange highlightedRange;

    public AssemblerException() {
        super();
        file = null;
        tokenRange = null;
        highlightedRange = null;
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable AssemblyFile file,
                              final @Nullable TokenRange tokenRange,
                              final @Nullable SourceRange highlightedRange) {
        super(message);
        this.file = file;
        this.tokenRange = tokenRange;
        this.highlightedRange = highlightedRange;
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable AssemblyFile file,
                              final @Nullable TokenRange tokenRange) {
        this(message, file, tokenRange, getHighlightedRange(file, tokenRange));
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable Throwable cause,
                              final @Nullable AssemblyFile file,
                              final @Nullable TokenRange tokenRange,
                              final @Nullable SourceRange highlightedRange) {
        super(message, cause);
        this.file = file;
        this.tokenRange = tokenRange;
        this.highlightedRange = highlightedRange;
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable Throwable cause,
                              final @Nullable AssemblyFile file,
                              final @Nullable TokenRange tokenRange) {
        this(message, cause, file, tokenRange, getHighlightedRange(file, tokenRange));
    }

    public AssemblerException(final @Nullable Throwable cause,
                              final @Nullable AssemblyFile file,
                              final @Nullable TokenRange tokenRange,
                              final @Nullable SourceRange highlightedRange) {
        super(cause);
        this.file = file;
        this.tokenRange = tokenRange;
        this.highlightedRange = highlightedRange;
    }

    public AssemblerException(final @Nullable Throwable cause,
                              final @Nullable AssemblyFile file,
                              final @Nullable TokenRange tokenRange) {
        this(cause, file, tokenRange, getHighlightedRange(file, tokenRange));
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable Throwable cause,
                              final @NotNull Element element,
                              final @Nullable SourceRange highlightedRange) {
        this(message, cause, element.getContainingFile(), element.getTokenRange(), highlightedRange);
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable Throwable cause,
                              final @NotNull Element element) {
        this(message, cause, element, getHighlightedRange(element.getContainingFile(), element.getTokenRange()));
    }

    public AssemblerException(final @Nullable String message,
                              final @NotNull Element element,
                              final @Nullable SourceRange highlightedRange) {
        this(message, null, element.getContainingFile(), element.getTokenRange(), highlightedRange);
    }

    public AssemblerException(final @Nullable String message,
                              final @NotNull Element element,
                              final @NotNull Element highlightedElement) {
        this(message,
            element,
            getHighlightedRange(highlightedElement.getContainingFile(), highlightedElement.getTokenRange()));
    }

    public AssemblerException(final @Nullable String message, final @NotNull Element element) {
        this(message, element, getHighlightedRange(element.getContainingFile(), element.getTokenRange()));
    }

    protected static @Nullable SourceRange getHighlightedRange(final @Nullable AssemblyFile file,
                                                               final @Nullable TokenRange tokenRange) {
        if (file == null || tokenRange == null) {
            return null;
        }
        return file.getSourceRange(tokenRange);
    }

    public @NotNull String getHeaderMessage() {
        return "Error while assembling at";
    }

    public @NotNull String getLocation() {
        if (file == null || tokenRange == null) {
            return "unknown"; // TODO: find a better solution for this
        }
        final var path = file.path;
        if (highlightedRange != null) {
            final var line = highlightedRange.startLine();
            final var column = highlightedRange.endColumn() + 1;
            return String.format("%s:%s:%s", path, line, column);
        }
        final var sourceRange = file.getSourceRange(tokenRange);
        final var line = sourceRange.startLine();
        final var column = sourceRange.startColumn() + 1;
        return String.format("%s:%s:%s", path, line, column);
    }

    @Override
    public String toString() {
        if (file != null && tokenRange != null) {
            final var builder = new StringBuilder();
            final var location = getLocation();
            builder.append(String.format("%s %s", getHeaderMessage(), location));
            builder.append("\n\n");
            final var tokens = file.getTokens(tokenRange);
            if (highlightedRange != null) {
                builder.append(SourceRenderer.render(tokens, highlightedRange, getMessage()));
            }
            else {
                builder.append(SourceRenderer.render(tokens));
            }
            builder.append("\n");
            return builder.toString();
        }
        return getMessage();
    }
}
