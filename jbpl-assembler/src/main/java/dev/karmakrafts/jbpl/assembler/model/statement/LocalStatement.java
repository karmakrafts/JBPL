package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.UnitExpr;
import org.jetbrains.annotations.NotNull;

public final class LocalStatement extends AbstractElement implements Statement {
    public Expr name;

    public LocalStatement(final @NotNull Expr name) {
        this.name = name;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return new UnitExpr();
    }
}
