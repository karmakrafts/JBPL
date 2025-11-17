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

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.statement.IncludeStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.scope.ScopeAwareElementVisitor;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

public final class IncludeValidationVisitor extends ScopeAwareElementVisitor {
    public static final IncludeValidationVisitor INSTANCE = new IncludeValidationVisitor();

    @Override
    public @NotNull Statement visitInclude(final @NotNull IncludeStatement includeStatement) {
        if (!(includeStatement.getContainingScope() instanceof AssemblyFile)) {
            final var message = "Include statement must appear at the top level only";
            throw new RuntimeException(new ValidationException(message,
                SourceDiagnostic.from(includeStatement, message),
                null));
        }
        return super.visitInclude(includeStatement);
    }
}
