package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.source.SourceRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AssemblerException extends Exception {
    public final AssemblyFile file;
    public final SourceRange sourceRange;

    public AssemblerException() {
        super();
        file = null;
        sourceRange = null;
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable AssemblyFile file,
                              final @Nullable SourceRange sourceRange) {
        super(message);
        this.file = file;
        this.sourceRange = sourceRange;
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable Throwable cause,
                              final @Nullable AssemblyFile file,
                              final @Nullable SourceRange sourceRange) {
        super(message, cause);
        this.file = file;
        this.sourceRange = sourceRange;
    }

    public AssemblerException(final @Nullable Throwable cause,
                              final @Nullable AssemblyFile file,
                              final @Nullable SourceRange sourceRange) {
        super(cause);
        this.file = file;
        this.sourceRange = sourceRange;
    }

    public AssemblerException(final @Nullable String message,
                              final @Nullable Throwable cause,
                              final @NotNull Element element) {
        this(message, cause, element.getContainingFile(), element.getSourceRange());
    }

    public AssemblerException(final @Nullable String message, final @NotNull Element element) {
        this(message, null, element.getContainingFile(), element.getSourceRange());
    }
}
