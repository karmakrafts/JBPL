package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class LabelStatement extends AbstractExprContainer implements Statement {
    public static final int NAME_INDEX = 0;

    public LabelStatement(final @NotNull Expr name) {
        addExpression(name);
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        // Make this local available in the current frame during further evaluation

        final var name = getName().evaluateAsConst(context, String.class);
        context.peekFrame().labels.put(name, this);
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        getExpressions().set(NAME_INDEX, name);
    }
}
