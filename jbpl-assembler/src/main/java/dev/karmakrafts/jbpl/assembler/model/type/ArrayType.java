package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.ArrayExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public record ArrayType(Type elementType) implements Type {
    @Override
    public @NotNull TypeCategory getCategory() {
        return TypeCategory.ARRAY;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) {
        return new ArrayExpr(this);
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) throws EvaluationException {
        if (!elementType.getCategory().isMaterializable()) {
            throw new UnsupportedOperationException(String.format("Array of type %s cannot be materialized",
                elementType));
        }
        return org.objectweb.asm.Type.getType(String.format("[%s", elementType.materialize(context).getDescriptor()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("[%s]", elementType);
    }
}
