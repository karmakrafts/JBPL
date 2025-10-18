package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ArrayType(Type elementType, int dimensions) implements Type {
    public ArrayType(final @NotNull Type elementType, final int dimensions) {
        this.elementType = elementType;
        this.dimensions = dimensions;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isMaterializable() {
        return elementType.isMaterializable();
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context) {
        if (!isMaterializable()) {
            throw new UnsupportedOperationException(String.format("Array of type %s cannot be materialized",
                elementType));
        }
        return org.objectweb.asm.Type.getType(String.format("[%s", elementType.materialize(context).getDescriptor()));
    }

    @Override
    public @NotNull ArrayType array(final int dimensions) {
        return new ArrayType(elementType, this.dimensions + dimensions);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (!(obj instanceof ArrayType arrayType)) {
            return false;
        }
        return elementType.equals(arrayType.elementType) && dimensions == arrayType.dimensions;
    }

    @Override
    public @NotNull String toString() {
        return String.format("ArrayType[elementType=%s,dimensions=%d]", elementType, dimensions);
    }
}
