package dev.karmakrafts.jbpl.assembler.validation;

import dev.karmakrafts.jbpl.assembler.AssemblerException;
import dev.karmakrafts.jbpl.assembler.eval.StackTrace;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ValidationException extends AssemblerException {
    public ValidationException(final @NotNull String message,
                               final @Nullable SourceDiagnostic diagnostic,
                               final @Nullable StackTrace stackTrace) {
        super(message, diagnostic, stackTrace);
    }

    public ValidationException(final @NotNull String message,
                               final @NotNull Throwable cause,
                               final @Nullable SourceDiagnostic diagnostic,
                               final @Nullable StackTrace stackTrace) {
        super(message, cause, diagnostic, stackTrace);
    }

    public ValidationException(final @NotNull Throwable cause,
                               final @Nullable SourceDiagnostic diagnostic,
                               final @Nullable StackTrace stackTrace) {
        super(cause, diagnostic, stackTrace);
    }
}
