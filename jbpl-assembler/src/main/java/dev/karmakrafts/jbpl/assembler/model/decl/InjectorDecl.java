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

package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import org.jetbrains.annotations.NotNull;

public final class InjectorDecl extends AbstractStatementContainer implements Declaration, ScopeOwner {
    private Expr target;
    private Expr selector;

    public @NotNull Expr getSelector() {
        return selector;
    }

    public void setSelector(final @NotNull Expr selector) {
        if (this.selector != null) {
            this.selector.setParent(null);
        }
        selector.setParent(this);
        this.selector = selector;
    }

    public @NotNull Expr getTarget() {
        return target;
    }

    public void setTarget(final @NotNull Expr target) {
        if (this.target != null) {
            this.target.setParent(null);
        }
        target.setParent(this);
        this.target = target;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {
        // TODO: implement this
    }

    @Override
    public @NotNull InjectorDecl copy() {
        final var injector = copyParentAndSourceTo(new InjectorDecl());
        injector.setTarget(getTarget().copy());
        injector.setSelector(getSelector().copy());
        injector.addStatements(getStatements().stream().map(Statement::copy).toList());
        return injector;
    }
}
