package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import org.jetbrains.annotations.NotNull;

public interface Type {
    boolean isMaterializable();

    boolean isObject();

    boolean isArray();

    default boolean isPrimitive() {
        return !isObject() && !isArray();
    }

    @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context);

    default @NotNull ArrayType array(final int dimensions) {
        return new ArrayType(this, dimensions);
    }
}
