package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class StringLerpExpr extends AbstractExprContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return BuiltinType.STRING;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        final var buffer = new StringBuilder();
        final var expressions = getExpressions();
        for (final var expr : expressions) {
            if (expr instanceof LiteralExpr literalExpr) {
                buffer.append(literalExpr.value);
                continue;
            }
            buffer.append(expr.evaluateAsLiteral(context, Object.class));
        }
        return LiteralExpr.of(buffer.toString());
    }
}
