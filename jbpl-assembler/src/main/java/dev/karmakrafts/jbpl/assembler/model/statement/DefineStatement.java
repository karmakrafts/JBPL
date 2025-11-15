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
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

public final class DefineStatement extends AbstractExprContainer
    implements Statement, NamedElement, IncludeVisibilityProvider {
    public static final int NAME_INDEX = 0;
    public static final int TYPE_INDEX = 1;
    public static final int VALUE_INDEX = 2;
    public static final int INITIAL_VALUE = 3;

    public boolean isFinal;
    public boolean isPrivate;

    public DefineStatement(final @NotNull Expr name,
                           final @NotNull Expr type,
                           final @NotNull Expr value,
                           final boolean isFinal,
                           final boolean isPrivate) {
        addExpression(name);
        addExpression(type);
        addExpression(value);
        addExpression(value);
        this.isFinal = isFinal;
        this.isPrivate = isPrivate;
    }

    public void resetValue() {
        setValue(getInitialValue());
    }

    public @NotNull Expr getInitialValue() {
        return getExpressions().get(INITIAL_VALUE);
    }

    public void setInitialValue(final @NotNull Expr initialValue) {
        initialValue.setParent(this);
        getExpressions().set(INITIAL_VALUE, initialValue);
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

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        value.setParent(this);
        getExpressions().set(VALUE_INDEX, value);
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
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue();
        final var type = getType().evaluateAs(context, Type.class).resolveIfNeeded(context);
        final var valueType = value.getType(context).resolveIfNeeded(context);
        if (!type.isAssignableFrom(valueType, context)) {
            final var message = String.format("Cannot assign value of type %s to define of type %s", valueType, type);
            final var diagnostic = SourceDiagnostic.from(this, value, message);
            throw new EvaluationException("Incompatible define value type", diagnostic, context.createStackTrace());
        }
        context.pushValue(type.cast(value.evaluateAsConst(context), context));
    }

    @Override
    public @NotNull DefineStatement copy() {
        return copyParentAndSourceTo(new DefineStatement(getName().copy(),
            getType().copy(),
            getValue().copy(),
            isFinal,
            isPrivate));
    }

    @Override
    public boolean isEvaluatedDirectly() {
        return false;
    }

    @Override
    public @NotNull String toString() {
        if (isFinal) {
            return String.format("final define %s: %s = %s", getName(), getType(), getValue());
        }
        return String.format("define %s: %s = %s", getName(), getType(), getValue());
    }
}
