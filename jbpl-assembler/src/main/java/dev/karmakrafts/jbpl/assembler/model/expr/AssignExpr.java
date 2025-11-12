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

package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public final class AssignExpr extends AbstractExprContainer implements Expr {
    public static final int REFERENCE_INDEX = 0;
    public static final int VALUE_INDEX = 1;

    public AssignExpr(final @NotNull Expr reference, final @NotNull Expr value) {
        addExpression(reference);
        addExpression(value);
    }

    public @NotNull Expr getReference() {
        return getExpressions().get(REFERENCE_INDEX);
    }

    public void setReference(final @NotNull Expr reference) {
        reference.setParent(this);
        getExpressions().set(REFERENCE_INDEX, reference);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        value.setParent(this);
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return getReference().getType(context); // We always rely on reference type, not value
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var reference = getReference();
        final var expectedType = getType(context);
        final var value = getValue();
        final var valueType = value.getType(context);
        if (!expectedType.isAssignableFrom(valueType)) {
            final var message = String.format("Cannot assign value of type %s to %s", valueType, expectedType);
            throw new EvaluationException(message,
                SourceDiagnostic.from(this, value, message),
                context.createStackTrace());
        }
        if (reference instanceof ReferenceExpr refExpr) {
            // In this case, the only valid option is that we are re-assigning a define, since params are always immutable
            if (refExpr.findArgument(context) != null) {
                final var message = String.format("Cannot re-assign parameter '%s'", refExpr.name);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, reference, message),
                    context.createStackTrace());
            }
            final var define = refExpr.getDefine(context);
            if (define.isFinal) {
                final var message = String.format("Cannot re-assign final define '%s'", refExpr.name);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, reference, message),
                    context.createStackTrace());
            }
            final var constValue = value.evaluateAsConst(context);
            define.setValue(constValue);
            context.pushValue(constValue); // Assignments evaluate to their assigned value
            return;
        }
        else if (reference instanceof ArrayAccessExpr arrayAccessExpr) {
            // In this case, we are storing into the array. Reference evaluates to an array ref, against what the function name suggests.
            final var arrayRef = arrayAccessExpr.getReference().evaluateAs(context, Object.class);
            final var arrayIndex = arrayAccessExpr.getIndex().evaluateAs(context, Integer.class);
            Array.set(arrayRef, arrayIndex, value.evaluateAs(context, Object.class));
            context.pushValue(value.evaluateAsConst(context));
            return;
        }
        final var message = String.format("Cannot re-assign expression %s", reference);
        throw new EvaluationException(message,
            SourceDiagnostic.from(this, reference, message),
            context.createStackTrace());
    }

    @Override
    public @NotNull AssignExpr copy() {
        return copyParentAndSourceTo(new AssignExpr(getReference().copy(), getValue().copy()));
    }
}
