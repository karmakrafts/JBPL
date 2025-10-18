package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import org.jetbrains.annotations.NotNull;

public final class AnonBlockExpr extends AbstractStatementContainer implements Expr, ScopeOwner {
    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return this; // Anon blocks evaluate to themselves
    }
}
