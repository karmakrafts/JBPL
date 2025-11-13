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
import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

public final class YeetStatement extends AbstractExprContainer implements Statement {
    public static final int TARGET_INDEX = 0;

    public YeetStatement(final @NotNull Expr target) {
        addExpression(target);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var target = getTarget();
        if (target instanceof LiteralExpr literalExpr) {
            var type = (ClassType) literalExpr.value;
            context.removeClass(type.name());
        }
        else if (target instanceof FunctionSignatureExpr functionSignatureExpr) {
            final var result = functionSignatureExpr.evaluateAs(context, FunctionSignatureExpr.class);
            final var name = result.getFunctionName().evaluateAs(context, String.class);
            final var owner = result.getFunctionOwner().evaluateAs(context, ClassType.class);
            final var returnType = result.getFunctionReturnType().evaluateAs(context, Type.class);
            // @formatter:off
            final var paramTypes = result.getFunctionParameters().stream()
                .map(ExceptionUtils.unsafeFunction(type -> type.evaluateAs(context, Type.class)))
                .toArray(Type[]::new);
            // @formatter:on
            context.removeFunction(owner.name(), name, returnType, paramTypes);
        }
        else if (target instanceof FieldSignatureExpr fieldSignatureExpr) {
            final var name = fieldSignatureExpr.getFieldName().evaluateAs(context, String.class);
            final var owner = fieldSignatureExpr.getFieldOwner().evaluateAs(context, ClassType.class);
            context.removeField(owner.name(), name);
        }
        else {
            throw new EvaluationException(String.format("Unsupported target type for yeet: %s", target),
                SourceDiagnostic.from(this),
                context.createStackTrace());
        }
    }

    public @NotNull Expr getTarget() {
        return getExpressions().get(TARGET_INDEX);
    }

    public void setTarget(final @NotNull Expr name) {
        getExpressions().set(TARGET_INDEX, name);
    }

    @Override
    public @NotNull YeetStatement copy() {
        return copyParentAndSourceTo(new YeetStatement(getTarget().copy()));
    }
}
