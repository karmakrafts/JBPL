package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public final class StringLerpExpr extends AbstractExprContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return BuiltinType.STRING;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var buffer = new StringBuilder();
        final var expressions = getExpressions();
        for (final var expr : expressions) {
            buffer.append(expr.evaluateAsConst(context, Object.class));
        }
        context.pushValue(LiteralExpr.of(buffer.toString()));
    }

    @Override
    public @NotNull StringLerpExpr copy() {
        final var result = copyParentAndSourceTo(new StringLerpExpr());
        result.addExpressions(getExpressions().stream().map(Expr::copy).toList());
        return result;
    }

    @Override
    public @NotNull String toString() {
        final var builder = new StringBuilder();
        builder.append('"');
        // @formatter:off
        builder.append(getExpressions().stream()
            .map(expr -> {
                if(expr instanceof LiteralExpr literalExpr && literalExpr.type == BuiltinType.STRING) {
                    return literalExpr.value.toString();
                }
                return expr.toString();
            })
            .collect(Collectors.joining("")));
        // @formatter:on
        builder.append('"');
        return builder.toString();
    }
}
