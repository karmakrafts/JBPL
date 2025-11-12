package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public interface Expr extends Statement {
    @Override
    @NotNull Expr copy();

    @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException;

    default @NotNull LiteralExpr evaluateAsConst(final @NotNull EvaluationContext context) throws EvaluationException {
        // After evaluation
        evaluate(context);
        return (LiteralExpr) context.popValue();
    }

    default <T> @NotNull T evaluateAsConst(final @NotNull EvaluationContext context,
                                           final @NotNull Class<T> type) throws EvaluationException {
        return type.cast(evaluateAsConst(context).value);
    }

    default @NotNull Object evaluateAsConstAndMaterialize(final @NotNull EvaluationContext context) throws EvaluationException {
        final var type = getType(context);
        if (type == PreproType.TYPE) {
            // Const types are materialized after unwrapping
            return evaluateAsConst(context, Type.class).materialize(context);
        }
        return evaluateAsConst(context, Object.class);
    }

    default boolean isUnit() {
        return this instanceof LiteralExpr literalExpr && literalExpr.type == BuiltinType.VOID;
    }
}
