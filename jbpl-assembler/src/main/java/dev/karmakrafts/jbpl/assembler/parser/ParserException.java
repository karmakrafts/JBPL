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

package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.AssemblerException;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.source.SourceRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParserException extends AssemblerException {
    public ParserException() {
        super();
    }

    public ParserException(final @Nullable String message,
                           final @Nullable AssemblyFile file,
                           final @Nullable SourceRange sourceRange) {
        super(message, file, sourceRange);
    }

    public ParserException(final @Nullable String message,
                           final @Nullable Throwable cause,
                           final @Nullable AssemblyFile file,
                           final @Nullable SourceRange sourceRange) {
        super(message, cause, file, sourceRange);
    }

    public ParserException(final @Nullable Throwable cause,
                           final @Nullable AssemblyFile file,
                           final @Nullable SourceRange sourceRange) {
        super(cause, file, sourceRange);
    }

    public ParserException(final @Nullable String message, final @Nullable Throwable cause, @NotNull Element element) {
        super(message, cause, element);
    }

    public ParserException(final @Nullable String message, @NotNull Element element) {
        super(message, element);
    }
}
