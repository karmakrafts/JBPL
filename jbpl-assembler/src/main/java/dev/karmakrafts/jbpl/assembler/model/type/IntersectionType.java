package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record IntersectionType(@NotNull List<Type> alternatives) implements Type {
    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isMaterializable() {
        return false;
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context) {
        throw new UnsupportedOperationException("Intersection types cannot be materialized");
    }
}
