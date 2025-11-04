package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.PreproClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public final class PreproClassExpr extends AbstractCallExpr implements Expr {
    public final PreproClassType type;

    public PreproClassExpr(final @NotNull PreproClassType type) {
        super();
        this.type = type;
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return type;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var instance = new PreproClassExpr(type);
        instance.setParent(getParent());
        instance.setTokenRange(getTokenRange());
        // @formatter:off
        instance.addExpressions(getExpressions().stream()
            .map(ExceptionUtils.unsafeFunction(expr -> expr.evaluateAsConst(context)))
            .toList());
        // @formatter:on
        context.pushValue(new LiteralExpr(type, instance));
    }

    @Override
    public @NotNull PreproClassExpr copy() {
        // We can ignore receiver for class instantiations here
        return copyParentAndSourceTo(new PreproClassExpr(type));
    }

    @Override
    public @NotNull String toString() { // @formatter:off
        return String.format("%s(%s)", type.name(), getArguments().stream()
            .map(pair -> pair.right().toString())
            .collect(Collectors.joining(", ")));
    } // @formatter:on
}
