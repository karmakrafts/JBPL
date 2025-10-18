package dev.karmakrafts.jbpl.assembler.model.expr;

import org.jetbrains.annotations.NotNull;

public final class UnaryExpr extends AbstractExprContainer implements Expr {
    public Op op;

    public UnaryExpr(final @NotNull Expr value, final @NotNull Op op) {
        addExpression(value);
        this.op = op;
    }

    public enum Op {
        PLUS, MINUS, INVERSE, NOT
    }
}
