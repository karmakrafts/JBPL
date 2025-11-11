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

import dev.karmakrafts.jbpl.assembler.model.decl.Declaration;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.statement.ReturnStatement;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.scope.Scope;
import dev.karmakrafts.jbpl.assembler.scope.ScopeAwareElementVisitor;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class ReturnValidationVisitor extends ScopeAwareElementVisitor {
    public static final ReturnValidationVisitor INSTANCE = new ReturnValidationVisitor();

    private ReturnValidationVisitor() {
    }

    private static final class Checker extends ScopeAwareElementVisitor {
        private final HashSet<Scope> scopes = new HashSet<>();

        @Override
        public @NotNull Statement visitStatement(final @NotNull Statement statement) {
            if (scopes.contains(getScope())) {
                throw new RuntimeException(new ValidationException("Statement can never be reached",
                    SourceDiagnostic.from(statement),
                    null));
            }
            return super.visitStatement(statement);
        }

        @Override
        public @NotNull Statement visitReturnStatement(final @NotNull ReturnStatement returnStatement) {
            scopes.add(getScope());
            return returnStatement;
        }
    }

    @Override
    public @NotNull Declaration visitMacro(final @NotNull MacroDecl macroDecl) {
        final var checker = new Checker();
        checker.restoreFrom(scopeStack);
        macroDecl.acceptChildren(checker);
        return super.visitMacro(macroDecl);
    }
}
