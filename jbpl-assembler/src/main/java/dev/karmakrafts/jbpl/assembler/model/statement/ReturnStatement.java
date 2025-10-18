package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.UnitExpr;
import org.jetbrains.annotations.NotNull;

public final class ReturnStatement extends AbstractExprContainer implements Statement {
    public ReturnStatement(final @NotNull Expr value) {
        addExpression(value);
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return new UnitExpr();
    }
}
