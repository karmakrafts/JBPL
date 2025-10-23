package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class StringLerpExpr extends AbstractExprContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return BuiltinType.STRING;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) throws EvaluationException {
        final var buffer = new StringBuilder();
        final var expressions = getExpressions();
        for (final var expr : expressions) {
            buffer.append(expr.evaluateAsConst(context, Object.class));
        }
        context.pushValue(LiteralExpr.of(buffer.toString()));
    }
}
