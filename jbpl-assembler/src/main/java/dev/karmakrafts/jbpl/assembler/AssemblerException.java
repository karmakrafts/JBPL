package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AssemblerException extends Exception {
    public final SourceDiagnostic diagnostic;

    public AssemblerException(final @NotNull String message, final @Nullable SourceDiagnostic diagnostic) {
        super(message);
        this.diagnostic = diagnostic;
    }

    public AssemblerException(final @NotNull String message,
                              final @NotNull Throwable cause,
                              final @Nullable SourceDiagnostic diagnostic) {
        super(message, cause);
        this.diagnostic = diagnostic;
    }

    public AssemblerException(final @NotNull Throwable cause, final @Nullable SourceDiagnostic diagnostic) {
        super(cause);
        this.diagnostic = diagnostic;
    }

    @Override
    public String toString() {
        if (diagnostic != null) {
            return String.format("%s\n\n%s", getMessage(), diagnostic);
        }
        return super.toString();
    }
}
