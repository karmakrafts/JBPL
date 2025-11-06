package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ClassType(String name) implements Type {
    public ClassType(final @NotNull Class<?> type) {
        this(org.objectweb.asm.Type.getInternalName(type));
    }

    public static @NotNull Optional<ClassType> tryParse(final @Nullable String value) {
        if (value == null || !value.startsWith("<") || !value.endsWith(">")) {
            return Optional.empty();
        }
        final var name = value.substring(1, value.length() - 1);
        return Optional.of(new ClassType(name));
    }

    public @NotNull Class<?> loadClass() throws ClassNotFoundException {
        return Class.forName(name.replace('/', '.'));
    }

    @Override
    public @NotNull TypeCategory getCategory() {
        return TypeCategory.OBJECT;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) {
        throw new UnsupportedOperationException("JVM class has no default value");
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) {
        return org.objectweb.asm.Type.getObjectType(name);
    }

    @Override
    public @NotNull String toString() {
        return String.format("<%s>", name);
    }
}
