package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.UnitExpr;
import org.jetbrains.annotations.NotNull;

public final class CompoundStatement extends AbstractElementContainer implements Statement {
    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        for (final var element : elements) {
            if (element instanceof Statement statement) {
                statement.evaluate(context);
            }
        }
        return new UnitExpr();
    }
}
