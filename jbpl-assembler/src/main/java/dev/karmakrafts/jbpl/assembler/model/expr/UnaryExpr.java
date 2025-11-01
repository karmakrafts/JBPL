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

    private @NotNull LiteralExpr evaluateForNumber(final @NotNull Number value) throws EvaluationException {
        return switch (op) {
            case MINUS, PLUS -> {
                if (value instanceof Byte byteValue) {
                    yield LiteralExpr.of(-byteValue);
                }
                else if (value instanceof Short shortValue) {
                    yield LiteralExpr.of(-shortValue);
                }
                else if (value instanceof Integer integerValue) {
                    yield LiteralExpr.of(-integerValue);
                }
                else if (value instanceof Long longValue) {
                    yield LiteralExpr.of(-longValue);
                }
                else if (value instanceof Float floatValue) {
                    yield LiteralExpr.of(-floatValue);
                }
                else if (value instanceof Double doubleValue) {
                    yield LiteralExpr.of(-doubleValue);
                }
                throw new IllegalStateException(String.format("Unsupported negation expression operand %s %s",
                    op,
                    value));
            }
            case INVERSE -> {
                if (value instanceof Byte byteValue) {
                    yield LiteralExpr.of(~byteValue);
                }
                else if (value instanceof Short shortValue) {
                    yield LiteralExpr.of(~shortValue);
                }
                else if (value instanceof Integer integerValue) {
                    yield LiteralExpr.of(~integerValue);
                }
                else if (value instanceof Long longValue) {
                    yield LiteralExpr.of(~longValue);
                }
                throw new IllegalStateException(String.format("Unsupported inverse expression operand %s", value));
            }
            default -> throw new EvaluationException(String.format("Unary operator %s cannot be applied to number", op),
                SourceDiagnostic.from(this));
        };
    }

    private @NotNull LiteralExpr evaluateForBool(final boolean value) throws EvaluationException {
        return switch (op) {
            case NOT -> LiteralExpr.of(!value);
            default ->
                throw new EvaluationException(String.format("Unary operator %s cannot be applied to boolean", op),
                    SourceDiagnostic.from(this));
        };
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue();
        final var constValue = value.evaluateAsConst(context, Object.class);
        final var type = value.getType(context);
        if (type == BuiltinType.BOOL) {
            context.pushValue(evaluateForBool((boolean) constValue));
            return;
        }
        else if (constValue instanceof Number numberValue) {
            context.pushValue(evaluateForNumber(numberValue));
            return;
        }
        throw new EvaluationException(String.format("Unary operator %s cannot be applied to %s", op, value),
            SourceDiagnostic.from(this));
    }

    @Override
    public @NotNull UnaryExpr copy() {
        return copyParentAndSourceTo(new UnaryExpr(getValue().copy(), op));
    }

    public enum Op {
        PLUS, MINUS, INVERSE, NOT
    }
}
