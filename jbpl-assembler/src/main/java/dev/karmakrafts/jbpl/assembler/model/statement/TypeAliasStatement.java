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

package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.lower.IncludeVisibilityProvider;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class TypeAliasStatement extends AbstractExprContainer
    implements Statement, NamedElement, IncludeVisibilityProvider {
    public static final int NAME_INDEX = 0;
    public static final int TYPE_INDEX = 1;

    public boolean isPrivate;

    public TypeAliasStatement(final @NotNull Expr name, final @NotNull Expr type, final boolean isPrivate) {
        addExpression(name);
        addExpression(type);
        this.isPrivate = isPrivate;
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        name.setParent(this);
        getExpressions().set(NAME_INDEX, name);
    }

    public @NotNull Expr getType() {
        return getExpressions().get(TYPE_INDEX);
    }

    public void setType(final @NotNull Expr type) {
        type.setParent(this);
        getExpressions().set(TYPE_INDEX, type);
    }

    public @NotNull Type resolve(final @NotNull EvaluationContext context) throws EvaluationException {
        return getType().evaluateAs(context, Type.class).resolveIfNeeded(context);
    }

    @Override
    public boolean shouldGetIncluded() {
        return !isPrivate;
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return getName().evaluateAs(context, String.class);
    }

    @Override
    public @NotNull TypeAliasStatement copy() {
        return copyParentAndSourceTo(new TypeAliasStatement(getName().copy(), getType().copy(), isPrivate));
    }

    @Override
    public boolean isEvaluatedDirectly() {
        return false; // Typealiases are evaluated indirectly during resolution
    }
}
