package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.PreproClassExpr;
import org.jetbrains.annotations.NotNull;

public record PreproClassType(@NotNull String name) implements Type {
    @Override
    public @NotNull TypeCategory getCategory() {
        return TypeCategory.PREPROCESSOR;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull AssemblerContext context) {
        final var scope = context.getScope();
        final var clazz = context.preproClassResolver.resolve(scope, name);
        if (clazz == null) {
            throw new IllegalStateException(String.format("Could not find preprocessor class '%s' in scope %s",
                name,
                scope));
        }
        final var result = new PreproClassExpr(this);
        for (final var entry : clazz.getFields().entrySet()) {
            final var value = entry.getValue().evaluateAsConst(context, Type.class).createDefaultValue(context);
            result.addArgument(value);
        }
        return result;
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context) {
        throw new UnsupportedOperationException("Preprocessor class types cannot be materialized");
    }

    @Override
    public @NotNull String toString() {
        return name;
    }
}
