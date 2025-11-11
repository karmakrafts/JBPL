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
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.type.ArrayType;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public final class ForStatement extends AbstractElementContainer implements Statement, ScopeOwner {
    private Expr variableName;
    private Expr value;

    public ForStatement(final @NotNull Expr variableName, final @NotNull Expr value) {
        setVariableName(variableName);
        setValue(value);
    }

    public @NotNull Expr getVariableName() {
        return variableName;
    }

    public void setVariableName(final @NotNull Expr variableName) {
        if (this.variableName != null) {
            this.variableName.setParent(null);
        }
        variableName.setParent(this);
        this.variableName = variableName;
    }

    public @NotNull Expr getValue() {
        return value;
    }

    public void setValue(final @NotNull Expr value) {
        if (this.value != null) {
            this.value.setParent(null);
        }
        value.setParent(this);
        this.value = value;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        // Enhanced for loop
        final var valueType = value.getType(context);
        final var variableName = this.variableName.evaluateAsConst(context, String.class);
        if (valueType instanceof ArrayType) {
            final var array = value.evaluateAsConst(context, Object.class);
            final var arrayLength = Array.getLength(array);
            // TODO: implement continue flag
            for (var i = 0; i < arrayLength; i++) {
                final var value = Array.get(array, i);
                context.pushFrame(this);
                context.peekFrame().injectedValues.put(variableName, LiteralExpr.of(value, this.value.getTokenRange()));
                for (final var element : getElements()) {
                    if (!element.isEvaluatedDirectly()) {
                        continue;
                    }
                    element.evaluate(context);
                    if (context.clearRet()) {
                        break;
                    }
                }
                context.popFrame();
            }
            return;
        }
        throw new EvaluationException(String.format("Cannot use value of type %s in right hand side of for loop",
            valueType), SourceDiagnostic.from(this, value), context.createStackTrace());
    }

    @Override
    public @NotNull ForStatement copy() {
        final var forStatement = copyParentAndSourceTo(new ForStatement(variableName.copy(), value.copy()));
        forStatement.addElements(getElements().stream().map(Element::copy).toList());
        return forStatement;
    }
}
