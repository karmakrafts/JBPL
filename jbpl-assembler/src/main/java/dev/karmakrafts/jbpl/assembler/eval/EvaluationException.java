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

package dev.karmakrafts.jbpl.assembler.eval;

import dev.karmakrafts.jbpl.assembler.AssemblerException;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.source.SourceRange;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EvaluationException extends AssemblerException {
    public EvaluationException() {
        super();
    }

    public EvaluationException(final @Nullable String message,
                               final @Nullable AssemblyFile file,
                               final @Nullable TokenRange tokenRange,
                               final @Nullable SourceRange highlightedRange) {
        super(message, file, tokenRange, highlightedRange);
    }

    public EvaluationException(final @Nullable String message,
                               final @Nullable AssemblyFile file,
                               final @Nullable TokenRange tokenRange) {
        super(message, file, tokenRange);
    }

    public EvaluationException(final @Nullable String message,
                               final @Nullable Throwable cause,
                               final @Nullable AssemblyFile file,
                               final @Nullable TokenRange tokenRange,
                               final @Nullable SourceRange highlightedRange) {
        super(message, cause, file, tokenRange, highlightedRange);
    }

    public EvaluationException(final @Nullable String message,
                               final @Nullable Throwable cause,
                               final @Nullable AssemblyFile file,
                               final @Nullable TokenRange tokenRange) {
        super(message, cause, file, tokenRange);
    }

    public EvaluationException(final @Nullable Throwable cause,
                               final @Nullable AssemblyFile file,
                               final @Nullable TokenRange tokenRange,
                               final @Nullable SourceRange highlightedRange) {
        super(cause, file, tokenRange, highlightedRange);
    }

    public EvaluationException(final @Nullable Throwable cause,
                               final @Nullable AssemblyFile file,
                               final @Nullable TokenRange tokenRange) {
        super(cause, file, tokenRange);
    }

    public EvaluationException(final @Nullable String message,
                               final @Nullable Throwable cause,
                               final @NotNull Element element,
                               final @Nullable SourceRange highlightedRange) {
        super(message, cause, element, highlightedRange);
    }

    public EvaluationException(final @Nullable String message,
                               final @Nullable Throwable cause,
                               final @NotNull Element element) {
        super(message, cause, element);
    }

    public EvaluationException(final @Nullable String message,
                               final @NotNull Element element,
                               final @Nullable SourceRange highlightedRange) {
        super(message, element, highlightedRange);
    }

    public EvaluationException(final @Nullable String message,
                               final @NotNull Element element,
                               final @NotNull Element highlightedElement) {
        super(message, element, highlightedElement);
    }

    public EvaluationException(final @Nullable String message, final @NotNull Element element) {
        super(message, element);
    }
}
