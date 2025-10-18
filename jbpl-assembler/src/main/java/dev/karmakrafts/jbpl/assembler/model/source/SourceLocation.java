package dev.karmakrafts.jbpl.assembler.model.source;

import org.jetbrains.annotations.NotNull;

public record SourceLocation(@NotNull String path, int line, int column) {

}
