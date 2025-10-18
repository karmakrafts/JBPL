package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class IsExpr extends AbstractExprContainer implements Expr {
    public Type type;

    public IsExpr(final @NotNull Expr expr, final @NotNull Type type) {
        addExpression(expr);
        this.type = type;
    }
}
