package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class TypeOfExpr extends AbstractExprContainer implements Expr {
    public static final int VALUE_INDEX = 0;

    public TypeOfExpr(final @NotNull Expr value) {
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return PreproType.TYPE;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue();
        final var type = value.getType(context);
        if (type == PreproType.TYPE) {
            // If the passed in expression already is a type literal, just forward it
            context.pushValue(value);
            return;
        }
        context.pushValue(LiteralExpr.of(type));
    }

    @Override
    public @NotNull TypeOfExpr copy() {
        return copyParentAndSourceTo(new TypeOfExpr(getValue().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("typeof(%s)", getValue());
    }
}
