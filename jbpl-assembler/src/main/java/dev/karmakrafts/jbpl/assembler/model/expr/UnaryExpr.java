package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
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
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        final var value = getValue().evaluateAsLiteral(context, Object.class);
        return switch (op) {
            case NOT -> {
                if (!(value instanceof Boolean boolValue)) {
                    throw new IllegalStateException("Not operator can only be applied to boolean values");
                }
                yield LiteralExpr.of(!boolValue);
            }
            case MINUS, PLUS -> {
                if (!(value instanceof Number numberValue)) {
                    throw new IllegalStateException("Negation operator can only be applied to numeric values");
                }
                if (numberValue instanceof Byte byteValue) {
                    yield LiteralExpr.of(-byteValue);
                }
                else if (numberValue instanceof Short shortValue) {
                    yield LiteralExpr.of(-shortValue);
                }
                else if (numberValue instanceof Integer integerValue) {
                    yield LiteralExpr.of(-integerValue);
                }
                else if (numberValue instanceof Long longValue) {
                    yield LiteralExpr.of(-longValue);
                }
                else if (numberValue instanceof Float floatValue) {
                    yield LiteralExpr.of(-floatValue);
                }
                else if (numberValue instanceof Double doubleValue) {
                    yield LiteralExpr.of(-doubleValue);
                }
                throw new IllegalStateException(String.format("Unsupported negation expression operand %s %s",
                    op,
                    value));
            }
            case INVERSE -> {
                if (!(value instanceof Number numberValue)) {
                    throw new IllegalStateException("Negation operator can only be applied to numeric values");
                }
                if (numberValue instanceof Byte byteValue) {
                    yield LiteralExpr.of(~byteValue);
                }
                else if (numberValue instanceof Short shortValue) {
                    yield LiteralExpr.of(~shortValue);
                }
                else if (numberValue instanceof Integer integerValue) {
                    yield LiteralExpr.of(~integerValue);
                }
                else if (numberValue instanceof Long longValue) {
                    yield LiteralExpr.of(~longValue);
                }
                throw new IllegalStateException(String.format("Unsupported inverse expression operand %s", value));
            }
        };
    }

    public enum Op {
        PLUS, MINUS, INVERSE, NOT
    }
}
