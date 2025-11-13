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

package dev.karmakrafts.jbpl.assembler.validation;

import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.model.statement.VersionStatement;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

public final class VersionValidationVisitor implements ElementVisitor {
    public static final VersionValidationVisitor INSTANCE = new VersionValidationVisitor();
    private boolean isVersionSet = false;

    private VersionValidationVisitor() {
    }

    @Override
    public @NotNull Statement visitVersionStatement(final @NotNull VersionStatement versionStatement) {
        if (isVersionSet) {
            throw new RuntimeException(new ValidationException("Cannot set bytecode version more than once per file",
                SourceDiagnostic.from(versionStatement),
                null));
        }
        isVersionSet = true;
        return versionStatement;
    }
}
