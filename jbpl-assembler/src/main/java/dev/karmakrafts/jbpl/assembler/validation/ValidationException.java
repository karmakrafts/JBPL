package dev.karmakrafts.jbpl.assembler.validation;

import dev.karmakrafts.jbpl.assembler.AssemblerException;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.source.SourceRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ValidationException extends AssemblerException {
    public ValidationException() {
        super();
    }

    public ValidationException(final @Nullable String message,
                               final @Nullable AssemblyFile file,
                               final @Nullable SourceRange sourceRange) {
        super(message, file, sourceRange);
    }

    public ValidationException(final @Nullable String message,
                               final @Nullable Throwable cause,
                               final @Nullable AssemblyFile file,
                               final @Nullable SourceRange sourceRange) {
        super(message, cause, file, sourceRange);
    }

    public ValidationException(final @Nullable Throwable cause,
                               final @Nullable AssemblyFile file,
                               final @Nullable SourceRange sourceRange) {
        super(cause, file, sourceRange);
    }

    public ValidationException(final @Nullable String message,
                               final @Nullable Throwable cause,
                               final @NotNull Element element) {
        super(message, cause, element);
    }

    public ValidationException(final @Nullable String message, final @NotNull Element element) {
        super(message, element);
    }
}
