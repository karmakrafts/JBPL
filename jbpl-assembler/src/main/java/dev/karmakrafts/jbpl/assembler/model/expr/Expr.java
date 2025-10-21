package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public interface Expr extends Statement {
    @NotNull Type getType(final @NotNull AssemblerContext context);

    @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context);

    @Override
    default void evaluate(final @NotNull AssemblerContext context) {
        // Regular evaluation is a noop for expressions by default
    }

    /**
     * Evaluate this expression into a constant scalar value and
     * unwraps it as the given type.
     *
     * @param context The assembler context instance of the current evaluation pass.
     * @param type    The type to unwrap the evaluated {@link LiteralExpr} as.
     * @param <T>     The type of constant value this expression is being evaluated into.
     * @return The evaluated, unwrapped constant value of this expression.
     */
    default <T> @NotNull T evaluateAsConst(final @NotNull AssemblerContext context, final @NotNull Class<T> type) {
        return type.cast(evaluateAsConst(context).value);
    }

    /**
     * Functions to bridge const evaluated expression into ObjectWeb constant values.
     * This will automatically materialize any {@link Type} results, and pass through
     * any other result type as described by {@link #evaluateAsConst(AssemblerContext, Class)}.
     *
     * @param context The assembler context instance of the current evaluation pass.
     * @return The evaluated, unwrapped result of evaluating this expression into a constant value.
     */
    default @NotNull Object evaluateAsConstAndMaterialize(final @NotNull AssemblerContext context) {
        final var type = getType(context);
        if (type == PreproType.TYPE) {
            // Const types are materialized after unwrapping
            return evaluateAsConst(context, Type.class).materialize(context);
        }
        return evaluateAsConst(context, Object.class);
    }
}
