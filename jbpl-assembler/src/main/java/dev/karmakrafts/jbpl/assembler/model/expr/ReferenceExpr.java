package dev.karmakrafts.jbpl.assembler.model.expr;

import org.jetbrains.annotations.NotNull;

public final class ReferenceExpr extends AbstractReceiverExpr implements Expr, ExprContainer {
    public String name;

    public ReferenceExpr(final @NotNull Expr receiver, final @NotNull String name) {
        super(receiver);
        this.name = name;
    }
}
