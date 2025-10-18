package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.UnitExpr;
import org.jetbrains.annotations.NotNull;

public final class YeetStatement extends AbstractExprContainer implements Statement {
    public YeetStatement(final @NotNull Expr target) {
        addExpression(target);
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return new UnitExpr();
    }
}
