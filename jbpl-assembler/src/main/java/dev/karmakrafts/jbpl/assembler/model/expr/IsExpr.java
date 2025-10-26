package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class IsExpr extends AbstractExprContainer implements Expr {
    public static final int VALUE_INDEX = 0;
    public Type type;

    public IsExpr(final @NotNull Expr value, final @NotNull Type type) {
        addExpression(value);
        this.type = type;
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return BuiltinType.BOOL;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.pushValue(LiteralExpr.of(getValue().getType(context).equals(type)));
    }

    @Override
    public @NotNull IsExpr copy() {
        return copyParentAndSourceTo(new IsExpr(getValue().copy(), type));
    }
}
