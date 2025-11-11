package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class IsExpr extends AbstractExprContainer implements Expr {
    public static final int VALUE_INDEX = 0;
    public static final int TYPE_INDEX = 1;

    public IsExpr(final @NotNull Expr value, final @NotNull Expr type) {
        addExpression(value);
        addExpression(type);
    }

    public @NotNull Expr getType() {
        return getExpressions().get(TYPE_INDEX);
    }

    public void setType(final @NotNull Expr type) {
        getType().setParent(null);
        type.setParent(this);
        getExpressions().set(TYPE_INDEX, type);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getValue().setParent(null);
        value.setParent(this);
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return BuiltinType.BOOL;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var type = getType().evaluateAsConst(context, Type.class);
        context.pushValue(LiteralExpr.of(getValue().getType(context).equals(type)));
    }

    @Override
    public @NotNull IsExpr copy() {
        return copyParentAndSourceTo(new IsExpr(getValue().copy(), getType().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s is %s", getValue(), getType());
    }
}
