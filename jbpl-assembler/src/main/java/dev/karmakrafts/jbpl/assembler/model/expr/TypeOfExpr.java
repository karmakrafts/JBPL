package dev.karmakrafts.jbpl.assembler.model.expr;

import org.jetbrains.annotations.NotNull;

public final class TypeOfExpr extends AbstractExprContainer implements Expr {
    public TypeOfExpr(final @NotNull Expr expr) {
        addExpression(expr);
    }
}
