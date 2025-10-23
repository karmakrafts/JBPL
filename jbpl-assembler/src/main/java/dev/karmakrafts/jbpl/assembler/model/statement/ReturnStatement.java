package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
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
    public void evaluate(final @NotNull AssemblerContext context) throws EvaluationException {
        final var value = getValue();
        final var type = value.getType(context);
        if (type == BuiltinType.VOID) {
            context.ret();
            return;
        }
        context.pushValue(value);
        context.ret();
    }
}
