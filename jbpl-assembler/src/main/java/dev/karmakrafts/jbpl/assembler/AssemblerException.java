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
            final var file = diagnostic.file();
            final var renderedRange = file.getSourceRange(diagnostic.renderedRange());
            var highlightedRange = diagnostic.highlightedRange();
            if (highlightedRange == null) {
                highlightedRange = renderedRange;
            }
            final var path = file.path;
            final var line = highlightedRange.startLine() + 1;
            final var column = highlightedRange.startColumn() + 1;
            final var error = String.format("Error while assembling %s:%s:%s: ", path, line, column);
            var text = error + "\n\n" + diagnostic.render(getMessage());
            if (!text.endsWith("\n")) {
                text += '\n';
            }
            return text;
        }
        return super.toString();
    }
}
