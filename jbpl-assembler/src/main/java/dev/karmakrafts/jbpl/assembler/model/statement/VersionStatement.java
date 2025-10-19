package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class VersionStatement extends AbstractElement implements Statement {
    public Expr version;

    public VersionStatement(final @NotNull Expr version) {
        this.version = version;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return version;
    }
}
