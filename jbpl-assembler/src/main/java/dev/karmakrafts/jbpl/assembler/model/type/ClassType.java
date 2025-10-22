package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public record ClassType(String name) implements Type {
    public ClassType(final @NotNull Class<?> type) {
        this(org.objectweb.asm.Type.getInternalName(type));
    }

    public @NotNull Class<?> loadClass() throws ClassNotFoundException {
        final var className = name.replace('/', '.');
        return Class.forName(className);
    }

    @Override
    public @NotNull TypeCategory getCategory() {
        return TypeCategory.OBJECT;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull AssemblerContext context) {
        throw new UnsupportedOperationException("JVM class has no default value");
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context) {
        return org.objectweb.asm.Type.getObjectType(name);
    }
}
