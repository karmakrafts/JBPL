/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.eval.StackTrace;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AssemblerException extends Exception {
    public final SourceDiagnostic diagnostic;
    public final StackTrace stackTrace;

    public AssemblerException(final @NotNull String message,
                              final @Nullable SourceDiagnostic diagnostic,
                              final @Nullable StackTrace stackTrace) {
        super(message);
        this.diagnostic = diagnostic;
        this.stackTrace = stackTrace;
    }

    public AssemblerException(final @NotNull String message,
                              final @NotNull Throwable cause,
                              final @Nullable SourceDiagnostic diagnostic,
                              final @Nullable StackTrace stackTrace) {
        super(message, cause);
        this.diagnostic = diagnostic;
        this.stackTrace = stackTrace;
    }

    public AssemblerException(final @NotNull Throwable cause,
                              final @Nullable SourceDiagnostic diagnostic,
                              final @Nullable StackTrace stackTrace) {
        super(cause);
        this.diagnostic = diagnostic;
        this.stackTrace = stackTrace;
    }

    @Override
    public @NotNull String toString() {
        final var builder = new StringBuilder();
        builder.append(getMessage());
        builder.append("\n\n");
        if (diagnostic != null) {
            builder.append(diagnostic);
        }
        if (stackTrace != null) {
            builder.append('\n');
            builder.append("────────────────────↯ STACKTRACE ↯────────────────────\n");
            builder.append(stackTrace);
            builder.append("\n\n");
        }
        return builder.toString();
    }
}
