package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.ArrayExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ArrayType(Type elementType) implements Type {
    public static @NotNull Optional<ArrayType> tryParse(final @Nullable String value) {
        if (value == null || !value.startsWith("[")) {
            return Optional.empty();
        }
        final var length = value.length();
        var dimensions = 0;
        Type elementType = null;
        for (var i = 0; i < length; i++) {
            final var c = value.charAt(i);
            if (c == '[') {
                dimensions++;
                continue;
            }
            if (c == ']') {
                break; // We can stop parsing
            }
            elementType = Type.tryParse(value.substring(dimensions, length - dimensions)).orElse(null);
        }
        if (elementType == null) {
            return Optional.empty();
        }
        var arrayType = new ArrayType(elementType);
        for (var i = 0; i < dimensions - 1; i++) {
            arrayType = new ArrayType(elementType);
        }
        return Optional.of(arrayType);
    }

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
