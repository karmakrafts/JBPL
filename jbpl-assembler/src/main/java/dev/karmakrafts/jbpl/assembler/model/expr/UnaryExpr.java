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
        return getValue().getType(context).resolveIfNeeded(context);
    }

    private @NotNull ConstExpr evaluateAssignmentForByte(final @NotNull Byte value,
                                                         final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case PRE_INC, POST_INC -> ConstExpr.of(value + 1, getTokenRange());
            case PRE_DEC, POST_DEC -> ConstExpr.of(value - 1, getTokenRange());
            default -> {
                final var message = String.format("Unsupported unary re-assignment operator %s for type i8", op);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateAssignmentForShort(final @NotNull Short value,
                                                          final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case PRE_INC, POST_INC -> ConstExpr.of(value + 1, getTokenRange());
            case PRE_DEC, POST_DEC -> ConstExpr.of(value - 1, getTokenRange());
            default -> {
                final var message = String.format("Unsupported unary re-assignment operator %s for type i16", op);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateAssignmentForInt(final @NotNull Integer value,
                                                        final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case PRE_INC, POST_INC -> ConstExpr.of(value + 1, getTokenRange());
            case PRE_DEC, POST_DEC -> ConstExpr.of(value - 1, getTokenRange());
            default -> {
                final var message = String.format("Unsupported unary re-assignment operator %s for type i32", op);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateAssignmentForLong(final @NotNull Long value,
                                                         final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case PRE_INC, POST_INC -> ConstExpr.of(value + 1, getTokenRange());
            case PRE_DEC, POST_DEC -> ConstExpr.of(value - 1, getTokenRange());
            default -> {
                final var message = String.format("Unsupported unary re-assignment operator %s for type i64", op);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateAssignmentForFloat(final @NotNull Float value,
                                                          final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case PRE_INC, POST_INC -> ConstExpr.of(value + 1F, getTokenRange());
            case PRE_DEC, POST_DEC -> ConstExpr.of(value - 1F, getTokenRange());
            default -> {
                final var message = String.format("Unsupported unary re-assignment operator %s for type f32", op);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateAssignmentForDouble(final @NotNull Double value,
                                                           final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (op) {
            case PRE_INC, POST_INC -> ConstExpr.of(value + 1D, getTokenRange());
            case PRE_DEC, POST_DEC -> ConstExpr.of(value - 1D, getTokenRange());
            default -> {
                final var message = String.format("Unsupported unary re-assignment operator %s for type f64", op);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private @NotNull ConstExpr evaluateAssignmentForBuiltinType(final @NotNull Object value,
                                                                final @NotNull BuiltinType builtinType,
                                                                final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (builtinType) {
            case I8 -> evaluateAssignmentForByte((Byte) value, context);
            case I16 -> evaluateAssignmentForShort((Short) value, context);
            case I32 -> evaluateAssignmentForInt((Integer) value, context);
            case I64 -> evaluateAssignmentForLong((Long) value, context);
            case F32 -> evaluateAssignmentForFloat((Float) value, context);
            case F64 -> evaluateAssignmentForDouble((Double) value, context);
            default -> {
                final var message = String.format("Unsupported unary re-assignment operator %s for type %s",
                    op,
                    builtinType);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
        };
    }

    private void evaluateAssignment(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue();
        if (!(value instanceof Reference reference)) {
            final var message = String.format("Cannot perform unary operation %s on value %s", op, value);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        final var oldValue = reference.loadFromReference(context);
        final var type = oldValue.getType(context);
        if (!(type instanceof BuiltinType builtinType)) {
            final var message = String.format("Cannot perform unary operation %s on type %s", op, type);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        final var newValue = evaluateAssignmentForBuiltinType(oldValue.getConstValue(), builtinType, context);
        reference.storeToReference(newValue, context);
        context.pushValue(op.isPostOp ? oldValue : newValue);
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
        PLUS    (false, false),
        MINUS   (false, false),
        INVERSE (false, false),
        NOT     (false, false),
        PRE_INC (true,  false),
        POST_INC(true,  true),
        PRE_DEC (true,  false),
        POST_DEC(true,  true);
        // @formatter:on

        public final boolean isAssignment;
        public final boolean isPostOp;

        Op(final boolean isAssignment, final boolean isPostOp) {
            this.isAssignment = isAssignment;
            this.isPostOp = isPostOp;
        }
    }
}
