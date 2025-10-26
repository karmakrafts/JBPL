package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import org.jetbrains.annotations.NotNull;

public final class ReturnStatement extends AbstractExprContainer implements Statement {
    public static final int VALUE_INDEX = 0;

    public ReturnStatement(final @NotNull Expr value) {
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue();
        final var type = value.getType(context);
        if (type == BuiltinType.VOID) {
            context.ret();
            return;
        }
        context.pushValue(value.evaluateAsConst(context));
        context.ret();
    }

    @Override
    public @NotNull ReturnStatement copy() {
        return new ReturnStatement(getValue().copy());
    }
}
