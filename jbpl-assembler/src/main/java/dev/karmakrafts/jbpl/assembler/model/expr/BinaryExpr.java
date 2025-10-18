package dev.karmakrafts.jbpl.assembler.model.expr;

import org.jetbrains.annotations.NotNull;

public final class BinaryExpr extends AbstractExprContainer implements Expr {
    public Op op;

    public BinaryExpr(final @NotNull Expr lhs, final @NotNull Expr rhs, final @NotNull Op op) {
        addExpression(lhs);
        addExpression(rhs);
        this.op = op;
    }

    public enum Op {
        ADD, SUB, MUL, DIV, REM, // Comparisons
        EQ, NE, LT, LE, GT, GE, // Logic
        LSH, RSH, URSH, AND, OR, XOR, SC_AND, SC_OR
    }
}
