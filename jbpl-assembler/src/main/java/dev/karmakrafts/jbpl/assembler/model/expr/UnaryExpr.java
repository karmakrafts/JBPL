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
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

public final class UnaryExpr extends AbstractExprContainer implements Expr {
    public static final int VALUE_INDEX = 0;
    public Op op;

    public UnaryExpr(final @NotNull Expr value, final @NotNull Op op) {
        addExpression(value);
        this.op = op;
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return getValue().getType(context);
    }

    private void evaluateAssignment(final @NotNull EvaluationContext context) {

    }

    private @NotNull ConstExpr evaluateForNumber(final @NotNull Number value,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case MINUS, PLUS -> {
                if (value instanceof Byte byteValue) {
                    yield ConstExpr.of(-byteValue, getTokenRange());
                }
                else if (value instanceof Short shortValue) {
                    yield ConstExpr.of(-shortValue, getTokenRange());
                }
                else if (value instanceof Integer integerValue) {
                    yield ConstExpr.of(-integerValue, getTokenRange());
                }
                else if (value instanceof Long longValue) {
                    yield ConstExpr.of(-longValue, getTokenRange());
                }
                else if (value instanceof Float floatValue) {
                    yield ConstExpr.of(-floatValue, getTokenRange());
                }
                else if (value instanceof Double doubleValue) {
                    yield ConstExpr.of(-doubleValue, getTokenRange());
                }
                throw new IllegalStateException(String.format("Unsupported negation expression operand %s %s",
                    op,
                    value));
            }
            case INVERSE -> {
                if (value instanceof Byte byteValue) {
                    yield ConstExpr.of(~byteValue, getTokenRange());
                }
                else if (value instanceof Short shortValue) {
                    yield ConstExpr.of(~shortValue, getTokenRange());
                }
                else if (value instanceof Integer integerValue) {
                    yield ConstExpr.of(~integerValue, getTokenRange());
                }
                else if (value instanceof Long longValue) {
                    yield ConstExpr.of(~longValue, getTokenRange());
                }
                throw new IllegalStateException(String.format("Unsupported inverse expression operand %s", value));
            }
            default -> throw new EvaluationException(String.format("Unary operator %s cannot be applied to number", op),
                SourceDiagnostic.from(this),
                context.createStackTrace());
        };
    }

    private @NotNull ConstExpr evaluateForBool(final boolean value,
                                               final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case NOT -> ConstExpr.of(!value, getTokenRange());
            default ->
                throw new EvaluationException(String.format("Unary operator %s cannot be applied to boolean", op),
                    SourceDiagnostic.from(this),
                    context.createStackTrace());
        };
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        if (op.isAssignment) {
            evaluateAssignment(context);
            return;
        }
        final var value = getValue();
        final var constValue = value.evaluateAs(context, Object.class);
        final var type = value.getType(context);
        if (type == BuiltinType.BOOL) {
            context.pushValue(evaluateForBool((boolean) constValue, context));
            return;
        }
        else if (constValue instanceof Number numberValue) {
            context.pushValue(evaluateForNumber(numberValue, context));
            return;
        }
        throw new EvaluationException(String.format("Unary operator %s cannot be applied to %s", op, value),
            SourceDiagnostic.from(this),
            context.createStackTrace());
    }

    @Override
    public @NotNull UnaryExpr copy() {
        return copyParentAndSourceTo(new UnaryExpr(getValue().copy(), op));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s%s", op, getValue());
    }

    public enum Op {
        // @formatter:off
        PLUS    (false),
        MINUS   (false),
        INVERSE (false),
        NOT     (false),
        PRE_INC (true),
        POST_INC(true),
        PRE_DEC (true),
        POST_DEC(true);
        // @formatter:on

        public final boolean isAssignment;

        Op(final boolean isAssignment) {
            this.isAssignment = isAssignment;
        }
    }
}
