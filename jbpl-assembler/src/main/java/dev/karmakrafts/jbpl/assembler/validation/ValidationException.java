package dev.karmakrafts.jbpl.assembler.validation;

import dev.karmakrafts.jbpl.assembler.AssemblerException;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ValidationException extends AssemblerException {
    public ValidationException(@NotNull String message, @Nullable SourceDiagnostic diagnostic) {
        super(message, diagnostic);
    }

    public ValidationException(@NotNull String message,
                               @NotNull Throwable cause,
                               @Nullable SourceDiagnostic diagnostic) {
        super(message, cause, diagnostic);
    }

    public ValidationException(@NotNull Throwable cause, @Nullable SourceDiagnostic diagnostic) {
        super(cause, diagnostic);
    }
}
