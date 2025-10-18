package dev.karmakrafts.jbpl.assembler.model.source;

import org.jetbrains.annotations.NotNull;

public record SourceRange(@NotNull String path, int startLine, int startColumn, int endLine, int endColumn) {
}
