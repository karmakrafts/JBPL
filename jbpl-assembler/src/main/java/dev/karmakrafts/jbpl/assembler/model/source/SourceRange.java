package dev.karmakrafts.jbpl.assembler.model.source;

import org.jetbrains.annotations.NotNull;

public record SourceRange(@NotNull String path, int startLine, int startColumn, int endLine, int endColumn) {
    public static @NotNull SourceRange of(final @NotNull SourceLocation location) {
        return of(location, location);
    }

    public static @NotNull SourceRange of(final @NotNull SourceLocation start, final @NotNull SourceLocation end) {
        return new SourceRange(start.path(), start.line(), start.column(), end.line(), end.column());
    }
}
