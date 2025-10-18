package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.UnitExpr;
import org.jetbrains.annotations.NotNull;

public final class IncludeStatement extends AbstractElement implements Statement {
    public String path;

    public IncludeStatement(final @NotNull String path) {
        this.path = path;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return new UnitExpr();
    }
}
