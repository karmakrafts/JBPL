package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.type.PreproClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class PreproClassExpr extends AbstractCallExpr implements Expr {
    public final PreproClassType type;

    public PreproClassExpr(final @NotNull PreproClassType type) {
        super(LiteralExpr.unit()); // Class instantiations don't have a receiver
        this.type = type;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return type;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        super.evaluate(context);
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        return null;
    }
}
