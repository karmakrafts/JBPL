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
import dev.karmakrafts.jbpl.assembler.model.decl.Declaration;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.model.statement.TypeAliasStatement;
import dev.karmakrafts.jbpl.assembler.scope.ScopeAwareElementVisitor;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

public final class VisibilityValidationVisitor extends ScopeAwareElementVisitor {
    public static final VisibilityValidationVisitor INSTANCE = new VisibilityValidationVisitor();

    private VisibilityValidationVisitor() {
    }

    @Override
    public @NotNull Statement visitDefine(final @NotNull DefineStatement defineStatement) {
        if (!(defineStatement.getParent() instanceof AssemblyFile) && defineStatement.isPrivate) {
            final var message = "Private modifier can only be applied to top-level define";
            throw new RuntimeException(new ValidationException(message,
                SourceDiagnostic.from(defineStatement, message),
                null));
        }
        return super.visitDefine(defineStatement);
    }

    @Override
    public @NotNull Declaration visitMacro(final @NotNull MacroDecl macroDecl) {
        if (!(macroDecl.getParent() instanceof AssemblyFile) && macroDecl.isPrivate) {
            final var message = "Private modifier can only be applied to top-level macro";
            throw new RuntimeException(new ValidationException(message,
                SourceDiagnostic.from(macroDecl, message),
                null));
        }
        return super.visitMacro(macroDecl);
    }

    @Override
    public @NotNull Statement visitTypeAliasStatement(final @NotNull TypeAliasStatement typeAliasStatement) {
        if (!(typeAliasStatement.getParent() instanceof AssemblyFile) && typeAliasStatement.isPrivate) {
            final var message = "Private modifier can only be applied to top-level type alias";
            throw new RuntimeException(new ValidationException(message,
                SourceDiagnostic.from(typeAliasStatement, message),
                null));
        }
        return super.visitTypeAliasStatement(typeAliasStatement);
    }
}
