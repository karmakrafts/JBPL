package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public record IntersectionType(@NotNull List<Type> alternatives) implements Type {
    public static @NotNull IntersectionType unfold(final List<Type> types) {
        final var queue = new ArrayDeque<>(types);
        final var unfoldedTypes = new ArrayList<Type>();
        while (!queue.isEmpty()) {
            final var type = queue.remove();
            if (type instanceof IntersectionType intersectionType) {
                queue.addAll(intersectionType.alternatives);
            }
            unfoldedTypes.add(type);
        }
        return new IntersectionType(unfoldedTypes);
    }

    public @NotNull IntersectionType unfold() {
        return unfold(alternatives);
    }

    @Override
    public @NotNull TypeCategory getCategory() {
        return TypeCategory.INTERSECTION;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull AssemblerContext context) {
        throw new UnsupportedOperationException("Intersection types do not have a default value");
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context) {
        throw new UnsupportedOperationException("Intersection types cannot be materialized");
    }
}
