package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import org.jetbrains.annotations.NotNull;

public interface Expr extends Statement {
    /**
     * Evaluate this expression into a constant state.
     * This means that all scalar values will be evaluated into {@link LiteralExpr},
     * and every non-scalar value will be evaluated recursively such that all its
     * scalar components are of type {@link LiteralExpr}.
     *
     * @param context The assembler context instance of the current evaluation pass.
     * @param type    The type to evaluate this expression into.
     *                For scalar values, this is usually {@link LiteralExpr},
     *                for any non-scalar value, this is usually the called expression type.
     * @param <E>     The type of expression this expression is being evaluated into.
     * @return The const-evaluated representation of this expression.
     */
    default <E extends Expr> @NotNull E evaluateAs(final @NotNull AssemblerContext context,
                                                   final @NotNull Class<E> type) {
        return type.cast(evaluate(context));
    }

    /**
     * Evaluate this expression into a constant scalar value and
     * unwrap it as the given type.
     *
     * @param context The assembler context instance of the current evaluation pass.
     * @param type    The type to unwrap the evaluated {@link LiteralExpr} as.
     * @param <T>     The type of constant value this expression is being evaluated into.
     * @return The evaluated, unwrapped constant value of this expression.
     */
    default <T> @NotNull T evaluateAsLiteral(final @NotNull AssemblerContext context, final @NotNull Class<T> type) {
        if (this instanceof LiteralExpr literalExpr) {
            return type.cast(literalExpr.value);
        }
        return type.cast(evaluateAs(context, LiteralExpr.class).value);
    }
}
