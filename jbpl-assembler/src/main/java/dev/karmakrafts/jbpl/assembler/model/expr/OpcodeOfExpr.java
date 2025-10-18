package dev.karmakrafts.jbpl.assembler.model.expr;

import org.jetbrains.annotations.NotNull;

public final class OpcodeOfExpr extends AbstractExprContainer implements Expr {
    public OpcodeOfExpr(final @NotNull Expr expr) {
        addExpression(expr);
    }
}
